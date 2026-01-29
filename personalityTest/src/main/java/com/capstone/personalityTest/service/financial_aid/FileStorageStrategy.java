package com.capstone.personalityTest.service.financial_aid;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageStrategy {
    String storeFile(MultipartFile file, String studentId, String fileType);
    Resource loadFileAsResource(String fileUrlOrName);
    void deleteFile(String fileUrlOrName);
}
