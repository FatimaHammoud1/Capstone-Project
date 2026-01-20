package com.capstone.personalityTest.controller.test.testcontroller;

import com.capstone.personalityTest.dto.RequestDTO.test.MetricRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.MetricResponse;
import com.capstone.personalityTest.service.test.testservice.MetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PostMapping
    public ResponseEntity<MetricResponse> createMetric(@Valid @RequestBody MetricRequest metricRequest) {
        MetricResponse createdMetric = metricService.createMetric(metricRequest);
        return new ResponseEntity<>(createdMetric, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MetricResponse>> getAllMetrics() {
        List<MetricResponse> metrics = metricService.getAllMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/base-test/{baseTestId}")
    public ResponseEntity<List<MetricResponse>> getMetricsByBaseTestId(@PathVariable Long baseTestId) {
        List<MetricResponse> metrics = metricService.getMetricsByBaseTestId(baseTestId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetricResponse> getMetricById(@PathVariable Long id) {
        MetricResponse metric = metricService.getMetricById(id);
        return ResponseEntity.ok(metric);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PatchMapping("/{id}")
    public ResponseEntity<MetricResponse> updateMetric(
            @PathVariable Long id,
            @Valid @RequestBody MetricRequest metricRequest) {
        MetricResponse updatedMetric = metricService.updateMetric(id, metricRequest);
        return ResponseEntity.ok(updatedMetric);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMetric(@PathVariable Long id) {
        metricService.deleteMetric(id);
        return new ResponseEntity<>("Metric deleted successfully", HttpStatus.OK);
    }
}
