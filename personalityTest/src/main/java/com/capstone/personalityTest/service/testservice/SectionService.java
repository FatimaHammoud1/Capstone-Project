package com.capstone.personalityTest.service.testservice;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.SectionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.mapper.TestMapper.SectionMapper;
import com.capstone.personalityTest.mapper.TestMapper.TestMapper;
import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.Test.Section;
import com.capstone.personalityTest.model.Test.Test;
import com.capstone.personalityTest.repository.TestRepo.SectionRepository;
import com.capstone.personalityTest.repository.TestRepo.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final TestRepository testRepository;
    private final SectionRepository sectionRepository;
    private final TestMapper testMapper;
    private final SectionMapper sectionMapper;

    // Add sections to test
    public TestResponse addSections(Long testId, SectionRequest sectionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);

        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }

        Test test = optionalTest.get();

        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a published test");
        }

        Section section = sectionMapper.toEntity(sectionRequest);
        section.setTest(test);

        test.getSections().add(section);

        testRepository.save(test);

        return testMapper.toDto(test);
    }

    @Transactional
    public void deleteSection(Long testId, Long sectionId) {
        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if (optionalSection.isEmpty())
            throw new EntityNotFoundException("Section not found with id " + sectionId);
        Section section = optionalSection.get();

        if (section.getTest().getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot delete a section in published test");
        }

        if (!section.getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Section does not belong to test with id " + testId);
        }

        sectionRepository.delete(section);
    }

    @Transactional
    // Update Section
    public TestResponse updateSection(Long testId, Long sectionId, SectionRequest sectionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);
        if (optionalTest.isEmpty())
            throw new EntityNotFoundException("Test not found: " + testId);

        Test test = optionalTest.get();

        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a section in a published test");
        }

        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if (optionalSection.isEmpty())
            throw new EntityNotFoundException("Section not found: " + sectionId);

        Section section = optionalSection.get();

        if (!section.getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Section does not belong to test " + testId);
        }

        sectionMapper.updateSectionFromDto(sectionRequest, section);
        testRepository.save(test);
        return testMapper.toDto(test);
    }
}
