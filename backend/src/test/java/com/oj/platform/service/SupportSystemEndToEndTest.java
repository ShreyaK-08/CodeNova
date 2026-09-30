package com.oj.platform.service;

import com.oj.platform.dto.*;
import com.oj.platform.entity.*;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SupportSystemEndToEndTest {

    @Autowired
    private SupportFeedbackService supportFeedbackService;

    @Autowired
    private SupportRequestRepository supportRequestRepository;

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @Autowired
    private UserRepository userRepository;

    private User candidateUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        candidateUser = userRepository.findByUsername("test_candidate_support")
                .orElseGet(() -> {
                    User u = new User("Support Candidate", "test_candidate_support", "cand_support@codenova.com", "pass123", Role.ROLE_USER);
                    return userRepository.save(u);
                });

        adminUser = userRepository.findByUsername("test_admin_support")
                .orElseGet(() -> {
                    User u = new User("Support Admin", "test_admin_support", "admin_support@codenova.com", "pass123", Role.ROLE_ADMIN);
                    return userRepository.save(u);
                });
    }

    @Test
    void testCompleteSupportTicketLifecycle() {
        // 1. Create a support ticket
        CreateSupportRequest req = new CreateSupportRequest();
        req.setCategory("Assessments");
        req.setSubject("Assessment timer disconnected");
        req.setDescription("My connection dropped during Question 5 and the timer jumped 10 minutes.");
        req.setPriority("HIGH");
        req.setAssessmentId(101L);
        req.setAssessmentTitle("Full Stack Mock Assessment");

        SupportRequestDto created = supportFeedbackService.createSupportRequest(req, candidateUser.getId());
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getTicketNumber().startsWith("CN-SUP-"));
        assertEquals("OPEN", created.getStatus());
        assertEquals("HIGH", created.getPriority());
        assertEquals("Assessments", created.getCategory());
        assertEquals(101L, created.getAssessmentId());

        // 2. Candidate adds another message
        CreateSupportMessageRequest msgReq1 = new CreateSupportMessageRequest();
        msgReq1.setMessage("I took a screenshot of the timer discrepancy.");
        SupportMessageDto userMsg = supportFeedbackService.addSupportMessage(created.getId(), msgReq1, candidateUser.getId(), false);
        assertNotNull(userMsg);
        assertEquals("USER", userMsg.getSenderRole());
        assertFalse(userMsg.isInternalNote());

        // 3. Admin adds an Internal Note
        CreateSupportMessageRequest internalNote = new CreateSupportMessageRequest();
        internalNote.setMessage("Checked database logs: user had 1 retry attempt remaining. We can restore 10 min.");
        internalNote.setInternalNote(true);
        SupportMessageDto adminNote = supportFeedbackService.addSupportMessage(created.getId(), internalNote, adminUser.getId(), true);
        assertNotNull(adminNote);
        assertTrue(adminNote.isInternalNote());

        // 4. Candidate views ticket -> Internal Note MUST BE STRIPPED
        SupportRequestDto candidateView = supportFeedbackService.getSupportRequestById(created.getId(), candidateUser.getId(), false);
        assertNotNull(candidateView);
        assertNotNull(candidateView.getMessages());
        // Should contain initial description + user follow-up (internal note omitted)
        for (SupportMessageDto m : candidateView.getMessages()) {
            assertFalse(m.isInternalNote(), "Candidate should NEVER see internal notes!");
        }

        // 5. Admin views ticket -> Internal Note MUST BE INCLUDED
        SupportRequestDto adminView = supportFeedbackService.getSupportRequestById(created.getId(), adminUser.getId(), true);
        assertNotNull(adminView);
        boolean hasInternal = adminView.getMessages().stream().anyMatch(SupportMessageDto::isInternalNote);
        assertTrue(hasInternal, "Admin MUST see internal notes.");

        // 6. Admin triage and public response
        UpdateSupportTicketRequest triage = new UpdateSupportTicketRequest();
        triage.setStatus("IN_PROGRESS");
        triage.setPriority("CRITICAL");
        triage.setAssignedToUserId(adminUser.getId());
        SupportRequestDto triaged = supportFeedbackService.adminTriageTicket(created.getId(), triage, adminUser.getId());
        assertEquals("IN_PROGRESS", triaged.getStatus());
        assertEquals("CRITICAL", triaged.getPriority());
        assertEquals(adminUser.getId(), triaged.getAssignedToUserId());

        // 7. Admin AI Draft generator
        AiSupportDraftRequest aiReq = new AiSupportDraftRequest();
        aiReq.setTone("EMPATHETIC");
        AiSupportDraftResponse aiDraft = supportFeedbackService.generateAiSupportDraft(created.getId(), aiReq, adminUser.getId());
        assertNotNull(aiDraft);
        assertNotNull(aiDraft.getDraftReply());
        assertTrue(aiDraft.getDraftReply().length() > 20);

        // 8. Admin sends public reply
        CreateSupportMessageRequest publicReply = new CreateSupportMessageRequest();
        publicReply.setMessage("Hello! We have reviewed your assessment attempt logs and restored your timer window.");
        publicReply.setInternalNote(false);
        supportFeedbackService.addSupportMessage(created.getId(), publicReply, adminUser.getId(), true);

        // 9. Mark ticket as resolved
        SupportRequestDto resolved = supportFeedbackService.resolveSupportTicket(created.getId(), candidateUser.getId(), false);
        assertEquals("RESOLVED", resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());

        // 10. Candidate submits 5-star rating
        SupportRatingRequest ratingReq = new SupportRatingRequest();
        ratingReq.setRating(5);
        ratingReq.setComments("Problem was resolved in minutes. Excellent service!");
        SupportRequestDto rated = supportFeedbackService.submitSupportRating(created.getId(), ratingReq, candidateUser.getId());
        assertEquals(5, rated.getRating());
        assertEquals("Problem was resolved in minutes. Excellent service!", rated.getRatingComments());
        assertNotNull(rated.getRatedAt());

        // 11. Support Analytics
        SupportAnalyticsDto analytics = supportFeedbackService.getSupportAnalytics();
        assertNotNull(analytics);
        assertTrue(analytics.getTotalTickets() >= 1);
        assertTrue(analytics.getAverageSatisfactionRating() >= 1.0);
    }
}
