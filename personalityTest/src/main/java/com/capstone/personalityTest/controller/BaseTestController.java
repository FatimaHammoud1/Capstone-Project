package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.model.BaseTest;
import com.capstone.personalityTest.service.BaseTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base-tests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BaseTestController {

    private final BaseTestService service;

    @PostMapping
    public BaseTest create(@RequestBody BaseTest baseTest) {
        return service.createBaseTest(baseTest);
    }

    @GetMapping
    public List<BaseTest> getAll() {
        return service.getAll();
    }
}

