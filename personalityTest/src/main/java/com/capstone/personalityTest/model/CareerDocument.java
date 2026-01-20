package com.capstone.personalityTest.model;

import com.capstone.personalityTest.model.Test.BaseTest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing career guidance documents uploaded by admins.
 * These documents are used by the RAG system for generating career recommendations.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "career_documents")
public class CareerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stored filename (with UUID prefix to avoid conflicts)
     */
    @Column(nullable = false)
    private String filename;

    /**
     * Original filename as uploaded by admin
     */
    @Column(nullable = false)
    private String originalFilename;

    /**
     * File type/extension (pdf, docx, txt, md, etc.)
     */
    @Column(nullable = false)
    private String fileType;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * Optional: Link document to specific test type
     * Allows organizing documents by test category
     */
    @ManyToOne
    @JoinColumn(name = "base_test_id")
    private BaseTest baseTest;

    /**
     * When the document was uploaded
     */
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * Email of admin who uploaded the document
     */
    private String uploadedBy;

    /**
     * Whether this document has been indexed in ChromaDB
     * Set to false when uploaded, true after successful indexing
     */
    @Column(nullable = false)
    private boolean indexed = false;

    /**
     * Optional description of document content
     */
    @Column(columnDefinition = "TEXT")
    private String description;
}
