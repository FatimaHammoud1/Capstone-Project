package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRegistrationRepository extends JpaRepository<StudentRegistration, Long> {
    boolean existsByExhibitionIdAndStudentId(Long exhibitionId, Long studentId);
    int countByExhibitionId(Long exhibitionId);
    List<StudentRegistration> findByStudentId(Long studentId);

    int countByExhibitionIdAndApprovedTrue(Long exhibitionId);
}
