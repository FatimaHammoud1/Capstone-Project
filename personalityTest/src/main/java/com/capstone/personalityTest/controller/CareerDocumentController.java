package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.ResponseDTO.CareerDocumentResponse.CareerDocumentResponse;
import com.capstone.personalityTest.service.CareerDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing career guidance documents.
 * Only accessible by ADMIN users.
 */
@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CareerDocumentController {

    private final CareerDocumentService documentService;

    /**
     * Upload a new career document
     * 
     * @param file The document file (PDF, DOCX, TXT, MD, etc.)
     * @param baseTestId Optional: Link document to a specific test type
     * @param description Optional: Description of document content
     * @param authentication Current authenticated user
     * @return Uploaded document metadata as DTO
     */

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload career guidance document",
            description = "Upload PDF documents for RAG system")
    public ResponseEntity<?> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) Long baseTestId,
        @RequestParam(required = false) String description,
        Authentication authentication
    ) {
        try {
            String uploadedBy = authentication.getName();
            
            CareerDocumentResponse document = documentService.uploadDocument(
                file,
                baseTestId,
                description,
                uploadedBy
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("document", document);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
                
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to save file: " + e.getMessage()));
        }
    }

    /**
     * Get all uploaded documents
     * 
     * @return List of all career documents as DTOs
     */
    @GetMapping
    public ResponseEntity<List<CareerDocumentResponse>> getAllDocuments() {
        List<CareerDocumentResponse> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents for a specific base test
     * 
     * @param baseTestId ID of the base test
     * @return List of documents linked to the base test as DTOs
     */
    @GetMapping("/base-test/{baseTestId}")
    public ResponseEntity<List<CareerDocumentResponse>> getDocumentsByBaseTest(@PathVariable Long baseTestId) {
        List<CareerDocumentResponse> documents = documentService.getDocumentsByBaseTest(baseTestId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get a specific document by ID
     * 
     * @param id Document ID
     * @return Document metadata as DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        try {
            CareerDocumentResponse document = documentService.getDocument(id);
            return ResponseEntity.ok(document);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a document
     * 
     * @param id Document ID to delete
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document deleted successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to delete file: " + e.getMessage()));
        }
    }

    /**
     * Manually trigger reindexing of all documents
     * Useful if automatic reindexing failed
     * 
     * @return Success message
     */
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> triggerReindex() {
        documentService.triggerReindex();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Reindexing triggered. This may take a few moments."
        ));
    }
}

