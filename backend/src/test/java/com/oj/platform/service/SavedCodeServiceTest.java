package com.oj.platform.service;

import com.oj.platform.dto.SavedCodeDto;
import com.oj.platform.entity.Problem;
import com.oj.platform.entity.SavedCode;
import com.oj.platform.entity.User;
import com.oj.platform.repository.ProblemRepository;
import com.oj.platform.repository.SavedCodeRepository;
import com.oj.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedCodeServiceTest {

    @Mock
    private SavedCodeRepository savedCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemRepository problemRepository;

    private SavedCodeService savedCodeService;

    private User testUser;
    private Problem testProblem;

    @BeforeEach
    void setUp() {
        savedCodeService = new SavedCodeService(savedCodeRepository, userRepository, problemRepository);

        testUser = new User("Test User", "testuser", "test@example.com", "hashed", com.oj.platform.entity.Role.ROLE_USER);
        testUser.setId(1L);

        testProblem = new Problem("Two Sum", "desc", com.oj.platform.entity.Difficulty.EASY, "Arrays", "starter");
        testProblem.setId(10L);
    }

    @Test
    void testSaveCreatesNewRecordWhenNoneExists() {
        when(savedCodeRepository.findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "JAVA"))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(problemRepository.findById(10L)).thenReturn(Optional.of(testProblem));

        SavedCode persisted = new SavedCode(testUser, testProblem, "JAVA", "public class Solution {}");
        persisted.setUpdatedAt(java.time.LocalDateTime.now());
        when(savedCodeRepository.save(any(SavedCode.class))).thenReturn(persisted);

        SavedCodeDto result = savedCodeService.saveOrUpdate(1L, 10L, "JAVA", "public class Solution {}");

        assertNotNull(result);
        assertEquals("JAVA", result.getLanguage());
        assertEquals("public class Solution {}", result.getCode());

        ArgumentCaptor<SavedCode> captor = ArgumentCaptor.forClass(SavedCode.class);
        verify(savedCodeRepository).save(captor.capture());
        assertNull(captor.getValue().getId(), "A brand new SavedCode should not already have an id");
        verify(userRepository).findById(1L);
        verify(problemRepository).findById(10L);
    }

    @Test
    void testSaveUpdatesExistingRecordInsteadOfCreatingDuplicate() {
        SavedCode existing = new SavedCode(testUser, testProblem, "JAVA", "old code");
        when(savedCodeRepository.findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "JAVA"))
                .thenReturn(Optional.of(existing));
        when(savedCodeRepository.save(any(SavedCode.class))).thenAnswer(inv -> inv.getArgument(0));

        SavedCodeDto result = savedCodeService.saveOrUpdate(1L, 10L, "JAVA", "new updated code");

        assertEquals("new updated code", result.getCode());
        // Must never look up user/problem again for an update - and must never create a
        // second row for the same (user, problem, language).
        verify(userRepository, never()).findById(anyLong());
        verify(problemRepository, never()).findById(anyLong());
        verify(savedCodeRepository, times(1)).save(any(SavedCode.class));
    }

    @Test
    void testSavedCodeIsSeparatedByLanguage() {
        when(savedCodeRepository.findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "JAVA"))
                .thenReturn(Optional.empty());
        when(savedCodeRepository.findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "PYTHON"))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(problemRepository.findById(10L)).thenReturn(Optional.of(testProblem));
        when(savedCodeRepository.save(any(SavedCode.class))).thenAnswer(inv -> inv.getArgument(0));

        SavedCodeDto javaSave = savedCodeService.saveOrUpdate(1L, 10L, "JAVA", "java code");
        SavedCodeDto pythonSave = savedCodeService.saveOrUpdate(1L, 10L, "PYTHON", "python code");

        assertEquals("JAVA", javaSave.getLanguage());
        assertEquals("java code", javaSave.getCode());
        assertEquals("PYTHON", pythonSave.getLanguage());
        assertEquals("python code", pythonSave.getCode());

        // Each language must be looked up independently - confirms they are never mixed.
        verify(savedCodeRepository).findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "JAVA");
        verify(savedCodeRepository).findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "PYTHON");
    }

    @Test
    void testGetSavedCodeReturnsEmptyWhenNoneExists() {
        when(savedCodeRepository.findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "CPP"))
                .thenReturn(Optional.empty());

        Optional<SavedCodeDto> result = savedCodeService.getSavedCode(1L, 10L, "CPP");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSavedCodeReturnsStoredCodeWhenPresent() {
        SavedCode existing = new SavedCode(testUser, testProblem, "JAVASCRIPT", "console.log('hi');");
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        when(savedCodeRepository.findByUserIdAndProblemIdAndLanguageIgnoreCase(1L, 10L, "JAVASCRIPT"))
                .thenReturn(Optional.of(existing));

        Optional<SavedCodeDto> result = savedCodeService.getSavedCode(1L, 10L, "JAVASCRIPT");

        assertTrue(result.isPresent());
        assertEquals("console.log('hi');", result.get().getCode());
        assertEquals("JAVASCRIPT", result.get().getLanguage());
        assertEquals(10L, result.get().getProblemId());
    }
}
