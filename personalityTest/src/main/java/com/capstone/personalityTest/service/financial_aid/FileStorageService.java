package com.capstone.personalityTest.service.financial_aid;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    /**
     * Uploads a file to Firebase Storage and returns a signed URL.
     *
     * @param file       The file to upload
     * @param studentId  Used to tag the file
     * @param fileType   Used for organization
     * @return String    The public (signed) URL of the uploaded file
     */
    public String storeFile(MultipartFile file, String studentId, String fileType) {
        try {
            // Get the default bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Generate unique path: financial_aid/123/FEES_1738493.pdf
            String fileName = "financial_aid/" + studentId + "/" + fileType + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Upload the file
            Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());

            // Generate a signed URL valid for 365 days (approx 1 year)
            // This ensures links work for the duration of the development and demo period.
            URL signedUrl = blob.signUrl(365, TimeUnit.DAYS);
            
            return signedUrl.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Firebase", e);
        }
    }

    public Resource loadFileAsResource(String fileUrl) {
        try {
            return new UrlResource(fileUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid file URL: " + fileUrl, e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            // Extract the path from the URL
            // Format: https://storage.googleapis.com/BUCKET_NAME/financial_aid/1/ID_123.jpg?query...
            // or: https://firebasestorage.googleapis.com/v0/b/BUCKET/o/financial_aid%2F1%2FID.jpg
            
            // Simple robust extraction for "financial_aid/..." onwards
            String path = null;
            if (fileUrl.contains("/financial_aid/")) {
                int index = fileUrl.indexOf("financial_aid/");
                // Handle potential URL encoding if coming from different format
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
            // Log but don't crash - deletion failure shouldn't stop the request
            System.err.println("Failed to delete file: " + fileUrl + " Error: " + ex.getMessage());
        }
    }
}