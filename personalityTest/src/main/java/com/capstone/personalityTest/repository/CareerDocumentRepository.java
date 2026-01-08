package com.capstone.personalityTest.repository;

import com.capstone.personalityTest.model.CareerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerDocumentRepository extends JpaRepository<CareerDocument, Long> {

    /**
     * Find document by stored filename
     */
    Optional<CareerDocument> findByFilename(String filename);

    /**
     * Find all documents for a specific base test
     */
    List<CareerDocument> findByBaseTestId(Long baseTestId);

    /**
     * Find all unindexed documents
     */
    List<CareerDocument> findByIndexedFalse();

    /**
     * Update indexed status for all documents
     */
    @Modifying
    @Query("UPDATE CareerDocument d SET d.indexed = :indexed")
    void updateAllIndexedStatus(boolean indexed);

    /**
     * Count documents by file type
     */
    long countByFileType(String fileType);

    /**
     * Check if a document with the original filename already exists
     */
    boolean existsByOriginalFilename(String originalFilename);
}
