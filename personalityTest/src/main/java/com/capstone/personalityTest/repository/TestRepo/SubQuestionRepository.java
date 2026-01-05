package com.capstone.personalityTest.repository.TestRepo;

import com.capstone.personalityTest.model.Test.SubQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// import java.util.List;

public interface SubQuestionRepository extends JpaRepository<SubQuestion, Long> {

    @Query("SELECT COUNT(sq) > 0 FROM SubQuestion sq " +
            "WHERE sq.metric.id = :metricId " +
            "AND sq.question.section.test.status = com.capstone.personalityTest.model.Enum.TestStatus.PUBLISHED")
    boolean existsByMetricIdInPublishedTests(@Param("metricId") Long metricId);

    // @Query("SELECT sq FROM SubQuestion sq " +
    //         "WHERE sq.metric.id = :metricId " +
    //         "AND sq.question.section.test.status = com.capstone.personalityTest.model.Enum.TestStatus.PUBLISHED")
    // List<SubQuestion> findByMetricIdInPublishedTests(@Param("metricId") Long metricId);
}
