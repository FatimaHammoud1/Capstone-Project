package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRegistrationRepository extends JpaRepository<StudentRegistration, Long> {
    boolean existsByExhibitionIdAndStudentId(Long exhibitionId, Long studentId);
    int countByExhibitionId(Long exhibitionId);
    List<StudentRegistration> findByStudentId(Long studentId);
    List<StudentRegistration> findByExhibitionId(Long exhibitionId);

    int countByExhibitionIdAndApprovedTrue(Long exhibitionId);
    
    Optional<StudentRegistration> findByExhibitionIdAndStudentId(Long exhibitionId, Long studentId);
    
    int countByExhibitionIdAndAttendedAtIsNotNull(Long exhibitionId);
}
