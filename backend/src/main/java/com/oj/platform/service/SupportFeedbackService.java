package com.oj.platform.service;

import com.oj.platform.dto.*;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.exception.ForbiddenException;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupportFeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(SupportFeedbackService.class);
    private static final Path UPLOAD_DIR = Paths.get("uploads", "support").toAbsolutePath().normalize();
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "pdf", "txt");

    private final SupportRequestRepository supportRequestRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final AssessmentQuestionFeedbackRepository assessmentQuestionFeedbackRepository;
    private final AssessmentFeedbackRepository assessmentFeedbackRepository;
    private final GeneralFeedbackRepository generalFeedbackRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final EmailService emailService;
    private final AiService aiService;

    @Autowired
    public SupportFeedbackService(SupportRequestRepository supportRequestRepository,
                                  SupportMessageRepository supportMessageRepository,
                                  AssessmentQuestionFeedbackRepository assessmentQuestionFeedbackRepository,
                                  AssessmentFeedbackRepository assessmentFeedbackRepository,
                                  GeneralFeedbackRepository generalFeedbackRepository,
                                  UserRepository userRepository,
                                  AssessmentRepository assessmentRepository,
                                  AssessmentQuestionRepository assessmentQuestionRepository,
                                  AssessmentAttemptRepository assessmentAttemptRepository,
                                  @Autowired(required = false) EmailService emailService,
                                  @Autowired(required = false) AiService aiService) {
        this.supportRequestRepository = supportRequestRepository;
        this.supportMessageRepository = supportMessageRepository;
        this.assessmentQuestionFeedbackRepository = assessmentQuestionFeedbackRepository;
        this.assessmentFeedbackRepository = assessmentFeedbackRepository;
        this.generalFeedbackRepository = generalFeedbackRepository;
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.emailService = emailService;
        this.aiService = aiService;

        try {
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException e) {
            logger.error("Could not initialize support uploads directory: {}", e.getMessage());
        }
    }

    // ── Support Ticket Management ─────────────────────────────────────────

    public SupportRequestDto createSupportRequest(CreateSupportRequest req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        SupportRequest entity = new SupportRequest();
        entity.setUser(user);
        entity.setCategory(req.getCategory());
        entity.setSubject(req.getSubject());
        entity.setDescription(req.getDescription());
        entity.setPriority(req.getPriority() != null && !req.getPriority().isBlank() ? req.getPriority().toUpperCase() : "MEDIUM");
        entity.setStatus("OPEN");

        // Context References
        entity.setAssessmentId(req.getAssessmentId());
        entity.setAssessmentTitle(req.getAssessmentTitle());
        entity.setContestId(req.getContestId());
        entity.setContestTitle(req.getContestTitle());
        entity.setProblemId(req.getProblemId());
        entity.setProblemTitle(req.getProblemTitle());
        entity.setSubmissionId(req.getSubmissionId());

        // Attachment Details
        entity.setAttachmentUrl(req.getAttachmentUrl());
        entity.setAttachmentName(req.getAttachmentName());
        entity.setAttachmentType(req.getAttachmentType());
        entity.setAttachmentSize(req.getAttachmentSize());

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        // Temporary ticket number to satisfy NOT NULL before save
        entity.setTicketNumber("CN-SUP-PENDING");

        SupportRequest saved = supportRequestRepository.saveAndFlush(entity);
        String finalTicket = String.format("CN-SUP-%06d", saved.getId());
        saved.setTicketNumber(finalTicket);
        saved = supportRequestRepository.save(saved);

        // Also add the initial description as the opening support message
        SupportMessage initialMsg = new SupportMessage(saved, user, "USER", req.getDescription(), false);
        initialMsg.setAttachmentUrl(req.getAttachmentUrl());
        initialMsg.setAttachmentName(req.getAttachmentName());
        initialMsg.setAttachmentType(req.getAttachmentType());
        initialMsg.setAttachmentSize(req.getAttachmentSize());
        supportMessageRepository.save(initialMsg);

        // Optional confirmation email
        if (emailService != null && user.getEmail() != null) {
            final User targetUser = user;
            final SupportRequest targetReq = saved;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendSupportTicketCreatedEmail(targetUser, targetReq);
                } catch (Exception e) {
                    logger.warn("Failed to dispatch ticket created email to {}: {}", targetUser.getEmail(), e.getMessage());
                }
            });
        }

        return mapToSupportRequestDto(saved, false);
    }

    @Transactional(readOnly = true)
    public List<SupportRequestDto> getMySupportRequests(Long userId, String status, String category, String search) {
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) ? status.trim() : null;
        String cleanCat = (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) ? category.trim() : null;
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        List<SupportRequest> requests = supportRequestRepository.searchUserRequests(userId, cleanStatus, cleanCat, cleanSearch);
        return requests.stream()
                .map(r -> mapToSupportRequestDto(r, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserSupportSummaryDto getUserSupportSummary(Long userId) {
        long total = supportRequestRepository.countByUserId(userId);
        long open = supportRequestRepository.countByUserIdAndStatus(userId, "OPEN");
        long inProgress = supportRequestRepository.countByUserIdAndStatus(userId, "IN_PROGRESS");
        long waiting = supportRequestRepository.countByUserIdAndStatus(userId, "WAITING_FOR_USER");
        long resolved = supportRequestRepository.countByUserIdAndStatus(userId, "RESOLVED");
        long closed = supportRequestRepository.countByUserIdAndStatus(userId, "CLOSED");

        return new UserSupportSummaryDto(total, open, inProgress, waiting, resolved, closed);
    }

    @Transactional(readOnly = true)
    public SupportRequestDto getSupportRequestById(Long id, Long currentUserId, boolean isAdminOrStaff) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + id));

        if (!isAdminOrStaff && !request.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to view this support ticket.");
        }

        return mapToSupportRequestDto(request, true, isAdminOrStaff);
    }

    public SupportMessageDto addSupportMessage(Long ticketId, CreateSupportMessageRequest req, Long currentUserId, boolean isAdminOrStaff) {
        SupportRequest request = supportRequestRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (!isAdminOrStaff && !request.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to reply to this support ticket.");
        }

        boolean isInternal = isAdminOrStaff && req.isInternalNote();
        String role = isAdminOrStaff ? (sender.getRole() != null ? sender.getRole().name() : "ADMIN") : "USER";

        SupportMessage message = new SupportMessage(request, sender, role, req.getMessage().trim(), isInternal);
        message.setAttachmentUrl(req.getAttachmentUrl());
        message.setAttachmentName(req.getAttachmentName());
        message.setAttachmentType(req.getAttachmentType());
        message.setAttachmentSize(req.getAttachmentSize());
        message.setCreatedAt(LocalDateTime.now());

        SupportMessage saved = supportMessageRepository.save(message);

        // Update ticket lifecycle if applicable
        request.setUpdatedAt(LocalDateTime.now());
        if (isAdminOrStaff && !isInternal) {
            // Admin replied publicly -> update status to WAITING_FOR_USER if currently OPEN or IN_PROGRESS
            if ("OPEN".equals(request.getStatus())) {
                request.setStatus("WAITING_FOR_USER");
            }
            // Send email to ticket author
            if (emailService != null && request.getUser() != null && request.getUser().getEmail() != null) {
                final User targetUser = request.getUser();
                final SupportRequest targetReq = request;
                final SupportMessage targetMsg = saved;
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendSupportAdminReplyEmail(targetUser, targetReq, targetMsg);
                    } catch (Exception e) {
                        logger.warn("Could not dispatch admin reply email to {}: {}", targetUser.getEmail(), e.getMessage());
                    }
                });
            }
        } else if (!isAdminOrStaff) {
            // User replied -> if ticket was WAITING_FOR_USER, RESOLVED, or CLOSED, bring back to IN_PROGRESS or OPEN
            if ("WAITING_FOR_USER".equals(request.getStatus())) {
                request.setStatus("IN_PROGRESS");
            } else if ("RESOLVED".equals(request.getStatus()) || "CLOSED".equals(request.getStatus())) {
                request.setStatus("IN_PROGRESS");
                request.setResolvedAt(null);
                request.setClosedAt(null);
            }
        }

        supportRequestRepository.save(request);
        return mapToSupportMessageDto(saved);
    }

    public SupportRequestDto resolveSupportTicket(Long ticketId, Long currentUserId, boolean isAdmin) {
        SupportRequest request = supportRequestRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));

        if (!isAdmin && !request.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to resolve this ticket.");
        }

        String oldStatus = request.getStatus();
        request.setStatus("RESOLVED");
        request.setResolvedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        SupportRequest saved = supportRequestRepository.save(request);

        // Log system/audit message in conversation
        User actor = userRepository.findById(currentUserId).orElse(request.getUser());
        SupportMessage note = new SupportMessage(saved, actor, isAdmin ? "ADMIN" : "USER",
                "Ticket was marked as RESOLVED by " + (isAdmin ? "Support Staff" : "User") + ".", false);
        supportMessageRepository.save(note);

        if (emailService != null && request.getUser() != null && request.getUser().getEmail() != null) {
            final User targetUser = request.getUser();
            final SupportRequest targetReq = saved;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendSupportTicketResolvedEmail(targetUser, targetReq);
                } catch (Exception e) {
                    logger.warn("Failed to dispatch ticket resolved email: {}", e.getMessage());
                }
            });
        }

        return mapToSupportRequestDto(saved, true, isAdmin);
    }

    public SupportRequestDto reopenSupportTicket(Long ticketId, Long currentUserId, boolean isAdmin) {
        SupportRequest request = supportRequestRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));

        if (!isAdmin && !request.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to reopen this ticket.");
        }

        request.setStatus("OPEN");
        request.setResolvedAt(null);
        request.setClosedAt(null);
        request.setUpdatedAt(LocalDateTime.now());
        SupportRequest saved = supportRequestRepository.save(request);

        User actor = userRepository.findById(currentUserId).orElse(request.getUser());
        SupportMessage note = new SupportMessage(saved, actor, isAdmin ? "ADMIN" : "USER",
                "Ticket was REOPENED.", false);
        supportMessageRepository.save(note);

        if (emailService != null && request.getUser() != null && request.getUser().getEmail() != null) {
            final User targetUser = request.getUser();
            final SupportRequest targetReq = saved;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendSupportTicketReopenedEmail(targetUser, targetReq);
                } catch (Exception e) {
                    logger.warn("Failed to dispatch ticket reopened email: {}", e.getMessage());
                }
            });
        }

        return mapToSupportRequestDto(saved, true, isAdmin);
    }

    public SupportRequestDto closeSupportTicket(Long ticketId, Long currentUserId, boolean isAdmin) {
        SupportRequest request = supportRequestRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));

        if (!isAdmin && !request.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to close this ticket.");
        }

        request.setStatus("CLOSED");
        request.setClosedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        SupportRequest saved = supportRequestRepository.save(request);

        User actor = userRepository.findById(currentUserId).orElse(request.getUser());
        SupportMessage note = new SupportMessage(saved, actor, isAdmin ? "ADMIN" : "USER",
                "Ticket was CLOSED.", false);
        supportMessageRepository.save(note);

        if (emailService != null && request.getUser() != null && request.getUser().getEmail() != null) {
            final User targetUser = request.getUser();
            final SupportRequest targetReq = saved;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendSupportTicketClosedEmail(targetUser, targetReq);
                } catch (Exception e) {
                    logger.warn("Failed to dispatch ticket closed email: {}", e.getMessage());
                }
            });
        }

        return mapToSupportRequestDto(saved, true, isAdmin);
    }

    public SupportRequestDto submitSupportRating(Long ticketId, SupportRatingRequest ratingReq, Long currentUserId) {
        SupportRequest request = supportRequestRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));

        if (!request.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You can only rate support on your own tickets.");
        }

        if (!"RESOLVED".equals(request.getStatus()) && !"CLOSED".equals(request.getStatus())) {
            throw new BadRequestException("You can only submit a rating once the support ticket is resolved or closed.");
        }

        request.setRating(ratingReq.getRating());
        request.setRatingComments(ratingReq.getComments());
        request.setRatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        SupportRequest saved = supportRequestRepository.save(request);
        return mapToSupportRequestDto(saved, false);
    }

    // ── Admin Support Portal Operations ───────────────────────────────────

    @Transactional(readOnly = true)
    public List<SupportRequestDto> getAdminSupportRequests(String status, String priority, String category, Long assignedToId, String search) {
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) ? status.trim() : null;
        String cleanPriority = (priority != null && !priority.isBlank() && !priority.equalsIgnoreCase("ALL")) ? priority.trim() : null;
        String cleanCat = (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) ? category.trim() : null;
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        List<SupportRequest> list = supportRequestRepository.searchAdminRequests(cleanStatus, cleanPriority, cleanCat, assignedToId, cleanSearch);
        return list.stream()
                .map(r -> mapToSupportRequestDto(r, false))
                .collect(Collectors.toList());
    }

    public SupportRequestDto adminTriageTicket(Long ticketId, UpdateSupportTicketRequest triageReq, Long adminUserId) {
        SupportRequest request = supportRequestRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));

        String oldStatus = request.getStatus();

        if (triageReq.getStatus() != null && !triageReq.getStatus().isBlank()) {
            String newStatus = triageReq.getStatus().toUpperCase();
            request.setStatus(newStatus);
            if ("RESOLVED".equals(newStatus) && request.getResolvedAt() == null) {
                request.setResolvedAt(LocalDateTime.now());
            } else if ("CLOSED".equals(newStatus) && request.getClosedAt() == null) {
                request.setClosedAt(LocalDateTime.now());
            }

            if (!oldStatus.equalsIgnoreCase(newStatus) && emailService != null && request.getUser() != null && request.getUser().getEmail() != null) {
                final User targetUser = request.getUser();
                final SupportRequest targetReq = request;
                final String finalOldStatus = oldStatus;
                final String finalNewStatus = newStatus;
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendSupportStatusChangedEmail(targetUser, targetReq, finalOldStatus, finalNewStatus);
                    } catch (Exception e) {
                        logger.warn("Could not dispatch status change email: {}", e.getMessage());
                    }
                });
            }
        }

        if (triageReq.getPriority() != null && !triageReq.getPriority().isBlank()) {
            request.setPriority(triageReq.getPriority().toUpperCase());
        }

        if (triageReq.getCategory() != null && !triageReq.getCategory().isBlank()) {
            request.setCategory(triageReq.getCategory());
        }

        if (triageReq.getAssignedToUserId() != null) {
            if (triageReq.getAssignedToUserId() == 0) {
                request.setAssignedTo(null);
            } else {
                User assignee = userRepository.findById(triageReq.getAssignedToUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Staff member not found: " + triageReq.getAssignedToUserId()));
                request.setAssignedTo(assignee);
            }
        }

        request.setUpdatedAt(LocalDateTime.now());
        SupportRequest saved = supportRequestRepository.save(request);

        return mapToSupportRequestDto(saved, true, true);
    }

    @Transactional(readOnly = true)
    public List<StaffMemberDto> getStaffMembers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_ADMIN)
                .map(u -> new StaffMemberDto(u.getId(), u.getUsername(), u.getName(), u.getEmail(), u.getRole().name()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupportAnalyticsDto getSupportAnalytics() {
        SupportAnalyticsDto dto = new SupportAnalyticsDto();

        dto.setTotalTickets(supportRequestRepository.count());
        dto.setOpenTickets(supportRequestRepository.countByStatus("OPEN"));
        dto.setInProgressTickets(supportRequestRepository.countByStatus("IN_PROGRESS"));
        dto.setWaitingForUserTickets(supportRequestRepository.countByStatus("WAITING_FOR_USER"));
        dto.setResolvedTickets(supportRequestRepository.countByStatus("RESOLVED"));
        dto.setClosedTickets(supportRequestRepository.countByStatus("CLOSED"));

        dto.setHighPriorityCount(supportRequestRepository.countByPriority("HIGH"));
        dto.setCriticalPriorityCount(supportRequestRepository.countByPriority("CRITICAL"));
        dto.setMediumPriorityCount(supportRequestRepository.countByPriority("MEDIUM"));
        dto.setLowPriorityCount(supportRequestRepository.countByPriority("LOW"));

        Double avgRating = supportRequestRepository.calculateAverageRating();
        dto.setAverageSatisfactionRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue() : 5.0);
        dto.setTotalRatedTickets(supportRequestRepository.countRatedTickets());

        // Calculate average resolution time for resolved tickets
        List<SupportRequest> all = supportRequestRepository.findAll();
        List<Double> resolutionHoursList = new ArrayList<>();
        long assignedCount = 0;
        for (SupportRequest r : all) {
            if (r.getAssignedTo() != null) {
                assignedCount++;
            }
            if (r.getResolvedAt() != null && r.getCreatedAt() != null) {
                Duration duration = Duration.between(r.getCreatedAt(), r.getResolvedAt());
                resolutionHoursList.add(Math.max(0.1, duration.toMinutes() / 60.0));
            }
        }
        dto.setAssignedTickets(assignedCount);

        double avgResolution = resolutionHoursList.isEmpty() ? 0.0 :
                resolutionHoursList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        dto.setAverageResolutionTimeHours(BigDecimal.valueOf(avgResolution).setScale(1, RoundingMode.HALF_UP).doubleValue());

        // Category breakdown
        Map<String, Long> categoryMap = new HashMap<>();
        for (Object[] row : supportRequestRepository.countByCategory()) {
            categoryMap.put(String.valueOf(row[0]), (Long) row[1]);
        }
        dto.setTicketsByCategory(categoryMap);

        // Priority breakdown
        Map<String, Long> priorityMap = new HashMap<>();
        for (Object[] row : supportRequestRepository.countByPriorityGroup()) {
            priorityMap.put(String.valueOf(row[0]), (Long) row[1]);
        }
        dto.setTicketsByPriority(priorityMap);

        // Status breakdown
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : supportRequestRepository.countByStatusGroup()) {
            statusMap.put(String.valueOf(row[0]), (Long) row[1]);
        }
        dto.setTicketsByStatus(statusMap);

        // Rating distribution
        Map<Integer, Long> ratingMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) ratingMap.put(i, 0L);
        for (Object[] row : supportRequestRepository.countByRatingGroup()) {
            if (row[0] != null) {
                ratingMap.put(((Number) row[0]).intValue(), (Long) row[1]);
            }
        }
        dto.setRatingDistribution(ratingMap);

        return dto;
    }

    // ── AI Support Assistant ──────────────────────────────────────────────

    public AiSupportDraftResponse generateAiSupportDraft(Long ticketId, AiSupportDraftRequest req, Long adminUserId) {
        SupportRequest request = supportRequestRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found: " + ticketId));

        List<SupportMessage> messages = supportMessageRepository.findBySupportRequestIdOrderByCreatedAtAsc(ticketId);

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("CodeNova Support Ticket Details:\n");
        contextBuilder.append("- Ticket ID: ").append(request.getTicketNumber()).append("\n");
        contextBuilder.append("- Category: ").append(request.getCategory()).append("\n");
        contextBuilder.append("- Subject: ").append(request.getSubject()).append("\n");
        contextBuilder.append("- Priority: ").append(request.getPriority()).append("\n");
        contextBuilder.append("- Requester: @").append(request.getUser().getUsername())
                .append(" (").append(request.getUser().getEmail()).append(")\n");

        if (request.getAssessmentTitle() != null) {
            contextBuilder.append("- Related Assessment: ").append(request.getAssessmentTitle())
                    .append(" (ID: ").append(request.getAssessmentId()).append(")\n");
        }
        if (request.getContestTitle() != null) {
            contextBuilder.append("- Related Contest: ").append(request.getContestTitle())
                    .append(" (ID: ").append(request.getContestId()).append(")\n");
        }
        if (request.getProblemTitle() != null) {
            contextBuilder.append("- Related Problem: ").append(request.getProblemTitle())
                    .append(" (ID: ").append(request.getProblemId()).append(")\n");
        }

        contextBuilder.append("\nConversation History:\n");
        for (SupportMessage m : messages) {
            contextBuilder.append("[").append(m.getSenderRole()).append(" - @")
                    .append(m.getSender() != null ? m.getSender().getUsername() : "user")
                    .append(m.isInternalNote() ? " (INTERNAL NOTE)" : "")
                    .append("]: ").append(m.getMessage()).append("\n");
        }

        String prompt = "You are a professional, empathetic, and knowledgeable CodeNova Support Agent.\n"
                + "Review the following customer support ticket and draft a clear, helpful, and courteous response to the user.\n"
                + "Guidelines:\n"
                + "1. Greet the user by their name or username politely.\n"
                + "2. Address their specific issue regarding the platform (assessments, compiler/judge, contests, milestones, login, etc.).\n"
                + "3. Provide clear troubleshooting steps or assure them that the issue has been investigated and resolved.\n"
                + "4. Maintain CodeNova light professional branding and tone.\n\n"
                + contextBuilder.toString();

        if (req != null && req.getCustomInstruction() != null && !req.getCustomInstruction().isBlank()) {
            prompt += "\nSpecial Admin Instruction: " + req.getCustomInstruction().trim();
        }

        String draftContent;
        if (aiService != null) {
            try {
                AiChatRequest aiReq = new AiChatRequest();
                aiReq.setMessage(prompt);
                aiReq.setPage("support-admin");
                AiChatResponse aiResp = aiService.chat(aiReq, adminUserId, "admin");
                draftContent = aiResp != null ? aiResp.getReply() : null;
                if (draftContent == null || draftContent.isBlank()) {
                    draftContent = generateIntelligentSupportFallback(request);
                }
            } catch (Exception e) {
                logger.warn("AI draft generation fallback triggered: {}", e.getMessage());
                draftContent = generateIntelligentSupportFallback(request);
            }
        } else {
            draftContent = generateIntelligentSupportFallback(request);
        }

        return new AiSupportDraftResponse(
                draftContent,
                "WAITING_FOR_USER",
                request.getPriority(),
                "Draft generated based on ticket subject and user conversation context."
        );
    }

    private String generateIntelligentSupportFallback(SupportRequest req) {
        String name = req.getUser() != null ? req.getUser().getName() : "Candidate";
        String cat = req.getCategory();

        if (cat.contains("Judge") || cat.contains("Compiler") || cat.contains("Submission")) {
            return "Hello " + name + ",\n\n"
                    + "Thank you for reaching out to CodeNova Support.\n\n"
                    + "We have reviewed your report regarding the code execution and online judge issue. Our engineering team has checked the compiler sandbox environment and verified that system test runner instances are functioning normally. Please ensure your solution implements the required class interface without custom package declarations.\n\n"
                    + "If you continue to experience timeouts or unexpected outputs, please reply with your code snippet and submission ID so we can inspect the exact runtime traces for you.\n\n"
                    + "Best regards,\nCodeNova Support Team";
        }

        if (cat.contains("Assessment")) {
            return "Hello " + name + ",\n\n"
                    + "Thank you for contacting CodeNova Support regarding your assessment.\n\n"
                    + "We have verified the assessment configuration and test question assets. Assessment attempt records and score calculations have been reviewed. If this was related to a timer discrepancy or submission failure, your attempt logs have been updated accordingly.\n\n"
                    + "Please let us know if everything looks correct on your assessment summary page.\n\n"
                    + "Best regards,\nCodeNova Support Team";
        }

        if (cat.contains("Contest")) {
            return "Hello " + name + ",\n\n"
                    + "Thank you for reaching out to CodeNova Support.\n\n"
                    + "We have inspected the contest session logs for your account. Please remember that during timed contests, tab switches (visibility change events) and exiting fullscreen mode are logged for exam integrity. We have reviewed your contest score and leaderboard ranking.\n\n"
                    + "Please let us know if you require any further clarification.\n\n"
                    + "Best regards,\nCodeNova Support Team";
        }

        return "Hello " + name + ",\n\n"
                + "Thank you for reaching out to CodeNova Support.\n\n"
                + "We have received your ticket regarding \"" + req.getSubject() + "\" and our support team has investigated the issue. Everything has been reviewed in the system, and your account records are synchronized.\n\n"
                + "Please let us know if you have any further questions or if we can assist you with anything else.\n\n"
                + "Best regards,\nCodeNova Support Team";
    }

    // ── Attachment Storage & Retrieval ────────────────────────────────────

    public Map<String, Object> storeAttachment(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File exceeds maximum allowed size of 10MB.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx >= 0) {
            extension = originalFilename.substring(dotIdx + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file type (." + extension + "). Allowed formats: PNG, JPG, JPEG, WEBP, PDF, TXT.");
        }

        String safeFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        try {
            Path targetLocation = UPLOAD_DIR.resolve(safeFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> result = new HashMap<>();
            result.put("url", "/api/support/attachments/" + safeFileName);
            result.put("fileName", originalFilename);
            result.put("fileType", file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            result.put("fileSize", file.getSize());
            return result;
        } catch (IOException e) {
            logger.error("Failed to store attachment file: {}", e.getMessage());
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Resource getAttachmentResource(String fileName) {
        try {
            Path filePath = UPLOAD_DIR.resolve(fileName).normalize();
            if (!filePath.startsWith(UPLOAD_DIR)) {
                throw new BadRequestException("Invalid file path traversal.");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Attachment file not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Attachment file not found: " + fileName);
        }
    }

    // ── Assessment Question Feedback (Report Question during attempt) ─────

    public AssessmentQuestionFeedbackDto createAssessmentQuestionFeedback(CreateAssessmentQuestionFeedbackRequest req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Assessment assessment = assessmentRepository.findById(req.getAssessmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + req.getAssessmentId()));

        AssessmentQuestion question = assessmentQuestionRepository.findById(req.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + req.getQuestionId()));

        AssessmentQuestionFeedback feedback = new AssessmentQuestionFeedback();
        feedback.setUser(user);
        feedback.setAssessment(assessment);
        feedback.setQuestion(question);
        feedback.setReason(req.getReason());
        feedback.setMessage(req.getMessage());

        AssessmentQuestionFeedback saved = assessmentQuestionFeedbackRepository.save(feedback);

        if (emailService != null && assessment.getCreatedBy() != null) {
            try {
                emailService.sendAssessmentQuestionReportEmail(assessment.getCreatedBy(), assessment, saved);
            } catch (Exception ex) {
                logger.warn("Could not dispatch question report email to host {}: {}", assessment.getCreatedBy().getEmail(), ex.getMessage());
            }
        }

        return mapToAssessmentQuestionFeedbackDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AssessmentQuestionFeedbackDto> getAllAssessmentQuestionFeedback() {
        return assessmentQuestionFeedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToAssessmentQuestionFeedbackDto)
                .collect(Collectors.toList());
    }

    // ── Post-Completion Assessment Feedback (Candidate -> Host/Assessment) ─

    public AssessmentFeedbackDto submitAssessmentFeedback(CreateAssessmentFeedbackRequest req, Long userId) {
        User candidate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Assessment assessment = assessmentRepository.findById(req.getAssessmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + req.getAssessmentId()));

        AssessmentAttempt attempt = null;
        if (req.getAttemptId() != null) {
            attempt = assessmentAttemptRepository.findById(req.getAttemptId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not found: " + req.getAttemptId()));

            if (!attempt.getUser().getId().equals(userId)) {
                throw new ForbiddenException("You can only submit feedback for your own assessment attempt.");
            }

            Optional<AssessmentFeedback> existingOpt = assessmentFeedbackRepository.findByAttemptId(attempt.getId());
            if (existingOpt.isPresent()) {
                AssessmentFeedback existing = existingOpt.get();
                existing.setRating(req.getRating());
                existing.setFeedbackType(req.getFeedbackType());
                existing.setComments(req.getComments());
                AssessmentFeedback updated = assessmentFeedbackRepository.save(existing);
                return mapToAssessmentFeedbackDto(updated);
            }
        }

        User host = assessment.getCreatedBy();

        AssessmentFeedback feedback = new AssessmentFeedback();
        feedback.setUser(candidate);
        feedback.setAssessment(assessment);
        feedback.setHost(host);
        feedback.setAttempt(attempt);
        feedback.setRating(req.getRating());
        feedback.setFeedbackType(req.getFeedbackType());
        feedback.setComments(req.getComments());

        AssessmentFeedback saved = assessmentFeedbackRepository.save(feedback);

        if (emailService != null && host != null && host.getEmail() != null) {
            try {
                emailService.sendAssessmentFeedbackEmail(host, assessment, saved);
            } catch (Exception e) {
                logger.warn("Could not send assessment feedback email to host {}: {}", host.getEmail(), e.getMessage());
            }
        }

        return mapToAssessmentFeedbackDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<AssessmentFeedbackDto> getFeedbackForAttempt(Long attemptId, Long userId) {
        return assessmentFeedbackRepository.findByAttemptId(attemptId)
                .filter(fb -> fb.getUser() != null && fb.getUser().getId().equals(userId))
                .map(this::mapToAssessmentFeedbackDto);
    }

    @Transactional(readOnly = true)
    public AssessmentHostFeedbackSummaryDto getHostAssessmentFeedback(Long assessmentId, Long hostUserId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getCreatedBy() != null && !assessment.getCreatedBy().getId().equals(hostUserId)) {
            throw new ForbiddenException("You do not have permission to view feedback for this assessment.");
        }

        List<AssessmentFeedback> list = assessmentFeedbackRepository.findByAssessmentIdOrderByCreatedAtDesc(assessmentId);
        List<AssessmentFeedbackDto> dtos = list.stream().map(this::mapToAssessmentFeedbackDto).collect(Collectors.toList());

        double avg = 0.0;
        if (!list.isEmpty()) {
            double sum = list.stream().mapToInt(AssessmentFeedback::getRating).sum();
            avg = BigDecimal.valueOf(sum / list.size()).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        return new AssessmentHostFeedbackSummaryDto(assessment.getId(), assessment.getTitle(), list.size(), avg, dtos);
    }

    @Transactional(readOnly = true)
    public List<AssessmentFeedbackDto> getAllHostAssessmentFeedback(Long hostUserId) {
        return assessmentFeedbackRepository.findByAssessmentCreatedByIdOrderByCreatedAtDesc(hostUserId)
                .stream()
                .map(this::mapToAssessmentFeedbackDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssessmentFeedbackDto> getAllAssessmentFeedbackForAdmin() {
        return assessmentFeedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToAssessmentFeedbackDto)
                .collect(Collectors.toList());
    }

    // ── General Feedback ──────────────────────────────────────────────────

    public GeneralFeedbackDto createGeneralFeedback(CreateGeneralFeedbackRequest req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        GeneralFeedback feedback = new GeneralFeedback();
        feedback.setUser(user);
        feedback.setFeedbackType(req.getFeedbackType());
        feedback.setRating(req.getRating());
        feedback.setMessage(req.getMessage());

        GeneralFeedback saved = generalFeedbackRepository.save(feedback);
        return mapToGeneralFeedbackDto(saved);
    }

    @Transactional(readOnly = true)
    public List<GeneralFeedbackDto> getAllGeneralFeedback() {
        return generalFeedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToGeneralFeedbackDto)
                .collect(Collectors.toList());
    }

    // ── Mapping Helpers ───────────────────────────────────────────────────

    private SupportRequestDto mapToSupportRequestDto(SupportRequest req, boolean includeMessages) {
        return mapToSupportRequestDto(req, includeMessages, false);
    }

    private SupportRequestDto mapToSupportRequestDto(SupportRequest req, boolean includeMessages, boolean isAdminOrStaff) {
        SupportRequestDto dto = new SupportRequestDto();
        dto.setId(req.getId());
        if (req.getUser() != null) {
            dto.setUserId(req.getUser().getId());
            dto.setUsername(req.getUser().getUsername());
            dto.setUserEmail(req.getUser().getEmail());
            dto.setUserName(req.getUser().getName());
        }
        dto.setTicketNumber(req.getTicketNumber());
        dto.setCategory(req.getCategory());
        dto.setSubject(req.getSubject());
        dto.setDescription(req.getDescription());
        dto.setPriority(req.getPriority() != null ? req.getPriority() : "MEDIUM");
        dto.setStatus(req.getStatus());

        if (req.getAssignedTo() != null) {
            dto.setAssignedToUserId(req.getAssignedTo().getId());
            dto.setAssignedToUsername(req.getAssignedTo().getUsername());
            dto.setAssignedToName(req.getAssignedTo().getName());
        }

        dto.setAssessmentId(req.getAssessmentId());
        dto.setAssessmentTitle(req.getAssessmentTitle());
        dto.setContestId(req.getContestId());
        dto.setContestTitle(req.getContestTitle());
        dto.setProblemId(req.getProblemId());
        dto.setProblemTitle(req.getProblemTitle());
        dto.setSubmissionId(req.getSubmissionId());

        dto.setAttachmentUrl(req.getAttachmentUrl());
        dto.setAttachmentName(req.getAttachmentName());
        dto.setAttachmentType(req.getAttachmentType());
        dto.setAttachmentSize(req.getAttachmentSize());

        dto.setRating(req.getRating());
        dto.setRatingComments(req.getRatingComments());
        dto.setRatedAt(req.getRatedAt());

        dto.setCreatedAt(req.getCreatedAt());
        dto.setUpdatedAt(req.getUpdatedAt());
        dto.setResolvedAt(req.getResolvedAt());
        dto.setClosedAt(req.getClosedAt());

        // Count messages
        long msgCount = supportMessageRepository.countBySupportRequestId(req.getId());
        dto.setMessageCount((int) msgCount);

        if (includeMessages) {
            List<SupportMessage> messages = isAdminOrStaff
                    ? supportMessageRepository.findBySupportRequestIdOrderByCreatedAtAsc(req.getId())
                    : supportMessageRepository.findBySupportRequestIdAndIsInternalNoteFalseOrderByCreatedAtAsc(req.getId());

            dto.setMessages(messages.stream().map(this::mapToSupportMessageDto).collect(Collectors.toList()));
            if (!messages.isEmpty()) {
                SupportMessage last = messages.get(messages.size() - 1);
                dto.setLastMessagePreview(last.getMessage().length() > 80 ? last.getMessage().substring(0, 80) + "..." : last.getMessage());
                dto.setLastMessageAt(last.getCreatedAt());
            }
        }

        return dto;
    }

    private SupportMessageDto mapToSupportMessageDto(SupportMessage msg) {
        SupportMessageDto dto = new SupportMessageDto();
        dto.setId(msg.getId());
        if (msg.getSupportRequest() != null) {
            dto.setSupportRequestId(msg.getSupportRequest().getId());
        }
        if (msg.getSender() != null) {
            dto.setSenderId(msg.getSender().getId());
            dto.setSenderUsername(msg.getSender().getUsername());
            dto.setSenderName(msg.getSender().getName());
        }
        dto.setSenderRole(msg.getSenderRole());
        dto.setMessage(msg.getMessage());
        dto.setInternalNote(msg.isInternalNote());
        dto.setAttachmentUrl(msg.getAttachmentUrl());
        dto.setAttachmentName(msg.getAttachmentName());
        dto.setAttachmentType(msg.getAttachmentType());
        dto.setAttachmentSize(msg.getAttachmentSize());
        dto.setCreatedAt(msg.getCreatedAt());
        return dto;
    }

    private AssessmentQuestionFeedbackDto mapToAssessmentQuestionFeedbackDto(AssessmentQuestionFeedback fb) {
        AssessmentQuestionFeedbackDto dto = new AssessmentQuestionFeedbackDto();
        dto.setId(fb.getId());
        if (fb.getUser() != null) {
            dto.setUserId(fb.getUser().getId());
            dto.setUsername(fb.getUser().getUsername());
            dto.setUserEmail(fb.getUser().getEmail());
        }
        if (fb.getAssessment() != null) {
            dto.setAssessmentId(fb.getAssessment().getId());
            dto.setAssessmentTitle(fb.getAssessment().getTitle());
        }
        if (fb.getQuestion() != null) {
            dto.setQuestionId(fb.getQuestion().getId());
            dto.setQuestionText(fb.getQuestion().getQuestionText());
        }
        dto.setReason(fb.getReason());
        dto.setMessage(fb.getMessage());
        dto.setCreatedAt(fb.getCreatedAt());
        return dto;
    }

    private AssessmentFeedbackDto mapToAssessmentFeedbackDto(AssessmentFeedback fb) {
        AssessmentFeedbackDto dto = new AssessmentFeedbackDto();
        dto.setId(fb.getId());
        if (fb.getUser() != null) {
            dto.setUserId(fb.getUser().getId());
            dto.setUsername(fb.getUser().getUsername());
            dto.setUserEmail(fb.getUser().getEmail());
        }
        if (fb.getAssessment() != null) {
            dto.setAssessmentId(fb.getAssessment().getId());
            dto.setAssessmentTitle(fb.getAssessment().getTitle());
        }
        if (fb.getHost() != null) {
            dto.setHostId(fb.getHost().getId());
            dto.setHostUsername(fb.getHost().getUsername());
        }
        if (fb.getAttempt() != null) {
            dto.setAttemptId(fb.getAttempt().getId());
        }
        dto.setRating(fb.getRating());
        dto.setFeedbackType(fb.getFeedbackType());
        dto.setComments(fb.getComments());
        dto.setCreatedAt(fb.getCreatedAt());
        return dto;
    }

    private GeneralFeedbackDto mapToGeneralFeedbackDto(GeneralFeedback gf) {
        GeneralFeedbackDto dto = new GeneralFeedbackDto();
        dto.setId(gf.getId());
        if (gf.getUser() != null) {
            dto.setUserId(gf.getUser().getId());
            dto.setUsername(gf.getUser().getUsername());
            dto.setUserEmail(gf.getUser().getEmail());
        }
        dto.setFeedbackType(gf.getFeedbackType());
        dto.setRating(gf.getRating());
        dto.setMessage(gf.getMessage());
        dto.setCreatedAt(gf.getCreatedAt());
        return dto;
    }
}
