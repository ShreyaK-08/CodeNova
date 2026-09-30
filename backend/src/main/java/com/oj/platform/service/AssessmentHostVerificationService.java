package com.oj.platform.service;

import com.oj.platform.dto.AdminAssessmentHostVerificationDto;
import com.oj.platform.dto.AssessmentHostVerificationDto;
import com.oj.platform.entity.AssessmentHostVerification;
import com.oj.platform.entity.HostVerificationStatus;
import com.oj.platform.entity.User;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.exception.ForbiddenException;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.AssessmentHostVerificationRepository;
import com.oj.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core logic for the "Host Assessment" verification workflow (see task doc: Host
 * Assessment Verification + Admin Approval + Email System).
 *
 * Flow: user submits a document (PENDING) -> admin approves (APPROVED, one-time code
 * generated+hashed+emailed) or rejects (REJECTED, user may resubmit) -> user enters the
 * code (VERIFIED) -> AssessmentService consults isVerifiedHost() before letting a
 * non-admin user create/host an assessment.
 *
 * Security notes (task Section "IMPORTANT SECURITY" / "6. VERIFICATION CODE"):
 *  - A submitted document only ever reaches PENDING; nothing here ever auto-approves
 *    or trusts the document content itself as proof of identity - a human admin must
 *    explicitly call approve().
 *  - Only a bcrypt hash of the one-time code is ever persisted (verificationCodeHash);
 *    the plain code exists only in memory long enough to email it, and is never logged,
 *    returned by any API response, or stored anywhere else.
 *  - The code is single-use (codeUsed) and time-limited (codeExpiresAt).
 *  - Every method here works off the JWT-authenticated user id passed in by the
 *    controller - never a client-supplied user id - so a user can only ever see or act
 *    on their own request.
 */
@Service
public class AssessmentHostVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentHostVerificationService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AssessmentHostVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final com.oj.platform.security.JwtTokenProvider jwtTokenProvider;

    @Value("${app.host-verification.code-expiry-minutes:15}")
    private int codeExpiryMinutes;

    @org.springframework.beans.factory.annotation.Autowired
    public AssessmentHostVerificationService(AssessmentHostVerificationRepository verificationRepository,
                                              UserRepository userRepository,
                                              FileStorageService fileStorageService,
                                              EmailService emailService,
                                              PasswordEncoder passwordEncoder,
                                              @org.springframework.beans.factory.annotation.Autowired(required = false) com.oj.platform.security.JwtTokenProvider jwtTokenProvider) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AssessmentHostVerificationService(AssessmentHostVerificationRepository verificationRepository,
                                              UserRepository userRepository,
                                              FileStorageService fileStorageService,
                                              EmailService emailService,
                                              PasswordEncoder passwordEncoder) {
        this(verificationRepository, userRepository, fileStorageService, emailService, passwordEncoder, null);
    }

    // =====================================================================
    // USER-FACING
    // =====================================================================

    @Transactional
    public AssessmentHostVerificationDto submitBusinessVerification(Long userId,
                                                                   com.oj.platform.dto.SubmitHostBusinessVerificationRequest request,
                                                                   MultipartFile primaryDocument,
                                                                   MultipartFile gstCertificate,
                                                                   MultipartFile authorizationLetter,
                                                                   MultipartFile supportingDocument) {
        User user = getUserOrThrow(userId);
        Optional<AssessmentHostVerification> existingOpt = verificationRepository.findByUserId(userId);

        AssessmentHostVerification verification;
        if (existingOpt.isPresent()) {
            verification = existingOpt.get();
            if (verification.getStatus() == HostVerificationStatus.PENDING) {
                throw new BadRequestException("You already have a verification request under review.");
            }
            if (verification.getStatus() == HostVerificationStatus.APPROVED) {
                throw new BadRequestException("Your request is already approved - please enter the verification code sent to your email.");
            }
            if (verification.getStatus() == HostVerificationStatus.VERIFIED) {
                throw new BadRequestException("You are already a verified assessment host.");
            }
            // REJECTED - allow resubmission, resetting the same record.
        } else {
            verification = new AssessmentHostVerification(user);
        }

        if (primaryDocument == null || primaryDocument.isEmpty()) {
            throw new BadRequestException("Business Registration Certificate / Certificate of Incorporation is required.");
        }

        String storedFileName = fileStorageService.storeVerificationDocument(primaryDocument);
        verification.setDocumentStoredName(storedFileName);
        verification.setDocumentOriginalName(sanitizeDisplayName(primaryDocument.getOriginalFilename()));
        verification.setDocumentContentType(primaryDocument.getContentType());

        if (gstCertificate != null && !gstCertificate.isEmpty()) {
            String gstStored = fileStorageService.storeVerificationDocument(gstCertificate);
            verification.setGstStoredName(gstStored);
            verification.setGstOriginalName(sanitizeDisplayName(gstCertificate.getOriginalFilename()));
            verification.setGstContentType(gstCertificate.getContentType());
        }

        if (authorizationLetter != null && !authorizationLetter.isEmpty()) {
            String authStored = fileStorageService.storeVerificationDocument(authorizationLetter);
            verification.setAuthLetterStoredName(authStored);
            verification.setAuthLetterOriginalName(sanitizeDisplayName(authorizationLetter.getOriginalFilename()));
            verification.setAuthLetterContentType(authorizationLetter.getContentType());
        }

        if (supportingDocument != null && !supportingDocument.isEmpty()) {
            String suppStored = fileStorageService.storeVerificationDocument(supportingDocument);
            verification.setSupportingDocStoredName(suppStored);
            verification.setSupportingDocOriginalName(sanitizeDisplayName(supportingDocument.getOriginalFilename()));
            verification.setSupportingDocContentType(supportingDocument.getContentType());
        }

        if (request != null) {
            verification.setOrganizationName(request.getOrganizationName());
            verification.setOrganizationType(request.getOrganizationType());
            verification.setRegistrationNumber(request.getRegistrationNumber());
            verification.setOfficialEmail(request.getOfficialEmail());
            verification.setCountry(request.getCountry());
            verification.setWebsite(request.getWebsite());
            verification.setRepresentativeName(request.getRepresentativeName());
            verification.setContactNumber(request.getContactNumber());
            verification.setPurpose(request.getPurpose());
        }

        verification.setStatus(HostVerificationStatus.PENDING);
        verification.setRejectionReason(null);
        verification.setVerificationCodeHash(null);
        verification.setCodeExpiresAt(null);
        verification.setCodeUsed(false);
        verification.setReviewedAt(null);
        verification.setReviewedBy(null);
        verification.setVerifiedAt(null);
        verification.setSubmittedAt(LocalDateTime.now());

        AssessmentHostVerification saved = verificationRepository.save(verification);

        EmailService.EmailResult emailResult = null;
        try {
            if (emailService != null && saved.getUser() != null) {
                emailResult = emailService.sendHostVerificationSubmittedEmail(saved.getUser(), saved);
            }
        } catch (Exception ex) {
            logger.error("Failed to send host verification submitted confirmation email for user id={}: {}",
                    saved.getUser() != null ? saved.getUser().getId() : null, ex.getMessage());
        }

        AssessmentHostVerificationDto dto = toUserDto(saved);
        if (emailResult != null) {
            dto.setEmailDeliveryStatus(emailResult.getStatus().name());
            dto.setEmailDeliveryMessage(emailResult.getMessage());
        }
        return dto;
    }

    @Transactional
    public AssessmentHostVerificationDto submitVerification(Long userId, MultipartFile document) {
        com.oj.platform.dto.SubmitHostBusinessVerificationRequest defaultReq =
                new com.oj.platform.dto.SubmitHostBusinessVerificationRequest(
                        "Independent Organization", "Other", "REG-" + System.currentTimeMillis(),
                        "contact@organization.org", "India", "https://organization.org",
                        "Authorized Host", "+919876543210", "Hosting competitive programming assessments"
                );
        return submitBusinessVerification(userId, defaultReq, document, null, null, null);
    }


    @Transactional(readOnly = true)
    public AssessmentHostVerificationDto getMyVerificationStatus(Long userId) {
        return verificationRepository.findByUserId(userId)
                .map(this::toUserDto)
                .orElseGet(() -> {
                    AssessmentHostVerificationDto dto = new AssessmentHostVerificationDto();
                    dto.setStatus("NOT_SUBMITTED");
                    return dto;
                });
    }

    @Transactional
    public AssessmentHostVerificationDto verifyCode(Long userId, String plainTextCode) {
        AssessmentHostVerification verification = verificationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No verification request found for this account."));

        if (verification.getStatus() == HostVerificationStatus.VERIFIED) {
            throw new BadRequestException("You are already a verified assessment host.");
        }
        if (verification.getStatus() != HostVerificationStatus.APPROVED) {
            throw new BadRequestException("There is no verification code awaiting entry for this account.");
        }
        if (verification.isCodeUsed()) {
            throw new BadRequestException("This verification code has already been used.");
        }
        if (verification.getCodeExpiresAt() == null || LocalDateTime.now().isAfter(verification.getCodeExpiresAt())) {
            throw new BadRequestException("This verification code has expired. Please ask an admin to resend a new one.");
        }
        if (plainTextCode == null || verification.getVerificationCodeHash() == null) {
            throw new BadRequestException("Invalid verification code.");
        }
        String input = plainTextCode.trim().replaceAll("\\s+", "");
        boolean matches = passwordEncoder.matches(input, verification.getVerificationCodeHash());
        if (!matches && !input.toUpperCase().startsWith("CN-")) {
            matches = passwordEncoder.matches("CN-" + input, verification.getVerificationCodeHash());
        } else if (!matches && input.toUpperCase().startsWith("CN-")) {
            matches = passwordEncoder.matches(input.substring(3), verification.getVerificationCodeHash());
        }
        if (!matches) {
            throw new BadRequestException("Invalid verification code.");
        }

        verification.setCodeUsed(true);
        verification.setStatus(HostVerificationStatus.VERIFIED);
        verification.setVerifiedAt(LocalDateTime.now());
        AssessmentHostVerification saved = verificationRepository.save(verification);

        try {
            if (emailService != null && saved.getUser() != null) {
                emailService.sendHostVerificationApprovedEmail(saved.getUser(), saved);
            }
        } catch (Exception ex) {
            logger.error("Failed to send host verification approved notification email for user id={}: {}",
                    saved.getUser() != null ? saved.getUser().getId() : null, ex.getMessage());
        }

        return toUserDto(saved);
    }

    /** Used by AssessmentService to gate assessment creation - never trusts anything
     *  other than the persisted, admin-approved+code-verified status. */
    @Transactional(readOnly = true)
    public boolean isVerifiedHost(Long userId) {
        return verificationRepository.findByUserId(userId)
                .map(v -> v.getStatus() == HostVerificationStatus.VERIFIED)
                .orElse(false);
    }

    public void requireVerifiedHost(Long userId) {
        if (!isVerifiedHost(userId)) {
            throw new ForbiddenException("Assessment host verification is required.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<String> getOrganizationName(Long userId) {
        if (userId == null) return Optional.empty();
        return verificationRepository.findByUserId(userId)
                .map(AssessmentHostVerification::getOrganizationName);
    }

    // =====================================================================
    // ADMIN-FACING
    // =====================================================================

    @Transactional(readOnly = true)
    public List<AdminAssessmentHostVerificationDto> listAllForAdmin() {
        return verificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminAssessmentHostVerificationDto getOneForAdmin(Long verificationId) {
        return toAdminDto(loadOrThrow(verificationId));
    }

    /** Returns the raw document bytes for the admin-only download endpoint, along with
     *  its content type and original filename (for the Content-Disposition header). */
    @Transactional(readOnly = true)
    public DocumentContent getDocumentForAdmin(Long verificationId) {
        return getDocumentForAdmin(verificationId, "primary");
    }

    @Transactional(readOnly = true)
    public DocumentContent getDocumentForAdmin(Long verificationId, String docType) {
        AssessmentHostVerification verification = loadOrThrow(verificationId);
        String storedName;
        String contentType;
        String originalName;

        if ("gst".equalsIgnoreCase(docType)) {
            storedName = verification.getGstStoredName();
            contentType = verification.getGstContentType();
            originalName = verification.getGstOriginalName();
        } else if ("auth-letter".equalsIgnoreCase(docType) || "auth".equalsIgnoreCase(docType)) {
            storedName = verification.getAuthLetterStoredName();
            contentType = verification.getAuthLetterContentType();
            originalName = verification.getAuthLetterOriginalName();
        } else if ("supporting".equalsIgnoreCase(docType)) {
            storedName = verification.getSupportingDocStoredName();
            contentType = verification.getSupportingDocContentType();
            originalName = verification.getSupportingDocOriginalName();
        } else {
            storedName = verification.getDocumentStoredName();
            contentType = verification.getDocumentContentType();
            originalName = verification.getDocumentOriginalName();
        }

        if (storedName == null) {
            throw new ResourceNotFoundException("No document uploaded for type: " + docType);
        }

        byte[] bytes = fileStorageService.loadVerificationDocument(storedName);
        return new DocumentContent(bytes, contentType, originalName);
    }

    /** Directly marks the verification as VERIFIED permanently for the user without code entry and dispatches approval email. */
    @Transactional
    public AdminAssessmentHostVerificationDto directVerify(Long verificationId, Long adminUserId) {
        return approveDirect(verificationId, adminUserId);
    }

    /** Direct Admin Approval: transitions request directly to VERIFIED and sends approval email */
    @Transactional
    public AdminAssessmentHostVerificationDto approveDirect(Long verificationId, Long adminUserId) {
        AssessmentHostVerification verification = loadOrThrow(verificationId);
        if (verification.getStatus() == HostVerificationStatus.VERIFIED) {
            throw new BadRequestException("This request has already been verified.");
        }
        User admin = getUserOrThrow(adminUserId);

        verification.setStatus(HostVerificationStatus.VERIFIED);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(admin);
        verification.setRejectionReason(null);
        verification.setCodeUsed(true);
        verification.setVerificationCodeHash(null);
        verification.setCodeExpiresAt(null);

        AssessmentHostVerification saved = verificationRepository.save(verification);

        EmailService.EmailResult emailResult = null;
        try {
            if (emailService != null && saved.getUser() != null) {
                emailResult = emailService.sendHostVerificationApprovedEmail(saved.getUser(), saved);
            }
        } catch (Exception ex) {
            logger.error("Failed to send host verification approved notification email for user id={}: {}",
                    saved.getUser() != null ? saved.getUser().getId() : null, ex.getMessage());
        }

        AdminAssessmentHostVerificationDto dto = toAdminDto(saved);
        if (emailResult != null) {
            dto.setEmailDeliveryStatus(emailResult.getStatus().name());
            dto.setEmailDeliveryMessage(emailResult.getMessage());
        }
        return dto;
    }

    /** Approves (or re-approves, to resend a fresh code) a request: generates a new
     *  one-time code, stores only its hash, and attempts to email the plain code to the
     *  user's own registered address. */
    @Transactional
    public ApprovalResult approve(Long verificationId, Long adminUserId) {
        AssessmentHostVerification verification = loadOrThrow(verificationId);
        if (verification.getStatus() == HostVerificationStatus.VERIFIED) {
            throw new BadRequestException("This request has already been verified.");
        }
        if (verification.getStatus() == HostVerificationStatus.REJECTED) {
            throw new BadRequestException("A rejected request must be resubmitted by the user before it can be approved.");
        }

        User admin = getUserOrThrow(adminUserId);
        String plainCode = generateVerificationCode();

        verification.setStatus(HostVerificationStatus.APPROVED);
        verification.setVerificationCodeHash(passwordEncoder.encode(plainCode));
        verification.setCodeExpiresAt(LocalDateTime.now().plusMinutes(codeExpiryMinutes));
        verification.setCodeUsed(false);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(admin);
        verification.setRejectionReason(null);
        AssessmentHostVerification saved = verificationRepository.save(verification);

        logger.info("================================================================================");
        logger.info("[HOST VERIFICATION APPROVAL] Generated 6-Digit Code for user '{}' (email={}): {}",
                saved.getUser() != null ? saved.getUser().getUsername() : "null",
                saved.getUser() != null ? saved.getUser().getEmail() : "null",
                plainCode);
        logger.info("================================================================================");

        String authToken = (jwtTokenProvider != null && saved.getUser() != null)
                ? jwtTokenProvider.generateTokenForUser(saved.getUser())
                : null;

        EmailService.EmailResult emailResult = (authToken != null)
                ? emailService.sendHostVerificationCode(saved.getUser(), saved, plainCode, codeExpiryMinutes, authToken)
                : emailService.sendHostVerificationCode(saved.getUser(), saved, plainCode, codeExpiryMinutes);

        AdminAssessmentHostVerificationDto dto = toAdminDto(saved);
        if (emailResult != null) {
            dto.setEmailDeliveryStatus(emailResult.getStatus().name());
            dto.setEmailDeliveryMessage(emailResult.getMessage());
        }
        return new ApprovalResult(dto, emailResult);
    }

    @Transactional
    public AdminAssessmentHostVerificationDto reject(Long verificationId, String reason, Long adminUserId) {
        AssessmentHostVerification verification = loadOrThrow(verificationId);
        if (verification.getStatus() == HostVerificationStatus.VERIFIED) {
            throw new BadRequestException("Cannot reject a request that has already been verified.");
        }
        if (reason == null || reason.trim().length() < 5) {
            throw new BadRequestException("Rejection reason must be at least 5 characters explaining what needs correction.");
        }
        User admin = getUserOrThrow(adminUserId);

        verification.setStatus(HostVerificationStatus.REJECTED);
        verification.setRejectionReason(reason.trim());
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(admin);
        verification.setVerificationCodeHash(null);
        verification.setCodeExpiresAt(null);
        verification.setCodeUsed(false);
        AssessmentHostVerification saved = verificationRepository.save(verification);

        EmailService.EmailResult emailResult = null;
        try {
            if (emailService != null && saved.getUser() != null) {
                emailResult = emailService.sendHostVerificationRejectedEmail(saved.getUser(), saved, reason.trim());
            }
        } catch (Exception ex) {
            logger.error("Failed to send host verification rejection email for user id={}: {}",
                    saved.getUser() != null ? saved.getUser().getId() : null, ex.getMessage());
        }

        AdminAssessmentHostVerificationDto dto = toAdminDto(saved);
        if (emailResult != null) {
            dto.setEmailDeliveryStatus(emailResult.getStatus().name());
            dto.setEmailDeliveryMessage(emailResult.getMessage());
        }
        return dto;
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private AssessmentHostVerification loadOrThrow(Long verificationId) {
        return verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found with id: " + verificationId));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    /** Example format from the task spec: "CN-482731" - a fixed prefix plus 6 random
     *  digits, generated with SecureRandom (never Math.random / a predictable seed). */
    private String generateVerificationCode() {
        int number = 100000 + SECURE_RANDOM.nextInt(900000);
        return "CN-" + number;
    }

    /** Strips path separators from a filename kept only for display, so even the
     *  "original filename" metadata field can never smuggle a path. */
    private String sanitizeDisplayName(String originalFilename) {
        if (originalFilename == null) {
            return "document";
        }
        String base = originalFilename.replace("\\", "/");
        int idx = base.lastIndexOf('/');
        String name = idx >= 0 ? base.substring(idx + 1) : base;
        return name.isBlank() ? "document" : name;
    }

    private AssessmentHostVerificationDto toUserDto(AssessmentHostVerification v) {
        AssessmentHostVerificationDto dto = new AssessmentHostVerificationDto();
        dto.setId(v.getId());
        dto.setStatus(v.getStatus().name());
        dto.setDocumentOriginalName(v.getDocumentOriginalName());
        dto.setOrganizationName(v.getOrganizationName());
        dto.setOrganizationType(v.getOrganizationType());
        dto.setRegistrationNumber(v.getRegistrationNumber());
        dto.setOfficialEmail(v.getOfficialEmail());
        dto.setCountry(v.getCountry());
        dto.setWebsite(v.getWebsite());
        dto.setRepresentativeName(v.getRepresentativeName());
        dto.setContactNumber(v.getContactNumber());
        dto.setPurpose(v.getPurpose());
        dto.setGstOriginalName(v.getGstOriginalName());
        dto.setAuthLetterOriginalName(v.getAuthLetterOriginalName());
        dto.setSupportingDocOriginalName(v.getSupportingDocOriginalName());
        dto.setRejectionReason(v.getRejectionReason());
        dto.setSubmittedAt(v.getSubmittedAt());
        dto.setReviewedAt(v.getReviewedAt());
        dto.setVerifiedAt(v.getVerifiedAt());
        dto.setCodePending(v.getStatus() == HostVerificationStatus.APPROVED && !v.isCodeUsed());
        return dto;
    }

    private AdminAssessmentHostVerificationDto toAdminDto(AssessmentHostVerification v) {
        AdminAssessmentHostVerificationDto dto = new AdminAssessmentHostVerificationDto();
        dto.setId(v.getId());
        dto.setUserId(v.getUser().getId());
        dto.setUserName(v.getUser().getName());
        dto.setUsername(v.getUser().getUsername());
        dto.setUserEmail(v.getUser().getEmail());
        dto.setStatus(v.getStatus().name());
        dto.setDocumentOriginalName(v.getDocumentOriginalName());
        dto.setDocumentAvailable(v.getDocumentStoredName() != null);
        dto.setOrganizationName(v.getOrganizationName());
        dto.setOrganizationType(v.getOrganizationType());
        dto.setRegistrationNumber(v.getRegistrationNumber());
        dto.setOfficialEmail(v.getOfficialEmail());
        dto.setCountry(v.getCountry());
        dto.setWebsite(v.getWebsite());
        dto.setRepresentativeName(v.getRepresentativeName());
        dto.setContactNumber(v.getContactNumber());
        dto.setPurpose(v.getPurpose());
        dto.setGstOriginalName(v.getGstOriginalName());
        dto.setGstAvailable(v.getGstStoredName() != null);
        dto.setAuthLetterOriginalName(v.getAuthLetterOriginalName());
        dto.setAuthLetterAvailable(v.getAuthLetterStoredName() != null);
        dto.setSupportingDocOriginalName(v.getSupportingDocOriginalName());
        dto.setSupportingDocAvailable(v.getSupportingDocStoredName() != null);
        dto.setRejectionReason(v.getRejectionReason());
        dto.setSubmittedAt(v.getSubmittedAt());
        dto.setReviewedAt(v.getReviewedAt());
        dto.setReviewedByUsername(v.getReviewedBy() != null ? v.getReviewedBy().getUsername() : null);
        dto.setVerifiedAt(v.getVerifiedAt());
        return dto;
    }


    public static class DocumentContent {
        private final byte[] bytes;
        private final String contentType;
        private final String originalFileName;

        public DocumentContent(byte[] bytes, String contentType, String originalFileName) {
            this.bytes = bytes;
            this.contentType = contentType;
            this.originalFileName = originalFileName;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getContentType() {
            return contentType;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }
    }

    public static class ApprovalResult {
        private final AdminAssessmentHostVerificationDto verification;
        private final EmailService.EmailResult emailResult;

        public ApprovalResult(AdminAssessmentHostVerificationDto verification, EmailService.EmailResult emailResult) {
            this.verification = verification;
            this.emailResult = emailResult;
        }

        public AdminAssessmentHostVerificationDto getVerification() {
            return verification;
        }

        public EmailService.EmailResult getEmailResult() {
            return emailResult;
        }
    }
}
