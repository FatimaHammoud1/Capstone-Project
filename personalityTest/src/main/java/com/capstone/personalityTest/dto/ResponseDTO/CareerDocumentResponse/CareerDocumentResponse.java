package com.capstone.personalityTest.dto.ResponseDTO.CareerDocumentResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for career document information.
 * Returned when fetching document details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerDocumentResponse {

    /**
     * Document ID
     */
    private Long id;

    /**
     * Original filename as uploaded by admin
     */
    private String originalFilename;

    /**
     * File type/extension (pdf, docx, txt, etc.)
     */
    private String fileType;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * ID of linked base test (if any)
     */
    private Long baseTestId;

    /**
     * Name of linked base test (if any)
     */
    private String baseTestCode;

    /**
     * When the document was uploaded
     */
    private LocalDateTime uploadedAt;

    /**
     * Email of admin who uploaded the document
     */
    private String uploadedBy;

    /**
     * Whether this document has been indexed in ChromaDB
     */
    private boolean indexed;

    /**
     * Optional description of document content
     */
    private String description;
}
