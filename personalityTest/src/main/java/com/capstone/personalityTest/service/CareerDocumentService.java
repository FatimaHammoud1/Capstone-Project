package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.ResponseDTO.CareerDocumentResponse.CareerDocumentResponse;
import com.capstone.personalityTest.mapper.CareerDocumentMapper;
import com.capstone.personalityTest.model.Test.BaseTest;
import com.capstone.personalityTest.model.CareerDocument;
import com.capstone.personalityTest.repository.BaseTestRepository;
import com.capstone.personalityTest.repository.CareerDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CareerDocumentService {

    private final CareerDocumentRepository documentRepository;
    private final BaseTestRepository baseTestRepository;
    private final RestTemplate restTemplate;
    private final CareerDocumentMapper documentMapper;  // 🆕 MapStruct mapper

    @Value("${ai.documents.path:../ai-service/rag/uploaded_files}")
    private String documentsPath;

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "pdf", "docx", "txt", "md", "csv", "json", "xlsx"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Upload a career document
     */
    @Transactional
    public CareerDocumentResponse uploadDocument(
        MultipartFile file,
        Long baseTestId,
        String description,
        String uploadedBy
    ) throws IOException {
        
        log.info("📤 Uploading document: {}", file.getOriginalFilename());

        // Check if file with same name already exists
        if (documentRepository.existsByOriginalFilename(file.getOriginalFilename())) {
            log.warn("⚠️ Duplicate file upload attempted: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("File with name '" + file.getOriginalFilename() + "' already exists");
        }

        // Validate file
        validateFile(file);

        // Generate unique filename
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Ensure directory exists
        Path uploadPath = Paths.get(documentsPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("📁 Created upload directory: {}", uploadPath);
        }

        // Save file to filesystem
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("💾 File saved to: {}", filePath);

        // Create database record
        CareerDocument document = new CareerDocument();
        document.setFilename(storedFilename);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setFileType(fileExtension);
        document.setFileSize(file.getSize());
        document.setUploadedAt(LocalDateTime.now());
        document.setUploadedBy(uploadedBy);
        document.setDescription(description);
        document.setIndexed(false);

        // Link to base test if provided
        if (baseTestId != null) {
            BaseTest baseTest = baseTestRepository.findById(baseTestId)
                .orElseThrow(() -> new IllegalArgumentException("BaseTest not found: " + baseTestId));
            document.setBaseTest(baseTest);
        }

        CareerDocument savedDocument = documentRepository.save(document);
        log.info("✅ Document saved to database with ID: {}", savedDocument.getId());

        // Trigger reindexing asynchronously
        triggerReindex();

        // 🆕 Convert to DTO using mapper
        return documentMapper.toDto(savedDocument);
    }

    /**
     * Delete a career document
     */
    @Transactional
    public void deleteDocument(Long documentId) throws IOException {
        log.info("🗑️  Deleting document ID: {}", documentId);

        CareerDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        // Delete file from filesystem
        Path filePath = Paths.get(documentsPath, document.getFilename());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("💾 File deleted from filesystem: {}", filePath);
        }

        // Delete from database
        documentRepository.delete(document);
        log.info("✅ Document deleted from database");

        // Trigger reindexing
        triggerReindex();
    }

    /**
     * Get all documents
     */
    public List<CareerDocumentResponse> getAllDocuments() {
        // 🆕 Convert to DTOs using mapper
        return documentMapper.toDtoList(documentRepository.findAll());
    }

    /**
     * Get documents by base test
     */
    public List<CareerDocumentResponse> getDocumentsByBaseTest(Long baseTestId) {
        // 🆕 Convert to DTOs using mapper
        return documentMapper.toDtoList(documentRepository.findByBaseTestId(baseTestId));
    }

    /**
     * Get document by ID
     */
    public CareerDocumentResponse getDocument(Long id) {
        CareerDocument document = documentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
        
        // 🆕 Convert to DTO using mapper
        return documentMapper.toDto(document);
    }

    /**
     * Trigger Python AI to reindex documents
     */
    @Async
    public void triggerReindex() {
        try {
            log.info("🔄 Triggering Python AI reindex...");
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                aiServiceUrl + "/api/admin/reindex-documents",
                null,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Reindex triggered successfully");
                
                // Mark all documents as indexed
                documentRepository.updateAllIndexedStatus(true);
            } else {
                log.warn("⚠️  Reindex returned non-success status: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Failed to trigger reindex: {}", e.getMessage(), e);
            // Don't throw - document upload should succeed even if reindex fails
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Check file extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                String.format("File type '%s' not allowed. Allowed types: %s", extension, ALLOWED_EXTENSIONS)
            );
        }
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
