package com.capstone.personalityTest.service.test.testservice;

import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.CreateVersionRequest;
import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.TestRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.TestResponse;
import com.capstone.personalityTest.mapper.TestMapper.TestMapper;
import com.capstone.personalityTest.model.testm.Test.BaseTest;
import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.testm.Test.Section;
import com.capstone.personalityTest.model.testm.Test.Test;
import com.capstone.personalityTest.repository.test.BaseTestRepository;
import com.capstone.personalityTest.repository.test.TestRepo.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final TestMapper testMapper;
    private final BaseTestRepository baseTestRepository;

    // Create test for first time , then versions will be created by createVersion
    // after publishing
    public TestResponse createTest(TestRequest testRequest) {
        BaseTest baseTest = baseTestRepository.findById(testRequest.getBaseTestId())
                .orElseThrow(() -> new EntityNotFoundException("BaseTest not found"));

        Test test = testMapper.toEntity(testRequest);
        test.setBaseTest(baseTest);

        Test savedTest = testRepository.save(test);
        return testMapper.toDto(savedTest);
    }

    // Confirm test (finalize)
    public TestResponse publishTest(Long testId) {
        Optional<Test> optionalTest = testRepository.findById(testId);
        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }
        Test test = optionalTest.get();

        if (test.getSections().isEmpty()) {
            throw new IllegalStateException("Cannot publish an empty test");
        }

        test.setStatus(TestStatus.PUBLISHED); // lock
        testRepository.save(test);

        return testMapper.toDto(test);
    }

    @Transactional
    public TestResponse setTestActive(Long testId, boolean active) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found"));

        if (test.getStatus() != TestStatus.PUBLISHED) {
            throw new IllegalStateException("Only published tests can be activated/deactivated");
        }

        test.setActive(active);
        testRepository.save(test);

        return testMapper.toDto(test);
    }

    // Get all tests
    @Transactional
    public List<TestResponse> getAllTests(String role) {
        List<Test> tests;

        if (role.equals("ROLE_ADMIN")) {
            // Admin sees all tests
            tests = testRepository.findAll();
        } else {
            // User sees only published & active tests
            tests = testRepository.findByStatusAndActive(TestStatus.PUBLISHED, true);
        }

        return tests.stream()
                .map(testMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get test by ID
    public TestResponse getTestById(Long id) {
        Optional<Test> optionalTest = testRepository.findById(id);
        if (optionalTest.isEmpty())
            throw new EntityNotFoundException("Test not found: " + id);

        Test test = optionalTest.get();
        return testMapper.toDto(test);
    }

    // Update Title and description
    @Transactional
    public TestResponse updateTest(Long id, TestRequest testRequest) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id " + id));

        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a published test");
        }

        testMapper.updateTestFromDto(testRequest, test); // MapStruct updates only non-null fields

        testRepository.save(test); // Persist changes

        return testMapper.toDto(test);
    }

    @Transactional
    public void deleteTest(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id " + testId));

        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot delete a published test");
        }
        testRepository.deleteById(testId);
    }

    // A version can only be created from a published test
    @Transactional
    public TestResponse createVersion(CreateVersionRequest request) {

        BaseTest baseTest = baseTestRepository.findById(request.getBaseTestId())
                .orElseThrow(() -> new EntityNotFoundException("BaseTest not found"));

        Test source = testRepository.findById(request.getSourceTestId())
                .orElseThrow(() -> new EntityNotFoundException("Source test not found"));

        if (source.getStatus() != TestStatus.PUBLISHED) {
            throw new IllegalStateException("Only published tests can be versioned");
        }

        if (!source.getBaseTest().getId().equals(baseTest.getId())) {
            throw new IllegalArgumentException("Source test does not belong to this BaseTest");
        }

        Test newTest = new Test();
        newTest.setBaseTest(baseTest);
        newTest.setVersionName(request.getVersionName());
        newTest.setTitle(source.getTitle());
        newTest.setDescription(source.getDescription());
        newTest.setStatus(TestStatus.DRAFT);
        newTest.setActive(false);

        for (Section s : source.getSections()) {
            Section newSection = s.copy();
            newSection.setTest(newTest);
            newTest.getSections().add(newSection);
        }

        return testMapper.toDto(testRepository.save(newTest));
    }
}
