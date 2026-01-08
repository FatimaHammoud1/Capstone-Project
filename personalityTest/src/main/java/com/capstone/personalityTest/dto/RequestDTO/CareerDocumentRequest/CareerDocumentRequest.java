package com.capstone.personalityTest.dto.RequestDTO.CareerDocumentRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for uploading a career document.
 * Used when admin uploads a new document via multipart form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerDocumentRequest {

    /**
     * Optional: Link document to a specific base test
     */
    private Long baseTestId;

    /**
     * Optional: Description of document content
     */
    private String description;
}
