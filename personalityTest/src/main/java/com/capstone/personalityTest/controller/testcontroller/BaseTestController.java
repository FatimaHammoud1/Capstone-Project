package com.capstone.personalityTest.controller.testcontroller;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.BaseTestRequest;
import com.capstone.personalityTest.model.Test.BaseTest;
import com.capstone.personalityTest.service.testservice.BaseTestService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base-tests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
public class BaseTestController {

    private final BaseTestService service;

    @PostMapping
    public ResponseEntity<BaseTest> createBaseTest(@RequestBody BaseTestRequest baseTestRequest) {
        BaseTest createdTest = service.createBaseTest(baseTestRequest);
        return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
    }


    @GetMapping
    public List<BaseTest> getAll() {
        return service.getAll();
    }
}

