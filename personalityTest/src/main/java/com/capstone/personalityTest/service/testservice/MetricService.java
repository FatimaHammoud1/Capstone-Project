package com.capstone.personalityTest.service.testservice;

import com.capstone.personalityTest.dto.RequestDTO.MetricRequest;
import com.capstone.personalityTest.dto.ResponseDTO.MetricResponse;
import com.capstone.personalityTest.mapper.MetricMapper;
import com.capstone.personalityTest.model.Test.BaseTest;
import com.capstone.personalityTest.model.Test.Metric;
import com.capstone.personalityTest.repository.BaseTestRepository;
import com.capstone.personalityTest.repository.TestRepo.MetricRepository;
import com.capstone.personalityTest.exception.EntityExistsException;
import com.capstone.personalityTest.repository.TestRepo.SubQuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;
    private final MetricMapper metricMapper;
    private final BaseTestRepository baseTestRepository;
    private final SubQuestionRepository subQuestionRepository;

    public MetricResponse createMetric(MetricRequest metricRequest) {
        // Validate baseTest if provided
        BaseTest baseTest = baseTestRepository.findById(metricRequest.getBaseTestId())
                .orElseThrow(() -> new EntityNotFoundException("BaseTest not found"));

        // Check if code already exists
        if (metricRepository.findByCode(metricRequest.getCode()).isPresent()) {
            throw new EntityExistsException("Metric with code " + metricRequest.getCode() + " already exists");
        }

        Metric metric = metricMapper.toEntity(metricRequest);
        metric.setBaseTest(baseTest);

        Metric savedMetric = metricRepository.save(metric);
        return metricMapper.toDto(savedMetric);
    }

    @Transactional
    public List<MetricResponse> getAllMetrics() {
        List<Metric> metrics = metricRepository.findAll();
        return metrics.stream()
                .map(metricMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MetricResponse> getMetricsByBaseTestId(Long baseTestId) {
        List<Metric> metrics = metricRepository.findByBaseTestId(baseTestId);
        return metrics.stream()
                .map(metricMapper::toDto)
                .collect(Collectors.toList());
    }

    public MetricResponse getMetricById(Long id) {
        Optional<Metric> optionalMetric = metricRepository.findById(id);
        if (optionalMetric.isEmpty()) {
            throw new EntityNotFoundException("Metric not found: " + id);
        }

        Metric metric = optionalMetric.get();
        return metricMapper.toDto(metric);
    }

    @Transactional
    public MetricResponse updateMetric(Long id, MetricRequest metricRequest) {
        Metric metric = metricRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Metric not found with id " + id));

        // Check if metric is used in any published test
        boolean isUsedInPublishedTests = subQuestionRepository.existsByMetricIdInPublishedTests(id);

        // If code is being changed
        if (metricRequest.getCode() != null && !metricRequest.getCode().equals(metric.getCode())) {
            // Prevent code change if used in published tests (breaks historical data
            // integrity)
            if (isUsedInPublishedTests) {
                throw new IllegalStateException(
                        "Cannot change metric code: Metric is used in published tests. " +
                                "Changing the code would break historical test attempt results. " +
                                "You can only update label and description for metrics used in published tests.");
            }

            // Check if new code already exists
            if (metricRepository.findByCode(metricRequest.getCode()).isPresent()) {
                throw new EntityExistsException("Metric with code " + metricRequest.getCode() + " already exists");
            }
        }

        // Update baseTest if provided
        // if (metricRequest.getBaseTestId() != null) {
        // BaseTest baseTest =
        // baseTestRepository.findById(metricRequest.getBaseTestId())
        // .orElseThrow(() -> new EntityNotFoundException("BaseTest not found"));
        // metric.setBaseTest(baseTest);
        // }

        // Update other fields (label, description) - safe even if used in published
        // tests
        metricMapper.updateMetricFromDto(metricRequest, metric);
        metricRepository.save(metric);

        return metricMapper.toDto(metric);
    }

    @Transactional
    public void deleteMetric(Long id) {
        if (!metricRepository.existsById(id)) {
            throw new EntityNotFoundException("Metric not found with id " + id);
        }

        // Check if metric is used in any published test
        boolean isUsedInPublishedTests = subQuestionRepository.existsByMetricIdInPublishedTests(id);

        if (isUsedInPublishedTests) {
            throw new IllegalStateException(
                    "Cannot delete metric: Metric is used in published tests. " +
                            "Deleting this metric would break existing test attempts and results. " +
                            "Consider creating a new metric instead.");
        }

        metricRepository.deleteById(id);
    }
}
