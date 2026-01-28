package com.capstone.personalityTest.service.financial_aid;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class FirebaseFileStorageService implements FileStorageStrategy {

    @Override
    public String storeFile(MultipartFile file, String studentId, String fileType) {
        try {
            Bucket bucket = StorageClient.getInstance().bucket();
            String fileName = "financial_aid/" + studentId + "/" + fileType + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());
            URL signedUrl = blob.signUrl(365, TimeUnit.DAYS);
            return signedUrl.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Firebase", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileUrl) {
        try {
            return new UrlResource(fileUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid file URL: " + fileUrl, e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String path = null;
            if (fileUrl.contains("/financial_aid/")) {
                int index = fileUrl.indexOf("financial_aid/");
                path = fileUrl.substring(index).split("\\?")[0]; 
            }
            if (path != null) {
                Bucket bucket = StorageClient.getInstance().bucket();
                Blob blob = bucket.get(path);
                if (blob != null) {
                    blob.delete();
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to delete file: " + fileUrl + " Error: " + ex.getMessage());
        }
    }
}
