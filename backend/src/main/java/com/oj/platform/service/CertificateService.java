package com.oj.platform.service;

import com.oj.platform.dto.CertificateDto;
import com.oj.platform.dto.CertificateProgressDto;
import com.oj.platform.dto.CertificateVerificationDto;
import com.oj.platform.entity.Certificate;
import com.oj.platform.entity.User;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.CertificateRepository;
import com.oj.platform.repository.SubmissionRepository;
import com.oj.platform.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Certificate milestone logic (Part 17): every 50 distinct ACCEPTED problems earns a
 * certificate. This service is the single source of truth for calculating milestones,
 * creating certificates (idempotently), generating their PDF, and verifying them.
 *
 * IMPORTANT: certificates are only ever created via checkAndAwardMilestones(), which
 * recalculates the user's real distinct-solved-problem count from the submissions table
 * every time it runs - there is no path in this codebase that inserts a Certificate row
 * directly. This is what "the certificate must be generated through the actual
 * certificate logic, not inserted as a row" means in practice.
 */
@Service
public class CertificateService {

    private static final int[] OVERALL_MILESTONES = {1, 10, 25, 50, 100, 150, 200, 250};
    public static final int LANGUAGE_MILESTONE_THRESHOLD = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no ambiguous chars

    private final CertificateRepository certificateRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CertificateService(CertificateRepository certificateRepository,
                               SubmissionRepository submissionRepository,
                               UserRepository userRepository) {
        this(certificateRepository, submissionRepository, userRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CertificateService(CertificateRepository certificateRepository,
                               SubmissionRepository submissionRepository,
                               UserRepository userRepository,
                               @org.springframework.beans.factory.annotation.Autowired(required = false) EmailService emailService) {
        this.certificateRepository = certificateRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Checks and awards both overall milestone certificates and language-specific certificates.
     * Idempotent and safe to call after any accepted submission or data initialization.
     *
     * @return the list of newly-created certificates this call (empty if none earned).
     */
    @Transactional
    public List<Certificate> checkAndAwardMilestones(Long userId) {
        List<Certificate> newlyAwarded = new ArrayList<>();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 1. Overall Milestones
        long totalSolved = submissionRepository.countDistinctSolvedProblemsByUser(userId);
        for (int milestone : OVERALL_MILESTONES) {
            if (totalSolved >= milestone) {
                String title = milestone == 1 ? "First Problem Solved" : milestone + " Problems Solved";
                if (!certificateRepository.existsByUserIdAndTitle(userId, title)) {
                    newlyAwarded.add(createOverallCertificate(user, milestone, title));
                }
            }
        }

        // 2. Language-Specific Certifications
        List<Object[]> langRows = submissionRepository.countDistinctSolvedProblemsGroupedByLanguage(userId);
        for (Object[] row : langRows) {
            String rawLang = (String) row[0];
            Long count = (Long) row[1];
            if (rawLang != null && count != null && count >= LANGUAGE_MILESTONE_THRESHOLD) {
                String displayLang = normalizeLanguageName(rawLang);
                String title = displayLang + " Problem Solver";
                if (!certificateRepository.existsByUserIdAndTitle(userId, title)) {
                    newlyAwarded.add(createLanguageCertificate(user, count.intValue(), displayLang, title));
                }
            }
        }

        // 3. Dispatch Email Notifications for newly earned certificates
        if (emailService != null && !newlyAwarded.isEmpty()) {
            for (Certificate cert : newlyAwarded) {
                try {
                    emailService.sendCertificateIssuedEmail(user, cert);
                } catch (Exception ex) {
                    // Certificate persistence must not fail due to mail delivery issues
                }
            }
        }

        return newlyAwarded;
    }

    public static String normalizeLanguageName(String raw) {
        if (raw == null || raw.isBlank()) return "Unknown";
        String upper = raw.trim().toUpperCase();
        switch (upper) {
            case "CPP":
            case "C++":
                return "C++";
            case "C":
                return "C";
            case "JAVA":
                return "Java";
            case "PYTHON":
            case "PYTHON3":
            case "PY":
                return "Python";
            case "JAVASCRIPT":
            case "JS":
            case "NODE":
                return "JavaScript";
            case "SQL":
                return "SQL";
            default:
                return raw.substring(0, 1).toUpperCase() + raw.substring(1).toLowerCase();
        }
    }

    private Certificate createOverallCertificate(User user, int milestone, String title) {
        String certNumber = generateCertificateNumber();
        String level = achievementLevelFor(milestone);
        String description = String.format(
                "This certifies that %s has successfully solved %d coding problems on the CodeNova platform, "
                        + "achieving %s level status.",
                user.getName(), milestone, level);
        Certificate cert = new Certificate(user, milestone, certNumber, title, level,
                user.getName(), description, generateVerificationCode(), null, "OVERALL");
        return certificateRepository.save(cert);
    }

    private Certificate createLanguageCertificate(User user, int count, String language, String title) {
        String certNumber = generateCertificateNumber();
        String level = "Specialist";
        String description = String.format(
                "This certifies that %s has demonstrated proficiency by successfully solving %d problems in %s on the CodeNova platform.",
                user.getName(), count, language);
        Certificate cert = new Certificate(user, count, certNumber, title, level,
                user.getName(), description, generateVerificationCode(), language, "LANGUAGE");
        return certificateRepository.save(cert);
    }

    private String generateCertificateNumber() {
        int year = LocalDateTime.now().getYear();
        long seq = certificateRepository.count() + 1;
        return String.format("CN-%d-%06d", year, seq);
    }

    private String generateVerificationCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("CNV-");
            for (int i = 0; i < 12; i++) {
                if (i > 0 && i % 4 == 0) sb.append('-');
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (certificateRepository.findByVerificationCode(code).isPresent());
        return code;
    }

    public static String achievementLevelFor(int milestone) {
        if (milestone >= 250) return "Diamond";
        if (milestone >= 200) return "Platinum";
        if (milestone >= 100) return "Gold";
        if (milestone >= 25) return "Silver";
        return "Bronze";
    }

    @Transactional(readOnly = true)
    public List<CertificateDto> getUserCertificates(Long userId) {
        return getUserCertificates(userId, null);
    }

    @Transactional(readOnly = true)
    public List<CertificateDto> getUserCertificates(Long userId, String language) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .filter(c -> language == null || language.isBlank() || (c.getLanguage() != null && c.getLanguage().equalsIgnoreCase(language.trim())))
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<CertificateDto> getAllCertificatesForAdmin() {
        return certificateRepository.findAllByOrderByIssuedAtDesc().stream()
                .map(c -> {
                    CertificateDto dto = toDto(c);
                    dto.setUsername(c.getUser().getUsername());
                    dto.setUserEmail(c.getUser().getEmail());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificateProgressDto getProgress(Long userId) {
        long solvedCount = submissionRepository.countDistinctSolvedProblemsByUser(userId);
        int nextMilestone = OVERALL_MILESTONES[OVERALL_MILESTONES.length - 1];
        int previousMilestone = 0;
        for (int m : OVERALL_MILESTONES) {
            if (m > solvedCount) {
                nextMilestone = m;
                break;
            }
            previousMilestone = m;
        }

        int remaining = (int) Math.max(0, nextMilestone - solvedCount);
        double progressPercent = previousMilestone == 0
                ? (solvedCount * 100.0 / nextMilestone)
                : ((solvedCount - previousMilestone) * 100.0 / Math.max(1, nextMilestone - previousMilestone));

        CertificateProgressDto dto = new CertificateProgressDto();
        dto.setSolvedCount(solvedCount);
        dto.setNextMilestone(nextMilestone);
        dto.setRemainingToNextMilestone(remaining);
        dto.setProgressPercent(Math.min(100.0, Math.max(0.0, progressPercent)));
        dto.setEarnedCertificates(getUserCertificates(userId));

        List<CertificateProgressDto.MilestoneCardDto> cards = new ArrayList<>();
        for (int m : OVERALL_MILESTONES) {
            CertificateProgressDto.MilestoneCardDto card = new CertificateProgressDto.MilestoneCardDto();
            card.setMilestone(m);
            card.setAchievementLevel(achievementLevelFor(m));
            if (solvedCount >= m) {
                card.setStatus("EARNED");
                certificateRepository.findByUserIdAndMilestone(userId, m)
                        .ifPresent(c -> card.setCertificateNumber(c.getCertificateNumber()));
            } else if (m == nextMilestone) {
                card.setStatus("IN_PROGRESS");
            } else {
                card.setStatus("LOCKED");
            }
            cards.add(card);
        }
        dto.setMilestoneCards(cards);
        return dto;
    }

    @Transactional(readOnly = true)
    public CertificateVerificationDto verify(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode)
                .map(c -> CertificateVerificationDto.found(
                        c.getRecipientName(), c.getTitle(), c.getAchievementLevel(),
                        c.getCertificateNumber(), c.getIssuedAt(), c.getVerificationCode()))
                .orElseGet(() -> CertificateVerificationDto.notFound(verificationCode));
    }

    private CertificateDto toDto(Certificate c) {
        CertificateDto dto = new CertificateDto();
        dto.setId(c.getId());
        dto.setMilestone(c.getMilestone());
        dto.setCertificateNumber(c.getCertificateNumber());
        dto.setTitle(c.getTitle());
        dto.setAchievementLevel(c.getAchievementLevel());
        dto.setRecipientName(c.getRecipientName());
        dto.setDescription(c.getDescription());
        dto.setVerificationCode(c.getVerificationCode());
        dto.setIssuedAt(c.getIssuedAt());
        dto.setLanguage(c.getLanguage());
        dto.setCategory(c.getCategory());
        return dto;
    }

    // =====================================================================
    // PDF GENERATION (Part 6) - a real, downloadable, print-ready PDF via PDFBox.
    // =====================================================================

    @Transactional(readOnly = true)
    public byte[] generateCertificatePdf(Long certificateId, Long requestingUserId, boolean isAdmin) throws IOException {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));
        if (!isAdmin && !cert.getUser().getId().equals(requestingUserId)) {
            throw new SecurityException("You do not have permission to download this certificate.");
        }
        return renderPdf(cert);
    }

    private byte[] renderPdf(Certificate cert) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            // Landscape orientation for a certificate look
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            page.setMediaBox(landscape);
            doc.addPage(page);

            float pageWidth = landscape.getWidth();
            float pageHeight = landscape.getHeight();

            PDFont bold = PDType1Font.HELVETICA_BOLD;
            PDFont regular = PDType1Font.HELVETICA;
            PDFont italic = PDType1Font.HELVETICA_OBLIQUE;

            // CodeNova brand purple
            float pr = 124f / 255f, pg = 58f / 255f, pb = 237f / 255f;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Outer border
                cs.setStrokingColor(pr, pg, pb);
                cs.setLineWidth(4f);
                cs.addRect(30, 30, pageWidth - 60, pageHeight - 60);
                cs.stroke();
                cs.setLineWidth(1f);
                cs.addRect(42, 42, pageWidth - 84, pageHeight - 84);
                cs.stroke();

                float centerX = pageWidth / 2;
                float y = pageHeight - 100;

                y = drawCentered(cs, bold, 28, centerX, y, "</> CodeNova");
                y -= 22;
                y = drawCentered(cs, italic, 12, centerX, y, "Learn. Code. Solve. Achieve.");
                y -= 40;
                cs.setStrokingColor(pr, pg, pb);
                cs.setLineWidth(1.5f);
                cs.moveTo(centerX - 220, y);
                cs.lineTo(centerX + 220, y);
                cs.stroke();
                y -= 40;

                y = drawCentered(cs, bold, 24, centerX, y, "CERTIFICATE OF ACHIEVEMENT");
                y -= 45;
                y = drawCentered(cs, regular, 13, centerX, y, "This certificate is proudly presented to");
                y -= 45;
                y = drawCentered(cs, bold, 26, centerX, y, cert.getRecipientName());
                y -= 40;
                y = drawCentered(cs, regular, 13, centerX, y, "for successfully solving");
                y -= 32;
                String achievementText = cert.getLanguage() != null
                        ? cert.getTitle().toUpperCase()
                        : (cert.getMilestone() == 1 ? "FIRST CODING PROBLEM" : cert.getMilestone() + " CODING PROBLEMS");
                y = drawCentered(cs, bold, 20, centerX, y, achievementText);
                y -= 26;
                y = drawCentered(cs, regular, 12, centerX, y, "on the CodeNova coding practice platform.");
                y -= 45;

                y = drawCentered(cs, bold, 15, centerX, y, "Achievement Level: " + cert.getAchievementLevel());
                y -= 40;

                String dateStr = cert.getIssuedAt() != null
                        ? cert.getIssuedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        : "";
                float colLeftX = centerX - 220;
                float colRightX = centerX + 40;
                cs.beginText();
                cs.setFont(regular, 11);
                cs.newLineAtOffset(colLeftX, y);
                cs.showText("Date: " + dateStr);
                cs.endText();

                cs.beginText();
                cs.setFont(regular, 11);
                cs.newLineAtOffset(colRightX, y);
                cs.showText("Certificate ID: " + cert.getCertificateNumber());
                cs.endText();
                y -= 20;

                cs.beginText();
                cs.setFont(regular, 11);
                cs.newLineAtOffset(colLeftX, y);
                cs.showText("Verification Code: " + cert.getVerificationCode());
                cs.endText();
                y -= 45;

                drawCentered(cs, bold, 13, centerX, y, "CodeNova");
            }

            doc.save(out);
            return out.toByteArray();
        }
    }

    private float drawCentered(PDPageContentStream cs, PDFont font, float size, float centerX, float y, String text)
            throws IOException {
        float width = font.getStringWidth(text) / 1000 * size;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(centerX - width / 2, y);
        cs.showText(text);
        cs.endText();
        return y;
    }
}
