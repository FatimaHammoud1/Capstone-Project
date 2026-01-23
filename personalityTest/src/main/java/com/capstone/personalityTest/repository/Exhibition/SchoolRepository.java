package com.capstone.personalityTest.repository.Exhibition;


import com.capstone.personalityTest.model.Exhibition.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    java.util.List<School> findAllByOwnerId(Long ownerId);
}
