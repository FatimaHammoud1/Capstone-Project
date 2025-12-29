package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.BaseTestRequest;
import com.capstone.personalityTest.mapper.TestMapper.BaseTestMapper;
import com.capstone.personalityTest.model.BaseTest;
import com.capstone.personalityTest.repository.BaseTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseTestService {

    private final BaseTestRepository baseTestRepository;
    private final BaseTestMapper baseTestMapper;

    public BaseTest createBaseTest(BaseTestRequest baseTestRequest) {
        BaseTest baseTest = baseTestMapper.toEntity(baseTestRequest);
        return baseTestRepository.save(baseTest);
    }

    public List<BaseTest> getAll() {
        return baseTestRepository.findAll();
    }

    //delete only if versions don't exist
}

