package com.capstone.personalityTest.service;

import com.capstone.personalityTest.model.BaseTest;
import com.capstone.personalityTest.repository.BaseTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseTestService {

    private final BaseTestRepository baseTestRepository;

    public BaseTest createBaseTest(BaseTest baseTest) {
        return baseTestRepository.save(baseTest);
    }

    public List<BaseTest> getAll() {
        return baseTestRepository.findAll();
    }

    //delete only if versions don't exist
}

