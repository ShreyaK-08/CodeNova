package com.oj.platform.service;

import com.oj.platform.dto.ContestDto;
import com.oj.platform.dto.ContestProblemRequest;
import com.oj.platform.dto.ContestRegistrationDto;
import com.oj.platform.dto.ContestRequest;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.repository.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Focused tests for the Contest module (Task 4). Contest solving/submission is
 * out of scope, per the task, and is not tested here.
 */
@ExtendWith(MockitoExtension.class)
class ContestServiceTest {

    @Mock private ContestRepository contestRepository;
    @Mock private ContestProblemRepository contestProblemRepository;
    @Mock private ContestRegistrationRepository contestRegistrationRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ContestAttemptRepository contestAttemptRepository;
    @Mock private SubmissionRepository submissionRepository;

    private ContestService contestService;

    private User adminUser;
    private User studentUser;
    private Problem twoSum;
    private Problem binarySearch;
    private Problem validParentheses;

    @BeforeEach
    void setUp() {
        contestService = new ContestService(contestRepository, contestProblemRepository,
                contestRegistrationRepository, problemRepository, userRepository,
                contestAttemptRepository, submissionRepository);

        adminUser = new User("Admin", "admin", "admin@example.com", "hashed", Role.ROLE_ADMIN);
        adminUser.setId(1L);

        studentUser = new User("Student", "student", "student@example.com", "hashed", Role.ROLE_USER);
        studentUser.setId(2L);

        twoSum = new Problem("Two Sum", "desc", Difficulty.EASY, "Arrays", "starter");
        twoSum.setId(10L);

        binarySearch = new Problem("Binary Search", "desc", Difficulty.EASY, "Binary Search", "starter");
        binarySearch.setId(11L);

        validParentheses = new Problem("Valid Parentheses", "desc", Difficulty.EASY, "Stack", "starter");
        validParentheses.setId(12L);
    }

    private ContestRequest baseValidRequest() {
        ContestRequest req = new ContestRequest();
        req.setTitle("Weekly Contest #1");
        req.setOrganizationName("CodeNova University");
        req.setDescription("A weekly contest");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        return req;
    }

    // 1. Admin can create a contest.
    @Test
    void testAdminCanCreateContest() {
        ContestRequest request = baseValidRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(contestRepository.save(any(Contest.class))).thenAnswer(inv -> {
            Contest c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(100L)).thenReturn(List.of());

        ContestDto result = contestService.createContest(request, 1L);

        assertNotNull(result);
        assertEquals("Weekly Contest #1", result.getTitle());
        assertEquals("CodeNova University", result.getOrganizationName());
        assertEquals("DRAFT", result.getStatus());
        verify(contestRepository).save(any(Contest.class));
    }

    // 2. Contest requires organization name (bean validation on the DTO itself, the same
    // mechanism Spring's @Valid triggers at the controller layer).
    @Test
    void testContestRequiresOrganizationName() {
        ContestRequest request = baseValidRequest();
        request.setOrganizationName(""); // blank

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<ContestRequest>> violations = validator.validate(request);
            boolean hasOrgNameViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("organizationName"));
            assertTrue(hasOrgNameViolation, "Expected a validation error on organizationName when blank");
        } catch (Exception e) {
            fail("Validator setup failed: " + e.getMessage());
        }
    }

    // 3. Contest end time must be after start time.
    @Test
    void testEndTimeMustBeAfterStartTime() {
        ContestRequest request = baseValidRequest();
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        request.setStartTime(start);
        request.setEndTime(start.minusHours(1)); // end before start

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> contestService.createContest(request, 1L));
        assertTrue(ex.getMessage().toLowerCase().contains("end time"));
        verify(contestRepository, never()).save(any(Contest.class));
    }

    // 4. Contest can contain multiple existing problems.
    @Test
    void testContestCanContainMultipleExistingProblems() {
        ContestRequest request = baseValidRequest();
        ContestProblemRequest p1 = new ContestProblemRequest();
        p1.setProblemId(10L);
        p1.setDisplayOrder(1);
        p1.setPoints(100);
        ContestProblemRequest p2 = new ContestProblemRequest();
        p2.setProblemId(11L);
        p2.setDisplayOrder(2);
        p2.setPoints(150);
        request.setProblems(List.of(p1, p2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(contestRepository.save(any(Contest.class))).thenAnswer(inv -> {
            Contest c = inv.getArgument(0);
            c.setId(200L);
            return c;
        });
        when(problemRepository.findById(10L)).thenReturn(Optional.of(twoSum));
        when(problemRepository.findById(11L)).thenReturn(Optional.of(binarySearch));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(200L)).thenReturn(List.of(
                new ContestProblem(null, twoSum, 1, 100),
                new ContestProblem(null, binarySearch, 2, 150)
        ));

        ContestDto result = contestService.createContest(request, 1L);

        assertEquals(2, result.getProblems().size());
        assertEquals("Two Sum", result.getProblems().get(0).getProblemTitle());
        assertEquals(100, result.getProblems().get(0).getPoints());
        assertEquals("Binary Search", result.getProblems().get(1).getProblemTitle());

        // Existing Problem records must be looked up, never duplicated/created.
        verify(problemRepository, never()).save(any());
    }

    // 5. User can register for a contest.
    @Test
    void testUserCanRegisterForContest() {
        Contest contest = new Contest("Weekly", "Org", "desc", LocalDateTime.now(), LocalDateTime.now().plusHours(1), adminUser);
        contest.setId(300L);

        when(contestRepository.findById(300L)).thenReturn(Optional.of(contest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 300L)).thenReturn(false);
        when(contestRegistrationRepository.save(any(ContestRegistration.class))).thenAnswer(inv -> {
            ContestRegistration r = inv.getArgument(0);
            r.setId(1L);
            r.setRegisteredAt(LocalDateTime.now());
            return r;
        });

        ContestRegistrationDto result = contestService.register(300L, 2L);

        assertTrue(result.isRegistered());
        assertNotNull(result.getRegisteredAt());
        verify(contestRegistrationRepository).save(any(ContestRegistration.class));
    }

    // 6. Same user cannot register twice.
    @Test
    void testSameUserCannotRegisterTwice() {
        Contest contest = new Contest("Weekly", "Org", "desc", LocalDateTime.now(), LocalDateTime.now().plusHours(1), adminUser);
        contest.setId(300L);

        when(contestRepository.findById(300L)).thenReturn(Optional.of(contest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 300L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> contestService.register(300L, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("already registered"));
        verify(contestRegistrationRepository, never()).save(any(ContestRegistration.class));
    }

    // 7. Contest can be retrieved.
    @Test
    void testContestCanBeRetrieved() {
        Contest contest = new Contest("Weekly", "Org", "desc", LocalDateTime.now(), LocalDateTime.now().plusHours(1), adminUser);
        contest.setId(400L);
        contest.setStatus(ContestStatus.PUBLISHED);

        when(contestRepository.findById(400L)).thenReturn(Optional.of(contest));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(400L)).thenReturn(List.of());

        ContestDto result = contestService.getContest(400L);

        assertEquals(400L, result.getId());
        assertEquals("Weekly", result.getTitle());
        assertEquals("PUBLISHED", result.getStatus());
    }

    // Deletion is blocked once a contest has real registrations (the "safe delete" rule).
    @Test
    void testDeleteIsBlockedWhenRegistrationsExist() {
        when(contestRepository.existsById(500L)).thenReturn(true);
        when(contestRegistrationRepository.existsByContestId(500L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> contestService.deleteContest(500L));
        verify(contestRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteSucceedsWhenNoRegistrationsExist() {
        when(contestRepository.existsById(501L)).thenReturn(true);
        when(contestRegistrationRepository.existsByContestId(501L)).thenReturn(false);

        contestService.deleteContest(501L);

        verify(contestRepository).deleteById(501L);
    }

    // 8. Updating a contest with the same problems reuses existing relationships without creating duplicates.
    @Test
    void testUpdateContestWithSameProblemsDoesNotDuplicate() {
        Contest contest = new Contest("Weekly", "Org", "desc", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), adminUser);
        contest.setId(100L);

        ContestProblem cp1 = new ContestProblem(contest, twoSum, 1, 100);
        cp1.setId(1L);
        ContestProblem cp2 = new ContestProblem(contest, binarySearch, 2, 150);
        cp2.setId(2L);
        contest.getContestProblems().addAll(List.of(cp1, cp2));

        when(contestRepository.findById(100L)).thenReturn(Optional.of(contest));
        when(contestRepository.save(any(Contest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(100L)).thenReturn(new ArrayList<>(List.of(cp1, cp2)));

        ContestRequest updateReq = baseValidRequest();
        ContestProblemRequest req1 = new ContestProblemRequest();
        req1.setProblemId(10L);
        req1.setDisplayOrder(1);
        req1.setPoints(120); // updated points
        ContestProblemRequest req2 = new ContestProblemRequest();
        req2.setProblemId(11L);
        req2.setDisplayOrder(2);
        req2.setPoints(180); // updated points
        updateReq.setProblems(List.of(req1, req2));

        ContestDto result = contestService.updateContest(100L, updateReq);

        assertNotNull(result);
        // Verify no deletions occurred
        verify(contestProblemRepository, never()).deleteAll(anyList());
        // Verify no calls to look up problems to create new entities
        verify(problemRepository, never()).findById(anyLong());
        // Verify existing points updated
        assertEquals(120, cp1.getPoints());
        assertEquals(180, cp2.getPoints());
    }

    // 9. Updating a contest reconciles added and removed problems correctly.
    @Test
    void testUpdateContestReconcilesAddedAndRemovedProblems() {
        Contest contest = new Contest("Weekly", "Org", "desc", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), adminUser);
        contest.setId(100L);

        ContestProblem cp1 = new ContestProblem(contest, twoSum, 1, 100);
        cp1.setId(1L);
        ContestProblem cp2 = new ContestProblem(contest, binarySearch, 2, 150);
        cp2.setId(2L);
        contest.getContestProblems().addAll(new ArrayList<>(List.of(cp1, cp2)));

        when(contestRepository.findById(100L)).thenReturn(Optional.of(contest));
        when(contestRepository.save(any(Contest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(100L)).thenReturn(new ArrayList<>(List.of(cp1, cp2)));
        when(problemRepository.findById(12L)).thenReturn(Optional.of(validParentheses));

        ContestRequest updateReq = baseValidRequest();
        // Keep Problem 10 (Two Sum), drop Problem 11 (Binary Search), add Problem 12 (Valid Parentheses)
        ContestProblemRequest req1 = new ContestProblemRequest();
        req1.setProblemId(10L);
        req1.setDisplayOrder(1);
        req1.setPoints(100);
        ContestProblemRequest req3 = new ContestProblemRequest();
        req3.setProblemId(12L);
        req3.setDisplayOrder(2);
        req3.setPoints(200);
        updateReq.setProblems(List.of(req1, req3));

        ContestDto result = contestService.updateContest(100L, updateReq);

        assertNotNull(result);
        // Verify cp2 was removed
        verify(contestProblemRepository).deleteAll(argThat((List<ContestProblem> list) ->
                list.size() == 1 && list.get(0).getProblem().getId().equals(11L)));
        // Verify cp3 was added
        verify(contestProblemRepository).saveAll(argThat((Iterable<ContestProblem> list) -> {
            List<ContestProblem> l = new ArrayList<>();
            list.forEach(l::add);
            return l.stream().anyMatch(cp -> cp.getProblem().getId().equals(12L));
        }));
    }

    // 10. Duplicate input problem IDs [10, 10, 11, 11] are safely normalized to [10, 11].
    @Test
    void testCreateAndEditWithDuplicateInputDeduplicates() {
        ContestRequest request = baseValidRequest();
        ContestProblemRequest p1a = new ContestProblemRequest();
        p1a.setProblemId(10L);
        p1a.setDisplayOrder(1);
        p1a.setPoints(100);
        ContestProblemRequest p1b = new ContestProblemRequest();
        p1b.setProblemId(10L);
        p1b.setDisplayOrder(2);
        p1b.setPoints(100);
        ContestProblemRequest p2a = new ContestProblemRequest();
        p2a.setProblemId(11L);
        p2a.setDisplayOrder(3);
        p2a.setPoints(150);
        ContestProblemRequest p2b = new ContestProblemRequest();
        p2b.setProblemId(11L);
        p2b.setDisplayOrder(4);
        p2b.setPoints(150);
        request.setProblems(List.of(p1a, p1b, p2a, p2b));

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(contestRepository.save(any(Contest.class))).thenAnswer(inv -> {
            Contest c = inv.getArgument(0);
            c.setId(200L);
            return c;
        });
        when(problemRepository.findById(10L)).thenReturn(Optional.of(twoSum));
        when(problemRepository.findById(11L)).thenReturn(Optional.of(binarySearch));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(200L)).thenReturn(List.of(
                new ContestProblem(null, twoSum, 1, 100),
                new ContestProblem(null, binarySearch, 2, 150)
        ));

        ContestDto result = contestService.createContest(request, 1L);

        // Verify only 2 ContestProblem records saved, not 4
        verify(contestProblemRepository).saveAll(argThat((Iterable<ContestProblem> list) -> {
            List<ContestProblem> l = new ArrayList<>();
            list.forEach(l::add);
            return l.size() == 2;
        }));
    }

    @Test
    void testResolveStatusExpiredContestReturnsEndedEvenIfStoredAsOngoing() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        Contest contest = new Contest("Live Coding #09", "CodeNova", "Desc", start, end, adminUser);
        contest.setId(901L);
        contest.setStatus(ContestStatus.ONGOING); // legacy/stored as ONGOING

        when(contestRepository.findById(901L)).thenReturn(Optional.of(contest));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(901L)).thenReturn(List.of());

        ContestDto dto = contestService.getContest(901L);

        assertEquals("ENDED", dto.getStatus(), "Expired contest must resolve to ENDED even if database row was ONGOING");
    }

    @Test
    void testResolveStatusActiveContestRetainsPublished() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        Contest contest = new Contest("Active Arena", "CodeNova", "Desc", start, end, adminUser);
        contest.setId(903L);
        contest.setStatus(ContestStatus.PUBLISHED);

        when(contestRepository.findById(903L)).thenReturn(Optional.of(contest));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(903L)).thenReturn(List.of());

        ContestDto dto = contestService.getContest(903L);

        assertEquals("PUBLISHED", dto.getStatus());
    }

    @Test
    void testResolveStatusExplicitDraftRemainsDraft() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        Contest contest = new Contest("Draft Arena", "CodeNova", "Desc", start, end, adminUser);
        contest.setId(904L);
        contest.setStatus(ContestStatus.DRAFT);

        when(contestRepository.findById(904L)).thenReturn(Optional.of(contest));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(904L)).thenReturn(List.of());

        ContestDto dto = contestService.getContest(904L);

        assertEquals("DRAFT", dto.getStatus());
    }

    @Test
    void testResolveStatusExplicitEndedRemainsEnded() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        Contest contest = new Contest("Ended Arena", "CodeNova", "Desc", start, end, adminUser);
        contest.setId(905L);
        contest.setStatus(ContestStatus.ENDED);

        when(contestRepository.findById(905L)).thenReturn(Optional.of(contest));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(905L)).thenReturn(List.of());

        ContestDto dto = contestService.getContest(905L);

        assertEquals("ENDED", dto.getStatus());
    }
}

