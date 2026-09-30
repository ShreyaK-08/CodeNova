package com.oj.platform.service;

import com.oj.platform.dto.AiChatRequest;
import com.oj.platform.dto.AiChatResponse;
import com.oj.platform.dto.UserDashboardStatsDto;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.repository.AssessmentAttemptRepository;
import com.oj.platform.repository.ContestAttemptRepository;
import com.oj.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class AiServiceTest {

    private AiServiceImpl aiService;
    private DashboardService dashboardService;
    private AssessmentAttemptRepository assessmentAttemptRepository;
    private ContestAttemptRepository contestAttemptRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        dashboardService = Mockito.mock(DashboardService.class);
        assessmentAttemptRepository = Mockito.mock(AssessmentAttemptRepository.class);
        contestAttemptRepository = Mockito.mock(ContestAttemptRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        // Setup mock user
        User user = new User();
        user.setId(10L);
        user.setUsername("student_rocky");
        user.setName("Rocky");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // Setup mock dashboard stats (Easy: 9, Medium: 8, Hard: 3, Total: 20, Subs: 32, Accepted: 20, Rate: 62.5%)
        UserDashboardStatsDto stats = new UserDashboardStatsDto();
        stats.setTotalProblems(50);
        stats.setTotalProblemsSolved(20);
        stats.setEasySolved(9);
        stats.setMediumSolved(8);
        stats.setHardSolved(3);
        stats.setTotalSubmissions(32);
        stats.setAcceptedSubmissions(20);
        stats.setAcceptanceRate(62.5);
        stats.setCurrentStreakDays(5);
        stats.setCurrentRank("Gold");
        when(dashboardService.getUserDashboardStats(10L)).thenReturn(stats);

        // Setup mock assessment attempts
        Assessment assessment1 = new Assessment();
        assessment1.setTitle("Java & OOP Fundamentals");
        assessment1.setTotalMarks(100);

        AssessmentAttempt attempt1 = new AssessmentAttempt();
        attempt1.setAssessment(assessment1);
        attempt1.setScore(60);
        attempt1.setStatus(AssessmentAttemptStatus.COMPLETED);
        attempt1.setCompletedAt(LocalDateTime.now().minusDays(1));

        Assessment assessment2 = new Assessment();
        assessment2.setTitle("Data Structures");
        assessment2.setTotalMarks(100);

        AssessmentAttempt attempt2 = new AssessmentAttempt();
        attempt2.setAssessment(assessment2);
        attempt2.setScore(80);
        attempt2.setStatus(AssessmentAttemptStatus.COMPLETED);
        attempt2.setCompletedAt(LocalDateTime.now());

        when(assessmentAttemptRepository.findByUserIdWithAssessment(10L))
                .thenReturn(List.of(attempt1, attempt2));

        // Setup mock contest attempt
        Contest contest = new Contest();
        contest.setTitle("Weekly Challenge #1");

        ContestAttempt contestAttempt = new ContestAttempt();
        contestAttempt.setContest(contest);
        contestAttempt.setScore(300);
        contestAttempt.setProblemsSolved(3);
        contestAttempt.setSubmissionCount(5);
        contestAttempt.setStatus(ContestAttemptStatus.COMPLETED);

        when(contestAttemptRepository.findByParticipantIdWithContest(10L))
                .thenReturn(List.of(contestAttempt));

        aiService = new AiServiceImpl(dashboardService, assessmentAttemptRepository, contestAttemptRepository, userRepository);
    }

    @Test
    void testEmptyMessage_ThrowsBadRequestException() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("   ");

        assertThrows(BadRequestException.class, () -> aiService.chat(request, 1L, "user1"));
    }

    @Test
    void testContestTabHiddenQuestion_ExplainsSecurityAndNoWebcamHonesty() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("What happens when my contest tab becomes hidden?");
        request.setLanguage("en");

        AiChatResponse response = aiService.chat(request, 2L, "user2");

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertTrue(response.getReply().contains("violation"));
        assertTrue(response.getReply().contains("3"));
        assertTrue(response.getReply().contains("does NOT") || response.getReply().contains("webcam"));
    }

    @Test
    void testKannadaResponse_ReturnsKannadaText() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("ವೇದಿಕೆಯ ಬಗ್ಗೆ ತಿಳಿಸಿ");
        request.setLanguage("kn");

        AiChatResponse response = aiService.chat(request, 3L, "user3");

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertTrue(response.getReply().contains("CodeNova"));
    }

    @Test
    void testHindiResponse_ReturnsHindiText() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("प्लेटफ़ॉर्म पर क्या कर सकते हैं?");
        request.setLanguage("hi");

        AiChatResponse response = aiService.chat(request, 4L, "user4");

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertTrue(response.getReply().contains("CodeNova"));
    }

    @Test
    void testRateLimiter_ExceedingLimitThrowsBadRequest() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("Hello assistant");
        request.setLanguage("en");

        Long userId = 999L;
        // Make 20 calls (allowed limit)
        for (int i = 0; i < 20; i++) {
            aiService.chat(request, userId, "ratelimited_user");
        }

        // 21st call must throw BadRequestException
        assertThrows(BadRequestException.class, () -> aiService.chat(request, userId, "ratelimited_user"));
    }

    // =========================================================================
    // TASK 15 TESTS: REAL USER DATA IN PROGRESS RESPONSES
    // =========================================================================

    @Test
    void testMyProgress_ReturnsRealMetrics_AndDoesNotSayVisitProgressSection() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("my progress");
        request.setLanguage("en");

        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        assertNotNull(response);
        String reply = response.getReply();
        assertNotNull(reply);

        // Must NOT say "Please visit the progress section"
        assertFalse(reply.contains("Please visit the progress section"));

        // Must contain the actual database metrics
        assertTrue(reply.contains("Easy: 9") || reply.contains("Easy: 9"));
        assertTrue(reply.contains("Medium: 8"));
        assertTrue(reply.contains("Hard: 3"));
        assertTrue(reply.contains("Total Solved: 20"));
        assertTrue(reply.contains("Total: 32"));
        assertTrue(reply.contains("Accepted: 20"));
        assertTrue(reply.contains("62.5%"));
        assertTrue(reply.contains("Completed: 2"));
        assertTrue(reply.contains("Participated: 1"));
    }

    @Test
    void testDirectNumericalQuestions_EasyMediumHard() {
        // 1. Easy questions
        AiChatRequest reqEasy = new AiChatRequest();
        reqEasy.setMessage("How many easy problems have I solved?");
        AiChatResponse resEasy = aiService.chat(reqEasy, 10L, "student_rocky");
        assertTrue(resEasy.getReply().contains("You have solved 9 Easy problems."));

        // 2. Medium questions
        AiChatRequest reqMed = new AiChatRequest();
        reqMed.setMessage("How many medium problems have I solved?");
        AiChatResponse resMed = aiService.chat(reqMed, 10L, "student_rocky");
        assertTrue(resMed.getReply().contains("You have solved 8 Medium problems."));

        // 3. Hard questions
        AiChatRequest reqHard = new AiChatRequest();
        reqHard.setMessage("How many hard problems have I solved?");
        AiChatResponse resHard = aiService.chat(reqHard, 10L, "student_rocky");
        assertTrue(resHard.getReply().contains("You have solved 3 Hard problems."));
    }

    @Test
    void testTotalProblemsSolvedQuestion() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("How many problems have I solved?");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("You have solved 20 problems in total:"));
        assertTrue(reply.contains("Easy: 9"));
        assertTrue(reply.contains("Medium: 8"));
        assertTrue(reply.contains("Hard: 3"));
    }

    @Test
    void testAcceptanceRateQuestion() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("what is my acceptance rate?");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("62.5%"));
        assertTrue(reply.contains("20 accepted submissions"));
        assertTrue(reply.contains("32 total submissions"));
    }

    @Test
    void testShowMySubmissions() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("show my submissions");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("Total Submissions: 32"));
        assertTrue(reply.contains("Accepted Submissions: 20"));
        assertTrue(reply.contains("Acceptance Rate: 62.5%"));
    }

    @Test
    void testShowMyAssessmentResults() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("show my assessment results");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("Java & OOP Fundamentals — 60.0%"));
        assertTrue(reply.contains("Data Structures — 80.0%"));
        assertTrue(reply.contains("Total completed: 2"));
        assertTrue(reply.contains("Average: 70.0%"));
    }

    @Test
    void testMyContestProgress() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("my contest progress");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("Contests participated: 1"));
        assertTrue(reply.contains("Problems solved: 3"));
        assertTrue(reply.contains("Total submissions: 5"));
        assertTrue(reply.contains("Score: 300"));
    }

    @Test
    void testKannadaProgressQuestion() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("ನನ್ನ ಪ್ರಗತಿ");
        request.setLanguage("kn");

        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("ಸುಲಭ (Easy): 9"));
        assertTrue(reply.contains("ಮಧ್ಯಮ (Medium): 8"));
        assertTrue(reply.contains("ಕಠಿಣ (Hard): 3"));
        assertTrue(reply.contains("ಒಟ್ಟು ಪರಿಹರಿಸಲಾಗಿದೆ: 20"));
        assertTrue(reply.contains("62.5%"));
    }

    @Test
    void testHindiProgressQuestion() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("मेरी प्रगति");
        request.setLanguage("hi");

        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("आसान (Easy): 9"));
        assertTrue(reply.contains("मध्यम (Medium): 8"));
        assertTrue(reply.contains("कठिन (Hard): 3"));
        assertTrue(reply.contains("कुल हल की गईं: 20"));
        assertTrue(reply.contains("62.5%"));
    }

    @Test
    void testImprovementQuery() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("what should I improve?");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("Improvement Recommendations"));
    }

    @Test
    void testLatestAssessmentScore() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("what is my latest assessment score?");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        assertNotNull(response);
        String reply = response.getReply();
        assertNotNull(reply);
        assertTrue(reply.contains("Latest Assessment Score"));
        // attempt1 is the latest because attempts are sorted descending
        assertTrue(reply.contains("Score"));
        assertTrue(reply.contains("COMPLETED"));
    }

    @Test
    void testShowMyContestPerformance() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("show my contest performance");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        assertNotNull(response);
        String reply = response.getReply();
        assertNotNull(reply);
        assertTrue(reply.contains("Contest Performance"));
        assertTrue(reply.contains("Contests participated: 1"));
        assertTrue(reply.contains("Problems solved: 3"));
        assertTrue(reply.contains("Score: 300"));
        assertTrue(reply.contains("Weekly Challenge #1"));
    }

    @Test
    void testUserIsolation_UserWithNoDataReturnsCleanEmptyMessage() {
        Long isolatedUserId = 99L;
        User user99 = new User();
        user99.setId(isolatedUserId);
        user99.setUsername("new_student");
        when(userRepository.findById(isolatedUserId)).thenReturn(Optional.of(user99));

        UserDashboardStatsDto emptyStats = new UserDashboardStatsDto();
        emptyStats.setTotalProblems(50);
        emptyStats.setTotalProblemsSolved(0);
        emptyStats.setEasySolved(0);
        emptyStats.setMediumSolved(0);
        emptyStats.setHardSolved(0);
        emptyStats.setTotalSubmissions(0);
        emptyStats.setAcceptedSubmissions(0);
        emptyStats.setAcceptanceRate(0.0);
        when(dashboardService.getUserDashboardStats(isolatedUserId)).thenReturn(emptyStats);
        when(assessmentAttemptRepository.findByUserIdWithAssessment(isolatedUserId)).thenReturn(Collections.emptyList());
        when(contestAttemptRepository.findByParticipantIdWithContest(isolatedUserId)).thenReturn(Collections.emptyList());

        AiChatRequest request = new AiChatRequest();
        request.setMessage("my progress");
        AiChatResponse response = aiService.chat(request, isolatedUserId, "new_student");

        String reply = response.getReply();
        assertNotNull(reply);
        // User 99 should NOT see Rocky's numbers (9 easy, 8 medium, etc.)
        assertFalse(reply.contains("Easy: 9"));
        assertFalse(reply.contains("Medium: 8"));
        assertTrue(reply.contains("empty") || reply.contains("Start solving problems"));
    }

    @Test
    void testNoHardcodedValues_DynamicStatsReflectedAccurately() {
        Long dynamicUserId = 42L;
        User user42 = new User();
        user42.setId(dynamicUserId);
        user42.setUsername("dynamic_coder");
        when(userRepository.findById(dynamicUserId)).thenReturn(Optional.of(user42));

        UserDashboardStatsDto dynamicStats = new UserDashboardStatsDto();
        dynamicStats.setTotalProblems(100);
        dynamicStats.setTotalProblemsSolved(45);
        dynamicStats.setEasySolved(30);
        dynamicStats.setMediumSolved(10);
        dynamicStats.setHardSolved(5);
        dynamicStats.setTotalSubmissions(75);
        dynamicStats.setAcceptedSubmissions(50);
        dynamicStats.setAcceptanceRate(66.7);
        when(dashboardService.getUserDashboardStats(dynamicUserId)).thenReturn(dynamicStats);

        AiChatRequest reqEasy = new AiChatRequest();
        reqEasy.setMessage("how many easy problems have I solved?");
        AiChatResponse resEasy = aiService.chat(reqEasy, dynamicUserId, "dynamic_coder");
        assertTrue(resEasy.getReply().contains("You have solved 30 Easy problems."));

        AiChatRequest reqMed = new AiChatRequest();
        reqMed.setMessage("how many medium problems have I solved?");
        AiChatResponse resMed = aiService.chat(reqMed, dynamicUserId, "dynamic_coder");
        assertTrue(resMed.getReply().contains("You have solved 10 Medium problems."));

        AiChatRequest reqHard = new AiChatRequest();
        reqHard.setMessage("how many hard problems have I solved?");
        AiChatResponse resHard = aiService.chat(reqHard, dynamicUserId, "dynamic_coder");
        assertTrue(resHard.getReply().contains("You have solved 5 Hard problems."));
    }

    @Test
    void testLoggedOutUser_PromptsToLogin() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("my progress");
        AiChatResponse response = aiService.chat(request, null, null);

        String reply = response.getReply();
        assertTrue(reply.contains("Please log in to your CodeNova account"));
    }

    @Test
    void testWhatIsCodeNova_Preserved() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("What is CodeNova?");
        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("What You Can Do on CodeNova"));
    }

    @Test
    void testExplainThisCode_Preserved() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("explain this code");
        request.setCode("public class Solution { public int add(int a, int b) { return a + b; } }");
        request.setProgrammingLanguage("JAVA");

        AiChatResponse response = aiService.chat(request, 10L, "student_rocky");

        String reply = response.getReply();
        assertTrue(reply.contains("Code Explanation"));
    }
}
