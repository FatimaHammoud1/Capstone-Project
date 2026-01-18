package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.StudentRegistrationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.ExhibitionFeedback;
import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionFeedbackRepository;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.StudentRegistrationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionFeedbackService {

    private final ExhibitionFeedbackRepository feedbackRepository;
    private final StudentRegistrationRepository registrationRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Submit Feedback -----------------
    // 7️⃣ Security fix: Use studentEmail instead of studentId
    public ExhibitionFeedback submitFeedback(Long exhibitionId, String studentEmail, Integer rating, String comments) {
        
        UserInfo studentUser = userInfoRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    
        // Check that exhibition exists
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // 6️⃣ Restrict feedback submission: Allow only if COMPLETED
        if (exhibition.getStatus() != ExhibitionStatus.COMPLETED) {
            throw new RuntimeException("Feedback can only be submitted for COMPLETED exhibitions");
        }

        // Check that student exists and attended
        StudentRegistration registration = registrationRepository.findByExhibitionIdAndStudentId(exhibitionId, studentUser.getId())
                .orElseThrow(() -> new RuntimeException("Student registration not found"));

        if (registration.getStatus() != StudentRegistrationStatus.ATTENDED) {
            throw new RuntimeException("Only students who attended can submit feedback");
        }

        // Create feedback
        ExhibitionFeedback feedback = new ExhibitionFeedback();
        feedback.setExhibition(exhibition);
        feedback.setStudent(registration.getStudent());
        feedback.setRating(rating);
        feedback.setComments(comments);
        feedback.setCreatedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    // ----------------- Fetch Feedback for Exhibition -----------------
    public List<ExhibitionFeedback> getFeedbackForExhibition(Long exhibitionId) {
        return feedbackRepository.findByExhibitionId(exhibitionId);
    }
}
