package com.oj.platform.service;

import com.oj.platform.dto.*;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.exception.ForbiddenException;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.exception.UnauthorizedException;
import com.oj.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Skill Assessment service — supports:
 *   - MCQ / TRUE_FALSE / PROGRAMMING question types in mixed assessments
 *   - Multi-attempt with configurable maxAttempts (default 1)
 *   - Proportional scoring for programming questions
 *   - Candidate details collection
 *   - Proctoring violation tracking
 *   - Code execution + grading via CodeExecutionService
 *
 * Security rules preserved:
 *   - Answer key hidden while attempt is IN_PROGRESS
 *   - Hidden test case inputs/outputs never sent to candidates
 *   - Score always computed server-side; never trusted from client
 */
@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentOptionRepository assessmentOptionRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final UserRepository userRepository;
    private final AssessmentHostVerificationService hostVerificationService;
    private final ProgrammingQuestionRepository programmingQuestionRepository;
    private final ProgrammingTestCaseRepository programmingTestCaseRepository;
    private final CandidateDetailsRepository candidateDetailsRepository;
    private final AssessmentViolationRepository assessmentViolationRepository;
    private final AssessmentProgrammingSubmissionRepository programmingSubmissionRepository;
    private final CodeExecutionService codeExecutionService;
    private final EmailService emailService;

    public AssessmentService(
            AssessmentRepository assessmentRepository,
            AssessmentQuestionRepository assessmentQuestionRepository,
            AssessmentOptionRepository assessmentOptionRepository,
            AssessmentAttemptRepository assessmentAttemptRepository,
            AssessmentAnswerRepository assessmentAnswerRepository,
            UserRepository userRepository,
            AssessmentHostVerificationService hostVerificationService,
            ProgrammingQuestionRepository programmingQuestionRepository,
            ProgrammingTestCaseRepository programmingTestCaseRepository,
            CandidateDetailsRepository candidateDetailsRepository,
            AssessmentViolationRepository assessmentViolationRepository,
            AssessmentProgrammingSubmissionRepository programmingSubmissionRepository,
            CodeExecutionService codeExecutionService) {
        this(assessmentRepository, assessmentQuestionRepository, assessmentOptionRepository,
                assessmentAttemptRepository, assessmentAnswerRepository, userRepository,
                hostVerificationService, programmingQuestionRepository, programmingTestCaseRepository,
                candidateDetailsRepository, assessmentViolationRepository, programmingSubmissionRepository,
                codeExecutionService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AssessmentService(
            AssessmentRepository assessmentRepository,
            AssessmentQuestionRepository assessmentQuestionRepository,
            AssessmentOptionRepository assessmentOptionRepository,
            AssessmentAttemptRepository assessmentAttemptRepository,
            AssessmentAnswerRepository assessmentAnswerRepository,
            UserRepository userRepository,
            AssessmentHostVerificationService hostVerificationService,
            ProgrammingQuestionRepository programmingQuestionRepository,
            ProgrammingTestCaseRepository programmingTestCaseRepository,
            CandidateDetailsRepository candidateDetailsRepository,
            AssessmentViolationRepository assessmentViolationRepository,
            AssessmentProgrammingSubmissionRepository programmingSubmissionRepository,
            CodeExecutionService codeExecutionService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) EmailService emailService) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentOptionRepository = assessmentOptionRepository;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.assessmentAnswerRepository = assessmentAnswerRepository;
        this.userRepository = userRepository;
        this.hostVerificationService = hostVerificationService;
        this.programmingQuestionRepository = programmingQuestionRepository;
        this.programmingTestCaseRepository = programmingTestCaseRepository;
        this.candidateDetailsRepository = candidateDetailsRepository;
        this.assessmentViolationRepository = assessmentViolationRepository;
        this.programmingSubmissionRepository = programmingSubmissionRepository;
        this.codeExecutionService = codeExecutionService;
        this.emailService = emailService;
    }

    // =====================================================================
    // ADMIN - ASSESSMENT LIFECYCLE
    // =====================================================================

    @Transactional
    public AssessmentDto createAssessment(CreateAssessmentRequest request, Long adminUserId) {
        User admin = adminUserId != null
                ? userRepository.findById(adminUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUserId))
                : null;

        Assessment assessment = new Assessment(request.getTitle(), request.getDescription(), request.getInstructions(),
                request.getDurationMinutes(), request.getPassingMarks(), admin);
        applyAssessmentConfigFields(assessment, request);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            assessment.setStatus(parseAssessmentStatus(request.getStatus()));
        }
        Assessment saved = assessmentRepository.save(assessment);
        return toAssessmentDto(saved, false, false);
    }

    public boolean isHostedByOtherUser(Assessment assessment) {
        return assessment != null && assessment.getCreatedBy() != null && assessment.getCreatedBy().getRole() != Role.ROLE_ADMIN;
    }

    private void assertNotHostedByOtherUser(Assessment assessment) {
        if (isHostedByOtherUser(assessment)) {
            throw new ForbiddenException("Admins cannot edit assessments hosted by other users.");
        }
    }

    /** Metadata only — status changes use publish/archive endpoints. */
    @Transactional
    public AssessmentDto updateAssessment(Long id, CreateAssessmentRequest request) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
        assertNotHostedByOtherUser(assessment);
        return applyAssessmentUpdates(assessment, request);
    }

    private AssessmentDto applyAssessmentUpdates(Assessment assessment, CreateAssessmentRequest request) {
        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setInstructions(request.getInstructions());
        assessment.setDurationMinutes(request.getDurationMinutes());
        assessment.setPassingMarks(request.getPassingMarks() != null ? request.getPassingMarks() : 0);
        applyAssessmentConfigFields(assessment, request);
        Assessment saved = assessmentRepository.save(assessment);
        return toAssessmentDto(saved, false, false);
    }

    private void applyAssessmentConfigFields(Assessment assessment, CreateAssessmentRequest request) {
        if (request.getMaxAttempts() != null) {
            assessment.setMaxAttempts(request.getMaxAttempts());
        }
        assessment.setStartTime(request.getStartTime());
        assessment.setEndTime(request.getEndTime());
        if (request.getNegativeMarkingEnabled() != null) {
            assessment.setNegativeMarkingEnabled(request.getNegativeMarkingEnabled());
        }
        if (request.getNegativeMarkingValue() != null) {
            assessment.setNegativeMarkingValue(request.getNegativeMarkingValue());
        }
        if (request.getRequireCandidateDetails() != null) {
            assessment.setRequireCandidateDetails(request.getRequireCandidateDetails());
        }
        assessment.setCandidateFieldsConfig(request.getCandidateFieldsConfig());
    }

    @Transactional
    public AssessmentDto publishAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
        assertNotHostedByOtherUser(assessment);
        return applyPublishAssessment(assessment);
    }

    private AssessmentDto applyPublishAssessment(Assessment assessment) {
        if (assessment.getStatus() == AssessmentStatus.ARCHIVED) {
            throw new BadRequestException("Cannot publish an archived assessment.");
        }
        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessment.getId());
        if (questions.isEmpty()) {
            throw new BadRequestException("Cannot publish an assessment with no questions.");
        }
        for (AssessmentQuestion q : questions) {
            // Skip option validation for PROGRAMMING type — they have no MCQ options
            if (q.getQuestionType() == AssessmentQuestionType.PROGRAMMING) {
                if (q.getProgrammingQuestion() == null) {
                    throw new BadRequestException("Programming question #" + q.getOrderIndex() + " has no problem statement configured.");
                }
                continue;
            }
            List<AssessmentOption> opts = assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(q.getId());
            if (opts.size() < 2) {
                throw new BadRequestException("Question \"" + q.getQuestionText().substring(0, Math.min(40, q.getQuestionText().length())) + "...\" must have at least 2 options.");
            }
            long correctCount = opts.stream().filter(o -> Boolean.TRUE.equals(o.getIsCorrect())).count();
            if (correctCount != 1) {
                throw new BadRequestException("Question \"" + q.getQuestionText().substring(0, Math.min(40, q.getQuestionText().length())) + "...\" must have exactly 1 correct answer.");
            }
        }
        assessment.setStatus(AssessmentStatus.PUBLISHED);
        Assessment saved = assessmentRepository.save(assessment);
        return toAssessmentDto(saved, false, false);
    }

    private AssessmentDto applyArchiveAssessment(Assessment assessment) {
        if (assessment.getStatus() == AssessmentStatus.DRAFT) {
            throw new BadRequestException("Cannot archive a DRAFT assessment. Publish it first.");
        }
        assessment.setStatus(AssessmentStatus.ARCHIVED);
        Assessment saved = assessmentRepository.save(assessment);
        return toAssessmentDto(saved, false, false);
    }

    @Transactional
    public AssessmentDto archiveAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
        assertNotHostedByOtherUser(assessment);
        return applyArchiveAssessment(assessment);
    }

    @Transactional(readOnly = true)
    public List<AssessmentDto> listAssessments() {
        return assessmentRepository.findAll().stream()
                .map(a -> toAssessmentDto(a, false, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssessmentDto getAssessmentAdmin(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
        return toAssessmentDto(assessment, true, true);
    }

    // =====================================================================
    // HOST - VERIFICATION CHECK
    // =====================================================================

    public void requireVerifiedHost(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        if (!hostVerificationService.isVerifiedHost(userId)) {
            throw new ForbiddenException("Only verified hosts can create and manage assessments.");
        }
    }

    // =====================================================================
    // HOST - ASSESSMENT LIFECYCLE
    // =====================================================================

    @Transactional
    public AssessmentDto createHostedAssessment(CreateAssessmentRequest request, Long userId) {
        requireVerifiedHost(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Assessment assessment = new Assessment(request.getTitle(), request.getDescription(), request.getInstructions(),
                request.getDurationMinutes(), request.getPassingMarks(), user);
        applyAssessmentConfigFields(assessment, request);
        Assessment saved = assessmentRepository.save(assessment);
        return toAssessmentDto(saved, false, false);
    }

    @Transactional(readOnly = true)
    public List<AssessmentDto> listOwnAssessments(Long userId) {
        return assessmentRepository.findByCreatedByIdOrderByCreatedAtDesc(userId).stream()
                .map(a -> toAssessmentDto(a, false, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssessmentDto getOwnAssessment(Long id, Long userId) {
        Assessment assessment = loadAndAuthorizeOwnership(id, userId);
        return toAssessmentDto(assessment, true, true);
    }

    @Transactional
    public AssessmentDto updateOwnAssessment(Long id, CreateAssessmentRequest request, Long userId) {
        Assessment assessment = loadAndAuthorizeOwnership(id, userId);
        return applyAssessmentUpdates(assessment, request);
    }

    @Transactional
    public AssessmentDto publishOwnAssessment(Long id, Long userId) {
        Assessment assessment = loadAndAuthorizeOwnership(id, userId);
        return applyPublishAssessment(assessment);
    }

    @Transactional
    public AssessmentDto archiveOwnAssessment(Long id, Long userId) {
        Assessment assessment = loadAndAuthorizeOwnership(id, userId);
        return applyArchiveAssessment(assessment);
    }

    @Transactional
    public AssessmentQuestionDto addOwnQuestion(Long assessmentId, CreateAssessmentQuestionRequest request, Long userId) {
        Assessment assessment = loadAndAuthorizeOwnership(assessmentId, userId);
        return applyAddQuestion(assessment, request);
    }

    @Transactional
    public AssessmentQuestionDto updateOwnQuestion(Long assessmentId, Long questionId,
                                                    CreateAssessmentQuestionRequest request, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        return applyUpdateQuestion(question, request);
    }

    @Transactional
    public void deleteOwnQuestion(Long assessmentId, Long questionId, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        applyDeleteQuestion(question);
    }

    @Transactional
    public AssessmentOptionDto addOwnOption(Long assessmentId, Long questionId,
                                             CreateAssessmentOptionRequest request, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        return applyAddOption(question, request);
    }

    @Transactional
    public AssessmentOptionDto updateOwnOption(Long assessmentId, Long questionId, Long optionId,
                                                CreateAssessmentOptionRequest request, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        AssessmentOption option = validateOptionOwnership(question, optionId);
        return applyUpdateOption(option, request);
    }

    @Transactional
    public void deleteOwnOption(Long assessmentId, Long questionId, Long optionId, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        AssessmentOption option = validateOptionOwnership(question, optionId);
        applyDeleteOption(option);
    }

    // ─── Host: test case management ───────────────────────────────────────

    @Transactional
    public ProgrammingTestCaseDto addOwnTestCase(Long assessmentId, Long questionId,
                                                  ProgrammingTestCaseRequest request, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        if (question.getQuestionType() != AssessmentQuestionType.PROGRAMMING) {
            throw new BadRequestException("Test cases can only be added to PROGRAMMING type questions.");
        }
        if (question.getProgrammingQuestion() == null) {
            throw new BadRequestException("This question does not have a programming question configured yet.");
        }
        return applyAddTestCase(question.getProgrammingQuestion(), request);
    }

    private ProgrammingTestCaseDto applyAddTestCase(ProgrammingQuestion pq, ProgrammingTestCaseRequest request) {
        ProgrammingTestCase tc = new ProgrammingTestCase();
        tc.setProgrammingQuestion(pq);
        tc.setInput(request.getInput());
        tc.setExpectedOutput(request.getExpectedOutput());
        tc.setIsHidden(Boolean.TRUE.equals(request.getIsHidden()));
        tc.setDescription(request.getDescription());
        int nextIndex = request.getOrderIndex() != null ? request.getOrderIndex()
                : (int) programmingTestCaseRepository.countByProgrammingQuestionId(pq.getId()) + 1;
        tc.setOrderIndex(nextIndex);
        ProgrammingTestCase saved = programmingTestCaseRepository.save(tc);
        return toTestCaseDto(saved);
    }

    @Transactional
    public ProgrammingTestCaseDto updateOwnTestCase(Long assessmentId, Long questionId, Long testCaseId,
                                                     ProgrammingTestCaseRequest request, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        ProgrammingTestCase tc = programmingTestCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found: " + testCaseId));
        if (!tc.getProgrammingQuestion().getId().equals(question.getProgrammingQuestion().getId())) {
            throw new BadRequestException("Test case does not belong to this question.");
        }
        tc.setInput(request.getInput());
        tc.setExpectedOutput(request.getExpectedOutput());
        tc.setIsHidden(Boolean.TRUE.equals(request.getIsHidden()));
        if (request.getOrderIndex() != null) tc.setOrderIndex(request.getOrderIndex());
        tc.setDescription(request.getDescription());
        return toTestCaseDto(programmingTestCaseRepository.save(tc));
    }

    @Transactional
    public void deleteOwnTestCase(Long assessmentId, Long questionId, Long testCaseId, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        ProgrammingTestCase tc = programmingTestCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found: " + testCaseId));
        if (!tc.getProgrammingQuestion().getId().equals(question.getProgrammingQuestion().getId())) {
            throw new BadRequestException("Test case does not belong to this question.");
        }
        programmingTestCaseRepository.delete(tc);
    }

    @Transactional(readOnly = true)
    public List<ProgrammingTestCaseDto> getOwnTestCases(Long assessmentId, Long questionId, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        if (question.getProgrammingQuestion() == null) return List.of();
        return programmingTestCaseRepository
                .findByProgrammingQuestionIdOrderByOrderIndexAsc(question.getProgrammingQuestion().getId())
                .stream().map(this::toTestCaseDto).collect(Collectors.toList());
    }

    private Assessment loadAndAuthorizeOwnership(Long assessmentId, Long userId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (isHostedByOtherUser(assessment) && !assessment.getCreatedBy().getId().equals(userId)) {
            throw new ForbiddenException("Cannot edit an assessment hosted by another user.");
        }
        if (user.getRole() == Role.ROLE_ADMIN) {
            return assessment;
        }
        if (assessment.getCreatedBy() == null || !assessment.getCreatedBy().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to manage this assessment.");
        }
        return assessment;
    }

    // =====================================================================
    // ADMIN - QUESTIONS & OPTIONS
    // =====================================================================

    @Transactional
    public AssessmentQuestionDto addQuestion(Long assessmentId, CreateAssessmentQuestionRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));
        assertNotHostedByOtherUser(assessment);
        return applyAddQuestion(assessment, request);
    }

    private AssessmentQuestionDto applyAddQuestion(Assessment assessment, CreateAssessmentQuestionRequest request) {
        Long assessmentId = assessment.getId();
        AssessmentQuestionType type = parseQuestionType(request.getQuestionType());
        int orderIndex = request.getOrderIndex() != null
                ? request.getOrderIndex()
                : (int) assessmentQuestionRepository.countByAssessmentId(assessmentId) + 1;

        AssessmentQuestion question = new AssessmentQuestion(assessment, request.getQuestionText(), type,
                request.getMarks(), orderIndex, request.getExplanation());

        // For PROGRAMMING type: create the linked ProgrammingQuestion
        if (type == AssessmentQuestionType.PROGRAMMING && request.getProgrammingQuestion() != null) {
            ProgrammingQuestion pq = buildProgrammingQuestion(request.getProgrammingQuestion());
            question.setProgrammingQuestion(pq);
        }

        AssessmentQuestion savedQuestion = assessmentQuestionRepository.save(question);

        // For MCQ/TRUE_FALSE: add options if provided inline
        if (type != AssessmentQuestionType.PROGRAMMING && request.getOptions() != null) {
            int nextIndex = 1;
            for (CreateAssessmentOptionRequest optionRequest : request.getOptions()) {
                int idx = optionRequest.getOrderIndex() != null ? optionRequest.getOrderIndex() : nextIndex++;
                assessmentOptionRepository.save(new AssessmentOption(
                        savedQuestion, optionRequest.getOptionText(), optionRequest.getIsCorrect(), idx));
            }
        }

        recalculateTotalMarks(assessment);
        return toQuestionDto(savedQuestion, true, null);
    }

    private ProgrammingQuestion buildProgrammingQuestion(ProgrammingQuestionRequest req) {
        ProgrammingQuestion pq = new ProgrammingQuestion();
        pq.setProblemStatement(req.getProblemStatement());
        pq.setInputFormat(req.getInputFormat());
        pq.setOutputFormat(req.getOutputFormat());
        pq.setConstraints(req.getConstraints());
        pq.setSampleInput(req.getSampleInput());
        pq.setSampleOutput(req.getSampleOutput());
        if (req.getSupportedLanguages() != null) pq.setSupportedLanguages(req.getSupportedLanguages());
        pq.setStarterCodeJava(req.getStarterCodeJava());
        pq.setStarterCodePython(req.getStarterCodePython());
        pq.setStarterCodeCpp(req.getStarterCodeCpp());
        pq.setStarterCodeJavascript(req.getStarterCodeJavascript());
        if (req.getTimeLimitMs() != null) pq.setTimeLimitMs(req.getTimeLimitMs());
        if (req.getMemoryLimitMb() != null) pq.setMemoryLimitMb(req.getMemoryLimitMb());
        return pq;
    }

    @Transactional
    public AssessmentQuestionDto updateQuestion(Long assessmentId, Long questionId, CreateAssessmentQuestionRequest request) {
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        assertNotHostedByOtherUser(question.getAssessment());
        return applyUpdateQuestion(question, request);
    }

    private AssessmentQuestionDto applyUpdateQuestion(AssessmentQuestion question, CreateAssessmentQuestionRequest request) {
        question.setQuestionText(request.getQuestionText());
        AssessmentQuestionType type = parseQuestionType(request.getQuestionType());
        question.setQuestionType(type);
        question.setMarks(request.getMarks());
        if (request.getOrderIndex() != null) question.setOrderIndex(request.getOrderIndex());
        question.setExplanation(request.getExplanation());

        // Update programming question content if provided
        if (type == AssessmentQuestionType.PROGRAMMING && request.getProgrammingQuestion() != null) {
            ProgrammingQuestion pq = question.getProgrammingQuestion();
            if (pq == null) {
                pq = buildProgrammingQuestion(request.getProgrammingQuestion());
            } else {
                ProgrammingQuestionRequest req = request.getProgrammingQuestion();
                if (req.getProblemStatement() != null) pq.setProblemStatement(req.getProblemStatement());
                if (req.getInputFormat() != null) pq.setInputFormat(req.getInputFormat());
                if (req.getOutputFormat() != null) pq.setOutputFormat(req.getOutputFormat());
                if (req.getConstraints() != null) pq.setConstraints(req.getConstraints());
                if (req.getSampleInput() != null) pq.setSampleInput(req.getSampleInput());
                if (req.getSampleOutput() != null) pq.setSampleOutput(req.getSampleOutput());
                if (req.getSupportedLanguages() != null) pq.setSupportedLanguages(req.getSupportedLanguages());
                if (req.getStarterCodeJava() != null) pq.setStarterCodeJava(req.getStarterCodeJava());
                if (req.getStarterCodePython() != null) pq.setStarterCodePython(req.getStarterCodePython());
                if (req.getStarterCodeCpp() != null) pq.setStarterCodeCpp(req.getStarterCodeCpp());
                if (req.getStarterCodeJavascript() != null) pq.setStarterCodeJavascript(req.getStarterCodeJavascript());
                if (req.getTimeLimitMs() != null) pq.setTimeLimitMs(req.getTimeLimitMs());
                if (req.getMemoryLimitMb() != null) pq.setMemoryLimitMb(req.getMemoryLimitMb());
            }
            question.setProgrammingQuestion(pq);
        }

        AssessmentQuestion saved = assessmentQuestionRepository.save(question);
        recalculateTotalMarks(question.getAssessment());
        return toQuestionDto(saved, true, null);
    }

    @Transactional
    public void deleteQuestion(Long assessmentId, Long questionId) {
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        assertNotHostedByOtherUser(question.getAssessment());
        applyDeleteQuestion(question);
    }

    private void applyDeleteQuestion(AssessmentQuestion question) {
        Assessment assessment = question.getAssessment();
        assessmentQuestionRepository.delete(question);
        recalculateTotalMarks(assessment);
    }

    @Transactional
    public AssessmentOptionDto addOption(Long assessmentId, Long questionId, CreateAssessmentOptionRequest request) {
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        assertNotHostedByOtherUser(question.getAssessment());
        return applyAddOption(question, request);
    }

    private AssessmentOptionDto applyAddOption(AssessmentQuestion question, CreateAssessmentOptionRequest request) {
        int orderIndex = request.getOrderIndex() != null
                ? request.getOrderIndex()
                : assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(question.getId()).size() + 1;

        AssessmentOption option = new AssessmentOption(question, request.getOptionText(), request.getIsCorrect(), orderIndex);
        AssessmentOption saved = assessmentOptionRepository.save(option);
        return toOptionDto(saved, true);
    }

    @Transactional
    public AssessmentOptionDto updateOption(Long assessmentId, Long questionId, Long optionId, CreateAssessmentOptionRequest request) {
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        assertNotHostedByOtherUser(question.getAssessment());
        AssessmentOption option = validateOptionOwnership(question, optionId);
        return applyUpdateOption(option, request);
    }

    private AssessmentOptionDto applyUpdateOption(AssessmentOption option, CreateAssessmentOptionRequest request) {
        option.setOptionText(request.getOptionText());
        option.setIsCorrect(request.getIsCorrect());
        if (request.getOrderIndex() != null) option.setOrderIndex(request.getOrderIndex());
        AssessmentOption saved = assessmentOptionRepository.save(option);
        return toOptionDto(saved, true);
    }

    @Transactional
    public void deleteOption(Long assessmentId, Long questionId, Long optionId) {
        AssessmentQuestion question = validateQuestionOwnership(assessmentId, questionId);
        assertNotHostedByOtherUser(question.getAssessment());
        AssessmentOption option = validateOptionOwnership(question, optionId);
        applyDeleteOption(option);
    }

    private void applyDeleteOption(AssessmentOption option) {
        assessmentOptionRepository.delete(option);
    }

    // =====================================================================
    // STUDENT - BROWSING
    // =====================================================================

    @Transactional(readOnly = true)
    public List<AssessmentDto> listPublishedAssessments() {
        return assessmentRepository.findByStatus(AssessmentStatus.PUBLISHED).stream()
                .map(a -> toAssessmentDto(a, false, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssessmentDto getAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
        return toAssessmentDto(assessment, false, false);
    }

    // =====================================================================
    // STUDENT - CANDIDATE DETAILS
    // =====================================================================

    @Transactional
    public void saveCandidateDetails(Long assessmentId, Long attemptId, Long userId, CandidateDetailsRequest request) {
        AssessmentAttempt attempt = loadAndAuthorizeAttempt(assessmentId, attemptId, userId);
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot update candidate details for a non-active attempt.");
        }
        CandidateDetails details = candidateDetailsRepository.findByAttemptId(attemptId)
                .orElse(new CandidateDetails());
        details.setAttempt(attempt);
        details.setFullName(request.getFullName());
        details.setEmail(request.getEmail());
        details.setPhone(request.getPhone());
        details.setOrganization(request.getOrganization());
        details.setRegistrationId(request.getRegistrationId());
        candidateDetailsRepository.save(details);
    }

    // =====================================================================
    // STUDENT - ATTEMPTS (MULTI-ATTEMPT AWARE)
    // =====================================================================

    /**
     * Starts a new attempt or returns the current IN_PROGRESS attempt.
     * Multi-attempt: creates a new attempt if previous ones are COMPLETED/EXPIRED and maxAttempts allows.
     */
    @Transactional
    public AssessmentAttemptDto startAttempt(Long assessmentId, Long userId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BadRequestException("This assessment is not available to start.");
        }

        // Check availability window
        LocalDateTime now = LocalDateTime.now();
        if (assessment.getStartTime() != null && now.isBefore(assessment.getStartTime())) {
            throw new BadRequestException("This assessment is not open yet. It starts at " + assessment.getStartTime() + ".");
        }
        if (assessment.getEndTime() != null && now.isAfter(assessment.getEndTime())) {
            throw new BadRequestException("This assessment has closed.");
        }

        // Look for an existing IN_PROGRESS attempt to resume
        Optional<AssessmentAttempt> inProgressOpt = assessmentAttemptRepository
                .findInProgressByAssessmentIdAndUserId(assessmentId, userId);
        if (inProgressOpt.isPresent()) {
            AssessmentAttempt existing = inProgressOpt.get();
            expireIfPastDeadline(existing);
            return toAttemptDto(existing, userId);
        }

        // Count all existing attempts
        long existingCount = assessmentAttemptRepository.countByAssessmentIdAndUserId(assessmentId, userId);
        int maxAttempts = assessment.getMaxAttempts() != null ? assessment.getMaxAttempts() : 1;
        if (existingCount >= maxAttempts) {
            throw new BadRequestException("You have used all " + maxAttempts + " attempt(s) for this assessment.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        int nextAttemptNumber = (int) existingCount + 1;
        AssessmentAttempt attempt = new AssessmentAttempt(assessment, user);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);
        attempt.setAttemptNumber(nextAttemptNumber);
        attempt.setTotalQuestions((int) assessmentQuestionRepository.countByAssessmentId(assessmentId));

        AssessmentAttempt saved = assessmentAttemptRepository.save(attempt);
        return toAttemptDto(saved, userId);
    }

    @Transactional
    public AssessmentAttemptDto getAttempt(Long assessmentId, Long attemptId, Long userId) {
        AssessmentAttempt attempt = loadAndAuthorizeAttempt(assessmentId, attemptId, userId);
        expireIfPastDeadline(attempt);
        return toAttemptDto(attempt, userId);
    }

    /**
     * Records one MCQ/TRUE_FALSE answer (upsert by attempt+question).
     * Grading happens only at submitAttempt, not here.
     */
    @Transactional
    public AssessmentAnswerDto recordAnswer(Long assessmentId, Long attemptId, Long userId, SubmitAssessmentAnswerRequest request) {
        AssessmentAttempt attempt = loadAndAuthorizeAttempt(assessmentId, attemptId, userId);

        if (expireIfPastDeadline(attempt)) {
            throw new BadRequestException("This attempt has expired and no further answers can be recorded.");
        }
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This attempt is no longer active.");
        }

        AssessmentQuestion question = assessmentQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.getQuestionId()));
        if (!question.getAssessment().getId().equals(assessmentId)) {
            throw new BadRequestException("This question does not belong to this assessment.");
        }
        if (question.getQuestionType() == AssessmentQuestionType.PROGRAMMING) {
            throw new BadRequestException("Use the code-run endpoint for PROGRAMMING questions.");
        }

        AssessmentOption option = assessmentOptionRepository.findById(request.getSelectedOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + request.getSelectedOptionId()));
        if (!option.getQuestion().getId().equals(question.getId())) {
            throw new BadRequestException("This option does not belong to the specified question.");
        }

        AssessmentAnswer answer = assessmentAnswerRepository.findByAttemptIdAndQuestionId(attemptId, question.getId())
                .orElseGet(() -> new AssessmentAnswer(attempt, question, option));
        answer.setSelectedOption(option);
        if (answer.getIsCorrect() == null) {
            answer.setIsCorrect(false);
        }
        if (answer.getMarksAwarded() == null) {
            answer.setMarksAwarded(0);
        }
        AssessmentAnswer saved = assessmentAnswerRepository.save(answer);
        return toAnswerDto(saved, false);
    }

    /**
     * Run or submit code for a PROGRAMMING question in an assessment attempt.
     * - submitForGrading=false: run against custom input only (no score saved)
     * - submitForGrading=true: run against all test cases and save the submission
     */
    @Transactional
    public AssessmentCodeRunResponse runOrSubmitCode(Long assessmentId, Long attemptId, Long userId,
                                                      AssessmentCodeRunRequest request) {
        AssessmentAttempt attempt = loadAndAuthorizeAttempt(assessmentId, attemptId, userId);

        if (expireIfPastDeadline(attempt)) {
            throw new BadRequestException("This attempt has expired.");
        }
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This attempt is no longer active.");
        }

        AssessmentQuestion question = assessmentQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + request.getQuestionId()));
        if (!question.getAssessment().getId().equals(assessmentId)) {
            throw new BadRequestException("Question does not belong to this assessment.");
        }
        if (question.getQuestionType() != AssessmentQuestionType.PROGRAMMING) {
            throw new BadRequestException("This is not a programming question.");
        }
        if (question.getProgrammingQuestion() == null) {
            throw new BadRequestException("Programming question has no problem statement configured.");
        }

        ProgrammingQuestion pq = question.getProgrammingQuestion();
        AssessmentCodeRunResponse response = new AssessmentCodeRunResponse();
        response.setSubmitForGrading(request.isSubmitForGrading());

        // Compile once
        CodeExecutionService.CompileOutcome compileOutcome;
        try {
            compileOutcome = codeExecutionService.compile(request.getLanguage(), request.getCode());
        } catch (Exception e) {
            response.setCompilationError(true);
            response.setCompilationErrorMessage("Compilation failed: " + e.getMessage());
            return response;
        }

        if (!compileOutcome.success) {
            response.setCompilationError(true);
            response.setCompilationErrorMessage(compileOutcome.errorOutput);
            // Clean up
            codeExecutionService.cleanup(compileOutcome.workDir);
            return response;
        }

        if (!request.isSubmitForGrading()) {
            // Custom input run — no test case execution
            String input = request.getCustomInput() != null ? request.getCustomInput() : "";
            int timeLimit = pq.getTimeLimitMs() != null ? pq.getTimeLimitMs().intValue() : 5000;
            try {
                CodeExecutionService.RunOutcome run = codeExecutionService.run(
                        compileOutcome.workDir, compileOutcome.runCommand, input, timeLimit);
                response.setCustomOutput(run.stdout);
                response.setCustomError(run.stderr);
            } finally {
                codeExecutionService.cleanup(compileOutcome.workDir);
            }
            return response;
        }

        // Full submission — run against ALL test cases
        List<ProgrammingTestCase> testCases = programmingTestCaseRepository
                .findByProgrammingQuestionIdOrderByOrderIndexAsc(pq.getId());

        List<AssessmentCodeRunResponse.TestResult> results = new ArrayList<>();
        int passed = 0;
        long totalTimeMs = 0;

        for (int i = 0; i < testCases.size(); i++) {
            ProgrammingTestCase tc = testCases.get(i);
            AssessmentCodeRunResponse.TestResult tr = new AssessmentCodeRunResponse.TestResult();
            tr.setTestNumber(i + 1);
            tr.setHidden(Boolean.TRUE.equals(tc.getIsHidden()));

            int timeLimit = pq.getTimeLimitMs() != null ? pq.getTimeLimitMs().intValue() : 5000;
            CodeExecutionService.RunOutcome run = codeExecutionService.run(
                    compileOutcome.workDir, compileOutcome.runCommand, tc.getInput(), timeLimit);
            tr.setExecutionTimeMs(run.executionTimeMs);
            totalTimeMs += run.executionTimeMs;

            String actual = run.stdout != null ? run.stdout.trim() : "";
            String expected = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
            boolean isPassed = expected.equals(actual);
            tr.setPassed(isPassed);
            if (isPassed) passed++;

            tr.setActual(actual);
            // Only expose input/expected for non-hidden test cases
            if (!Boolean.TRUE.equals(tc.getIsHidden())) {
                tr.setInput(tc.getInput());
                tr.setExpected(tc.getExpectedOutput());
            }
            if (run.stderr != null && !run.stderr.isBlank()) tr.setError(run.stderr);

            results.add(tr);
        }

        // Clean up compiled artifacts
        codeExecutionService.cleanup(compileOutcome.workDir);

        int total = testCases.size();
        double score = total > 0
                ? ((double) passed / total) * (question.getMarks() != null ? question.getMarks() : 0)
                : 0.0;

        response.setPassedTests(passed);
        response.setTotalTests(total);
        response.setProgrammingScore(score);
        response.setTestResults(results);

        // Save submission
        AssessmentProgrammingSubmission submission = new AssessmentProgrammingSubmission();
        submission.setAttempt(attempt);
        submission.setQuestion(question);
        submission.setLanguage(request.getLanguage());
        submission.setCode(request.getCode());
        submission.setPassedTests(passed);
        submission.setTotalTests(total);
        submission.setProgrammingScore(score);
        submission.setSubmissionType(AssessmentProgrammingSubmission.SubmissionType.FULL);
        submission.setExecutionTimeMs(totalTimeMs);
        programmingSubmissionRepository.save(submission);

        return response;
    }



    /**
     * Records a proctoring violation event for the given attempt.
     */
    @Transactional
    public void recordViolation(Long assessmentId, Long attemptId, Long userId, AssessmentViolationRequest request) {
        AssessmentAttempt attempt = loadAndAuthorizeAttempt(assessmentId, attemptId, userId);
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) {
            return; // Silently ignore violations for non-active attempts
        }
        AssessmentViolation.ViolationType type;
        try {
            type = AssessmentViolation.ViolationType.valueOf(request.getViolationType().trim().toUpperCase());
        } catch (Exception e) {
            type = AssessmentViolation.ViolationType.OTHER;
        }
        AssessmentViolation violation = new AssessmentViolation();
        violation.setAttempt(attempt);
        violation.setViolationType(type);
        violation.setTimestamp(LocalDateTime.now());
        violation.setDescription(request.getDescription());
        assessmentViolationRepository.save(violation);

        // Update the count on the attempt
        attempt.setViolationCount(attempt.getViolationCount() + 1);
        assessmentAttemptRepository.save(attempt);
    }

    /**
     * Grades the MCQ answers + programming submissions, computes total score, and
     * marks the attempt COMPLETED.  All scoring is server-side — the client supplies
     * nothing except the submit request itself.
     *
     * MCQ scoring:
     *   - Correct: +marks
     *   - Wrong:   -negativeMarkingValue (if enabled)
     *   - Blank:   0
     *
     * Programming scoring:
     *   - Best FULL submission per question: (passedTests/totalTests) * questionMarks
     */
    @Transactional
    public AssessmentAttemptDto submitAttempt(Long assessmentId, Long attemptId, Long userId) {
        AssessmentAttempt attempt = loadAndAuthorizeAttempt(assessmentId, attemptId, userId);

        if (attempt.getStatus() == AssessmentAttemptStatus.COMPLETED) {
            throw new BadRequestException("This attempt has already been submitted.");
        }
        if (expireIfPastDeadline(attempt) || attempt.getStatus() == AssessmentAttemptStatus.EXPIRED) {
            throw new BadRequestException("This attempt has expired and can no longer be submitted.");
        }

        Assessment assessment = attempt.getAssessment();
        boolean negativeEnabled = Boolean.TRUE.equals(assessment.getNegativeMarkingEnabled());
        double negativeValue = assessment.getNegativeMarkingValue() != null ? assessment.getNegativeMarkingValue() : 0.0;

        // ── Grade MCQ / TRUE_FALSE answers ────────────────────────────────
        List<AssessmentAnswer> answers = assessmentAnswerRepository.findByAttemptId(attemptId);
        int mcqScore = 0;
        int correctCount = 0;
        int incorrectCount = 0;

        for (AssessmentAnswer answer : answers) {
            if (answer.getQuestion().getQuestionType() == AssessmentQuestionType.PROGRAMMING) continue;
            boolean correct = Boolean.TRUE.equals(answer.getSelectedOption().getIsCorrect());
            Integer questionMarks = answer.getQuestion().getMarks();
            int marks;
            if (correct) {
                marks = questionMarks != null ? questionMarks : 0;
                correctCount++;
            } else {
                marks = negativeEnabled ? -(int) Math.round(negativeValue) : 0;
                incorrectCount++;
            }
            answer.setIsCorrect(correct);
            answer.setMarksAwarded(marks);
            assessmentAnswerRepository.save(answer);
            mcqScore += marks;
        }
        mcqScore = Math.max(0, mcqScore); // Floor at 0

        // ── Grade Programming questions ───────────────────────────────────
        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessment.getId());
        double programmingScore = 0.0;
        int programmingAttempted = 0;
        int programmingSolved = 0;

        for (AssessmentQuestion q : questions) {
            if (q.getQuestionType() != AssessmentQuestionType.PROGRAMMING) continue;
            List<AssessmentProgrammingSubmission> subs = programmingSubmissionRepository
                    .findFullSubmissionsByAttemptAndQuestion(attemptId, q.getId());
            if (!subs.isEmpty()) {
                programmingAttempted++;
                // Best submission = highest score
                double bestScore = subs.stream()
                        .mapToDouble(s -> s.getProgrammingScore() != null ? s.getProgrammingScore() : 0.0)
                        .max().orElse(0.0);
                if (bestScore > 0) programmingSolved++;
                programmingScore += bestScore;
            }
        }

        // ── Unanswered count ──────────────────────────────────────────────
        long mcqQuestionCount = questions.stream()
                .filter(q -> q.getQuestionType() != AssessmentQuestionType.PROGRAMMING).count();
        int unansweredCount = (int) mcqQuestionCount - answers.size();
        unansweredCount = Math.max(0, unansweredCount);

        // ── Store results ─────────────────────────────────────────────────
        int totalScore = (int) Math.round(mcqScore + programmingScore);
        attempt.setScore(Math.max(0, totalScore));
        attempt.setMcqScore(mcqScore);
        attempt.setProgrammingScore(programmingScore);
        attempt.setCorrectAnswers(correctCount);
        attempt.setIncorrectAnswers(incorrectCount);
        attempt.setUnansweredCount(unansweredCount);
        attempt.setProgrammingAttempted(programmingAttempted);
        attempt.setProgrammingSolved(programmingSolved);
        attempt.setStatus(AssessmentAttemptStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());
        AssessmentAttempt saved = assessmentAttemptRepository.save(attempt);

        if (emailService != null && saved.getUser() != null) {
            final User targetUser = saved.getUser();
            final Assessment targetAssessment = assessment;
            final AssessmentAttempt targetAttempt = saved;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendAssessmentResultEmail(targetUser, targetAssessment, targetAttempt);
                } catch (Exception ex) {
                    // Email failure must never roll back completed attempt evaluation
                }
            });
        }

        return toAttemptDto(saved, userId);
    }

    // =====================================================================
    // HOST - RESULTS
    // =====================================================================

    @Transactional(readOnly = true)
    public List<AssessmentAttemptDto> getHostAssessmentAttempts(Long assessmentId, Long userId) {
        loadAndAuthorizeOwnership(assessmentId, userId);
        return assessmentAttemptRepository.findByAssessmentIdOrderByStartedAtDesc(assessmentId).stream()
                .map(a -> toAttemptDto(a, null))
                .collect(Collectors.toList());
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private AssessmentAttempt loadAndAuthorizeAttempt(Long assessmentId, Long attemptId, Long userId) {
        AssessmentAttempt attempt = assessmentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));
        if (!attempt.getAssessment().getId().equals(assessmentId)) {
            throw new ResourceNotFoundException("Attempt not found for assessment " + assessmentId);
        }
        if (!attempt.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot access another user's attempt.");
        }
        return attempt;
    }

    private boolean expireIfPastDeadline(AssessmentAttempt attempt) {
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) return false;
        LocalDateTime deadline = computeDeadline(attempt);
        if (deadline != null && !LocalDateTime.now().isBefore(deadline)) {
            attempt.setStatus(AssessmentAttemptStatus.EXPIRED);
            attempt.setCompletedAt(deadline);
            assessmentAttemptRepository.save(attempt);
            return true;
        }
        return false;
    }

    private LocalDateTime computeDeadline(AssessmentAttempt attempt) {
        if (attempt.getStartedAt() == null || attempt.getAssessment().getDurationMinutes() == null) return null;
        return attempt.getStartedAt().plusMinutes(attempt.getAssessment().getDurationMinutes());
    }

    private AssessmentQuestion validateQuestionOwnership(Long assessmentId, Long questionId) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        if (!question.getAssessment().getId().equals(assessmentId)) {
            throw new ResourceNotFoundException("Question not found for assessment " + assessmentId);
        }
        return question;
    }

    private AssessmentOption validateOptionOwnership(AssessmentQuestion question, Long optionId) {
        AssessmentOption option = assessmentOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + optionId));
        if (!option.getQuestion().getId().equals(question.getId())) {
            throw new ResourceNotFoundException("Option not found for question " + question.getId());
        }
        return option;
    }

    private void recalculateTotalMarks(Assessment assessment) {
        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessment.getId());
        int total = questions.stream().mapToInt(q -> q.getMarks() != null ? q.getMarks() : 0).sum();
        assessment.setTotalMarks(total);
        assessmentRepository.save(assessment);
    }

    private AssessmentQuestionType parseQuestionType(String type) {
        if (type == null) throw new BadRequestException("Question type is required.");
        try {
            return AssessmentQuestionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid question type: " + type);
        }
    }

    private AssessmentStatus parseAssessmentStatus(String status) {
        try {
            return AssessmentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid assessment status: " + status);
        }
    }

    // ── DTO Builders ──────────────────────────────────────────────────────

    private AssessmentDto toAssessmentDto(Assessment assessment, boolean includeQuestions, boolean includeAnswerKey) {
        AssessmentDto dto = new AssessmentDto();
        dto.setId(assessment.getId());
        dto.setTitle(assessment.getTitle());
        dto.setDescription(assessment.getDescription());
        dto.setInstructions(assessment.getInstructions());
        dto.setDurationMinutes(assessment.getDurationMinutes());
        dto.setTotalMarks(assessment.getTotalMarks());
        dto.setPassingMarks(assessment.getPassingMarks());
        dto.setStatus(assessment.getStatus().name());
        dto.setCreatedByUsername(assessment.getCreatedBy() != null ? assessment.getCreatedBy().getUsername() : null);
        dto.setHostEmail(assessment.getCreatedBy() != null ? assessment.getCreatedBy().getEmail() : null);
        dto.setHostName(assessment.getCreatedBy() != null ? assessment.getCreatedBy().getName() : null);

        String orgName = null;
        if (assessment.getCreatedBy() != null && hostVerificationService != null) {
            try {
                orgName = hostVerificationService.getOrganizationName(assessment.getCreatedBy().getId()).orElse(null);
            } catch (Exception ignored) {}
        }
        dto.setOrganizationName(orgName);

        if (assessment.getId() != null && assessmentAttemptRepository != null) {
            try {
                dto.setTotalAttempts((int) assessmentAttemptRepository.countByAssessmentId(assessment.getId()));
                dto.setCandidateCount((int) assessmentAttemptRepository.countDistinctUsersByAssessmentId(assessment.getId()));
            } catch (Exception ignored) {}
        }
        dto.setHostedByOtherUser(isHostedByOtherUser(assessment));
        dto.setCreatedAt(assessment.getCreatedAt());
        dto.setUpdatedAt(assessment.getUpdatedAt());

        // New config fields
        dto.setMaxAttempts(assessment.getMaxAttempts());
        dto.setStartTime(assessment.getStartTime());
        dto.setEndTime(assessment.getEndTime());
        dto.setNegativeMarkingEnabled(assessment.getNegativeMarkingEnabled());
        dto.setNegativeMarkingValue(assessment.getNegativeMarkingValue());
        dto.setRequireCandidateDetails(assessment.getRequireCandidateDetails());
        dto.setCandidateFieldsConfig(assessment.getCandidateFieldsConfig());

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessment.getId());
        dto.setQuestionCount(questions.size());
        if (includeQuestions) {
            dto.setQuestions(questions.stream().map(q -> toQuestionDto(q, includeAnswerKey, null)).collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * @param question         the question entity
     * @param includeAnswerKey whether to include isCorrect / explanation
     * @param attemptId        if non-null, also include the candidate's latest programming submission for this question
     */
    private AssessmentQuestionDto toQuestionDto(AssessmentQuestion question, boolean includeAnswerKey, Long attemptId) {
        AssessmentQuestionDto dto = new AssessmentQuestionDto();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType().name());
        dto.setMarks(question.getMarks());
        dto.setOrderIndex(question.getOrderIndex());
        dto.setExplanation(includeAnswerKey ? question.getExplanation() : null);

        if (question.getQuestionType() == AssessmentQuestionType.PROGRAMMING) {
            // For programming questions, build programming question DTO (never MCQ options)
            dto.setOptions(List.of());
            if (question.getProgrammingQuestion() != null) {
                dto.setProgrammingQuestion(toProgrammingQuestionDto(question.getProgrammingQuestion(), includeAnswerKey));
            }
            if (attemptId != null) {
                List<AssessmentProgrammingSubmission> subs = programmingSubmissionRepository
                        .findFullSubmissionsByAttemptAndQuestion(attemptId, question.getId());
                if (!subs.isEmpty()) {
                    AssessmentProgrammingSubmission best = subs.stream()
                            .max(java.util.Comparator.comparingDouble(s -> s.getProgrammingScore() != null ? s.getProgrammingScore() : 0))
                            .orElse(subs.get(0));
                    AssessmentQuestionDto.ProgrammingSubmissionResultDto res = new AssessmentQuestionDto.ProgrammingSubmissionResultDto();
                    res.setId(best.getId());
                    res.setPassedTests(best.getPassedTests());
                    res.setTotalTests(best.getTotalTests());
                    res.setProgrammingScore(best.getProgrammingScore());
                    res.setExecutionTimeMs(best.getExecutionTimeMs());
                    res.setLanguage(best.getLanguage());
                    dto.setLatestSubmission(res);
                }
            }
        } else {
            List<AssessmentOption> options = assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(question.getId());
            dto.setOptions(options.stream().map(o -> toOptionDto(o, includeAnswerKey)).collect(Collectors.toList()));
        }
        return dto;
    }

    private ProgrammingQuestionDto toProgrammingQuestionDto(ProgrammingQuestion pq, boolean includeHostDetails) {
        ProgrammingQuestionDto dto = new ProgrammingQuestionDto();
        dto.setId(pq.getId());
        dto.setProblemStatement(pq.getProblemStatement());
        dto.setInputFormat(pq.getInputFormat());
        dto.setOutputFormat(pq.getOutputFormat());
        dto.setConstraints(pq.getConstraints());
        dto.setSampleInput(pq.getSampleInput());
        dto.setSampleOutput(pq.getSampleOutput());
        dto.setSupportedLanguages(pq.getSupportedLanguages());
        dto.setStarterCodeJava(pq.getStarterCodeJava());
        dto.setStarterCodePython(pq.getStarterCodePython());
        dto.setStarterCodeCpp(pq.getStarterCodeCpp());
        dto.setStarterCodeJavascript(pq.getStarterCodeJavascript());
        dto.setTimeLimitMs(pq.getTimeLimitMs());
        dto.setMemoryLimitMb(pq.getMemoryLimitMb());

        List<ProgrammingTestCase> testCases = programmingTestCaseRepository
                .findByProgrammingQuestionIdOrderByOrderIndexAsc(pq.getId());
        dto.setTotalTestCases(testCases.size());
        dto.setHiddenTestCases((int) testCases.stream().filter(tc -> Boolean.TRUE.equals(tc.getIsHidden())).count());
        dto.setSampleTestCases((int) testCases.stream().filter(tc -> !Boolean.TRUE.equals(tc.getIsHidden())).count());
        return dto;
    }

    private ProgrammingTestCaseDto toTestCaseDto(ProgrammingTestCase tc) {
        ProgrammingTestCaseDto dto = new ProgrammingTestCaseDto();
        dto.setId(tc.getId());
        dto.setInput(tc.getInput());
        dto.setExpectedOutput(tc.getExpectedOutput());
        dto.setIsHidden(tc.getIsHidden());
        dto.setOrderIndex(tc.getOrderIndex());
        dto.setDescription(tc.getDescription());
        return dto;
    }

    private AssessmentOptionDto toOptionDto(AssessmentOption option, boolean includeAnswerKey) {
        AssessmentOptionDto dto = new AssessmentOptionDto();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setOrderIndex(option.getOrderIndex());
        dto.setIsCorrect(includeAnswerKey ? option.getIsCorrect() : null);
        return dto;
    }

    private AssessmentAnswerDto toAnswerDto(AssessmentAnswer answer, boolean includeAnswerKey) {
        AssessmentAnswerDto dto = new AssessmentAnswerDto();
        dto.setId(answer.getId());
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setSelectedOptionId(answer.getSelectedOption().getId());
        dto.setIsCorrect(includeAnswerKey ? answer.getIsCorrect() : null);
        dto.setMarksAwarded(includeAnswerKey ? answer.getMarksAwarded() : null);
        return dto;
    }

    private AssessmentAttemptDto toAttemptDto(AssessmentAttempt attempt, Long currentUserId) {
        Assessment assessment = attempt.getAssessment();
        boolean includeAnswerKey = attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS;

        AssessmentAttemptDto dto = new AssessmentAttemptDto();
        dto.setId(attempt.getId());
        dto.setAssessmentId(assessment.getId());
        dto.setAssessmentTitle(assessment.getTitle());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());
        dto.setDeadlineAt(computeDeadline(attempt));
        dto.setScore(attempt.getScore());
        dto.setMcqScore(attempt.getMcqScore());
        dto.setProgrammingScore(attempt.getProgrammingScore());
        dto.setTotalQuestions(attempt.getTotalQuestions());
        dto.setCorrectAnswers(attempt.getCorrectAnswers());
        dto.setIncorrectAnswers(attempt.getIncorrectAnswers());
        dto.setUnansweredCount(attempt.getUnansweredCount());
        dto.setProgrammingAttempted(attempt.getProgrammingAttempted());
        dto.setProgrammingSolved(attempt.getProgrammingSolved());
        dto.setViolationCount(attempt.getViolationCount());
        dto.setAttemptNumber(attempt.getAttemptNumber());
        dto.setMaxAttempts(assessment.getMaxAttempts());
        dto.setStatus(attempt.getStatus().name());
        dto.setTotalMarks(assessment.getTotalMarks());
        dto.setPassingMarks(assessment.getPassingMarks());
        dto.setPassed(includeAnswerKey && attempt.getScore() != null && assessment.getPassingMarks() != null
                ? attempt.getScore() >= assessment.getPassingMarks() : null);
        dto.setCandidateUsername(attempt.getUser() != null ? attempt.getUser().getUsername() : null);
        dto.setCandidateFullName(attempt.getUser() != null ? attempt.getUser().getName() : null);
        dto.setCandidateEmail(attempt.getUser() != null ? attempt.getUser().getEmail() : null);

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessment.getId());
        Long attemptIdForSubs = attempt.getId();
        dto.setQuestions(questions.stream()
                .map(q -> toQuestionDto(q, includeAnswerKey, attemptIdForSubs))
                .collect(Collectors.toList()));

        List<AssessmentAnswer> answers = assessmentAnswerRepository.findByAttemptId(attempt.getId());
        dto.setAnswers(answers.stream().map(a -> toAnswerDto(a, includeAnswerKey)).collect(Collectors.toList()));

        // Attempt history for multi-attempt assessments
        if (currentUserId != null && assessment.getMaxAttempts() != null && assessment.getMaxAttempts() > 1) {
            List<AssessmentAttempt> allAttempts = assessmentAttemptRepository
                    .findByAssessmentIdAndUserIdOrdered(assessment.getId(), currentUserId);
            if (allAttempts.size() > 1) {
                List<AssessmentAttemptDto.AttemptSummaryDto> history = allAttempts.stream().map(a -> {
                    AssessmentAttemptDto.AttemptSummaryDto s = new AssessmentAttemptDto.AttemptSummaryDto();
                    s.setId(a.getId());
                    s.setAttemptNumber(a.getAttemptNumber());
                    s.setStatus(a.getStatus().name());
                    s.setScore(a.getScore());
                    s.setStartedAt(a.getStartedAt());
                    s.setCompletedAt(a.getCompletedAt());
                    boolean done = a.getStatus() != AssessmentAttemptStatus.IN_PROGRESS;
                    s.setPassed(done && a.getScore() != null && assessment.getPassingMarks() != null
                            ? a.getScore() >= assessment.getPassingMarks() : null);
                    return s;
                }).collect(Collectors.toList());
                dto.setAttemptHistory(history);
            }
        }

        return dto;
    }
}
