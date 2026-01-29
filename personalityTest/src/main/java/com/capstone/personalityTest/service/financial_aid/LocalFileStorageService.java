package com.capstone.personalityTest.service.financial_aid;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageStrategy {

    @Value("${file.upload.dir}")
    private String uploadDir;
    
    // We cannot use constructor injection easily with @Value if we want the logic to run
    // correctly with multiple profiles, so we init path lazily or via PostConstruct.
    // However, simplest way here is just to resolve it in methods or use a field.
    
    private Path getRootLocation() {
        Path location = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(location);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
        return location;
    }

    @Override
    public String storeFile(MultipartFile file, String studentId, String fileType) {
        String fileName = studentId + "_" + fileType + "_" + System.currentTimeMillis() 
                        + "_" + file.getOriginalFilename();
        try {
            Path targetLocation = getRootLocation().resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // IMPORTANT: Return a full URL that the frontend can use to download it
            // This assumes we have an endpoint that serves files by filename
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/financial-aid/files/")
                    .path(fileName)
                    .toUriString();
                    
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            // If the input is a full URL (e.g. http://localhost:8080/...), extract just the filename
            String cleanFileName = fileName;
            if (fileName.contains("/files/")) {
                cleanFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }

            Path filePath = getRootLocation().resolve(cleanFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + cleanFileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            // Extract filename if full URL is passed
            String cleanFileName = fileName;
            if (fileName != null && fileName.contains("/files/")) {
                cleanFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            
            Path filePath = getRootLocation().resolve(cleanFileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }
}
