package com.oj.platform.service;

import com.oj.platform.dto.TestCaseDto;
import com.oj.platform.entity.Problem;
import com.oj.platform.entity.TestCase;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.ProblemRepository;
import com.oj.platform.repository.TestCaseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public TestCaseService(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public List<TestCaseDto> getTestCasesByProblemId(Long problemId) {
        List<TestCase> testCases = testCaseRepository.findByProblemId(problemId);
        boolean isAdmin = isCurrentUserAdmin();
        List<TestCaseDto> dtos = new ArrayList<>();
        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            TestCaseDto dto = mapToDto(tc);
            dto.setTestCaseNumber(i + 1);
            if (tc.getIsHidden() != null && tc.getIsHidden() && !isAdmin) {
                dto.setInput(null);
                dto.setExpectedOutput(null);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public TestCaseDto createTestCase(Long problemId, TestCaseDto dto) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Problem not found with id: " + problemId));

        long existingTestCases = testCaseRepository.findByProblemId(problemId).size();
        if (existingTestCases >= 25) {
            throw new IllegalArgumentException("A problem can have a maximum of 25 test cases.");
        }

        TestCase testCase = new TestCase(
                problem,
                dto.getInput(),
                dto.getExpectedOutput(),
                dto.getIsHidden() != null ? dto.getIsHidden() : false
        );
        testCase.setTimeLimitMs(dto.getTimeLimitMs());
        testCase.setMemoryLimitMb(dto.getMemoryLimitMb());

        TestCase saved = testCaseRepository.save(testCase);
        TestCaseDto result = mapToDto(saved);
        result.setTestCaseNumber((int) existingTestCases + 1);
        return result;
    }

    @Transactional
    public TestCaseDto updateTestCase(Long id, TestCaseDto dto) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase not found with id: " + id));

        testCase.setInput(dto.getInput());
        testCase.setExpectedOutput(dto.getExpectedOutput());
        if (dto.getIsHidden() != null) {
            testCase.setIsHidden(dto.getIsHidden());
        }
        testCase.setTimeLimitMs(dto.getTimeLimitMs());
        testCase.setMemoryLimitMb(dto.getMemoryLimitMb());

        TestCase updated = testCaseRepository.save(testCase);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteTestCase(Long id) {
        if (!testCaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("TestCase not found with id: " + id);
        }
        testCaseRepository.deleteById(id);
    }

    private TestCaseDto mapToDto(TestCase testCase) {
        TestCaseDto dto = new TestCaseDto();
        dto.setId(testCase.getId());
        dto.setProblemId(testCase.getProblem().getId());
        dto.setInput(testCase.getInput());
        dto.setExpectedOutput(testCase.getExpectedOutput());
        dto.setIsHidden(testCase.getIsHidden());
        dto.setTimeLimitMs(testCase.getTimeLimitMs());
        dto.setMemoryLimitMb(testCase.getMemoryLimitMb());
        dto.setCreatedAt(testCase.getCreatedAt());
        return dto;
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
    }
}
