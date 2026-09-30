package com.oj.platform.controller;

import com.oj.platform.dto.TestCaseDto;
import com.oj.platform.service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @GetMapping("/problems/{problemId}/testcases")
    public ResponseEntity<List<TestCaseDto>> getTestCasesByProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(testCaseService.getTestCasesByProblemId(problemId));
    }

    @PostMapping("/problems/{problemId}/testcases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseDto> createTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseDto testCaseDto) {
        TestCaseDto created = testCaseService.createTestCase(problemId, testCaseDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/testcases/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestCaseDto> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody TestCaseDto testCaseDto) {
        TestCaseDto updated = testCaseService.updateTestCase(id, testCaseDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/testcases/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteTestCase(@PathVariable Long id) {
        testCaseService.deleteTestCase(id);
        return ResponseEntity.ok(Map.of("message", "TestCase deleted successfully"));
    }
}
