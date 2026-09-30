package com.oj.platform.service;

import com.oj.platform.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Generates premium responsive HTML and plain-text email templates for CodeNova.
 * Uses email-client-safe table layouts and inline CSS compatible with Gmail, Outlook,
 * Apple Mail, and mobile clients.
 *
 * All user-supplied and dynamic text is strictly escaped using HtmlUtils.
 */
@Service
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private static final String BRAND_PURPLE = "#7c3aed";
    private static final String BRAND_PURPLE_DARK = "#6d28d9";
    private static final String BRAND_BG = "#f8fafc";
    private static final String CARD_BG = "#ffffff";
    private static final String BORDER_COLOR = "#e2e8f0";
    private static final String TEXT_MAIN = "#0f172a";
    private static final String TEXT_MUTED = "#64748b";

    // =========================================================================
    // 1. WELCOME EMAIL TEMPLATES
    // =========================================================================

    public String buildWelcomeEmailHtml(User user, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "Coder");
        String safeUsername = escape(user != null ? user.getUsername() : "");
        String problemsUrl = sanitizeUrl(frontendUrl) + "/problems";
        String dashboardUrl = sanitizeUrl(frontendUrl) + "/dashboard";

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #ede9fe; color: ").append(BRAND_PURPLE_DARK).append("; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">👋 WELCOME TO CODENOVA</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Welcome aboard, ").append(safeName).append("!</h2>")
            .append("<p style=\"margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #334155;\">Your CodeNova account (<strong>@").append(safeUsername).append("</strong>) is ready. You now have full access to solve algorithm problems, participate in competitive programming contests, and earn verified skill certificates.</p>")

            // Features Overview Box
            .append("<div style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; padding: 18px 20px; margin-bottom: 24px;\">")
            .append("<div style=\"font-size: 13px; font-weight: 800; color: ").append(TEXT_MAIN).append("; margin-bottom: 10px;\">What you can do right now:</div>")
            .append("<ul style=\"margin: 0; padding-left: 18px; font-size: 13px; color: #334155; line-height: 1.7;\">")
            .append("<li><strong>Solve Problems:</strong> Explore hundreds of curated data structure & algorithm challenges.</li>")
            .append("<li><strong>Compete in Contests:</strong> Test your skills in timed, proctored coding contests.</li>")
            .append("<li><strong>Earn Certificates:</strong> Unlock verified problem-solving and language milestone certificates.</li>")
            .append("<li><strong>AI Code Assistant:</strong> Get real-time hints, complexity analysis, and debugging tips.</li>")
            .append("</ul>")
            .append("</div>")

            .append("<div style=\"text-align: center; margin: 26px 0;\">")
            .append(renderButton("Explore Problems →", problemsUrl))
            .append("</div>")

            .append("<p style=\"margin: 20px 0 0 0; font-size: 12px; color: ").append(TEXT_MUTED).append("; text-align: center;\">")
            .append("Access your personalized developer dashboard anytime at <a href=\"").append(dashboardUrl).append("\" style=\"color: ").append(BRAND_PURPLE).append("; text-decoration: underline;\">").append(dashboardUrl).append("</a>.")
            .append("</p>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildWelcomeEmailText(User user, String frontendUrl) {
        String name = user != null ? user.getName() : "Coder";
        String username = user != null ? user.getUsername() : "";
        String problemsUrl = sanitizeUrl(frontendUrl) + "/problems";

        return "Welcome to CodeNova, " + name + "!\n\n"
                + "Your account (@" + username + ") has been successfully activated.\n\n"
                + "Here is what you can do on CodeNova:\n"
                + "- Solve curated Data Structures & Algorithms problems\n"
                + "- Compete in live coding contests with real-time leaderboards\n"
                + "- Earn verified skill and language milestone certificates\n"
                + "- Use our AI coding assistant for hints and complexity analysis\n\n"
                + "Start coding now: " + problemsUrl + "\n\n"
                + "Happy Coding!\nCodeNova Team\nLearn. Code. Solve.";
    }

    // =========================================================================
    // 2. ASSESSMENT RESULT / COMPLETION TEMPLATES
    // =========================================================================

    public String buildAssessmentResultHtml(User user, Assessment assessment, AssessmentAttempt attempt, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "Candidate");
        String safeTitle = escape(assessment != null ? assessment.getTitle() : "Assessment");
        int totalScore = attempt != null && attempt.getScore() != null ? attempt.getScore() : 0;
        int maxMarks = assessment != null && assessment.getTotalMarks() != null ? assessment.getTotalMarks() : 100;
        double percentage = maxMarks > 0 ? ((double) totalScore / maxMarks) * 100.0 : 0.0;
        String formattedPercentage = String.format(Locale.ENGLISH, "%.1f%%", percentage);

        int mcqScore = attempt != null && attempt.getMcqScore() != null ? attempt.getMcqScore() : 0;
        double progScore = attempt != null && attempt.getProgrammingScore() != null ? attempt.getProgrammingScore() : 0.0;
        int violations = attempt != null && attempt.getViolationCount() != null ? attempt.getViolationCount() : 0;

        String passStatus = percentage >= 60.0 ? "PASSED" : "COMPLETED";
        String statusBg = percentage >= 60.0 ? "#dcfce7" : "#e0e7ff";
        String statusColor = percentage >= 60.0 ? "#15803d" : "#4338ca";

        String reviewUrl = sanitizeUrl(frontendUrl) + "/assessments";

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Hero Card
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: ").append(statusBg).append("; color: ").append(statusColor).append("; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">")
            .append("📊 ").append(passStatus).append("</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Assessment Results: ").append(safeTitle).append("</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">Hi ").append(safeName).append(", your attempt for <strong>").append(safeTitle).append("</strong> has been evaluated. Here is your official performance summary:</p>")

            // Score Banner
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 12px; margin-bottom: 24px;\">")
            .append("<tr><td style=\"padding: 22px 16px; text-align: center;\">")
            .append("<div style=\"font-size: 11px; font-weight: 800; color: #64748b; letter-spacing: 0.12em; text-transform: uppercase;\">FINAL SCORE</div>")
            .append("<div style=\"font-size: 34px; font-weight: 800; color: ").append(BRAND_PURPLE_DARK).append("; margin: 8px 0;\">")
            .append(totalScore).append(" / ").append(maxMarks).append(" <span style=\"font-size: 20px; color: ").append(TEXT_MUTED).append(";\">(").append(formattedPercentage).append(")</span></div>")
            .append("</td></tr></table>")

            // Breakdown Grid
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; overflow: hidden; margin-bottom: 24px;\">")
            .append("<tr>")
            .append(renderDetailBox("MCQ Score", mcqScore + " pts", false))
            .append(renderDetailBox("Coding Score", String.format(Locale.ENGLISH, "%.1f pts", progScore), true))
            .append("</tr><tr>")
            .append(renderDetailBox("Violations Recorded", String.valueOf(violations), false))
            .append(renderDetailBox("Status", passStatus, true))
            .append("</tr></table>")

            .append("<div style=\"text-align: center; margin: 24px 0;\">")
            .append(renderButton("View Assessment Center →", reviewUrl))
            .append("</div>")

            .append("<p style=\"margin: 20px 0 0 0; font-size: 11px; color: #94a3b8; text-align: center;\">")
            .append("This is an official automated scorecard generated by CodeNova.")
            .append("</p>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildAssessmentResultText(User user, Assessment assessment, AssessmentAttempt attempt, String frontendUrl) {
        String name = user != null ? user.getName() : "Candidate";
        String title = assessment != null ? assessment.getTitle() : "Assessment";
        int totalScore = attempt != null && attempt.getScore() != null ? attempt.getScore() : 0;
        int maxMarks = assessment != null && assessment.getTotalMarks() != null ? assessment.getTotalMarks() : 100;
        double percentage = maxMarks > 0 ? ((double) totalScore / maxMarks) * 100.0 : 0.0;

        return "Hi " + name + ",\n\n"
                + "Your assessment '" + title + "' has been evaluated.\n\n"
                + "Results Summary:\n"
                + "- Total Score: " + totalScore + " / " + maxMarks + " (" + String.format(Locale.ENGLISH, "%.1f%%", percentage) + ")\n"
                + "- MCQ Score: " + (attempt != null ? attempt.getMcqScore() : 0) + " pts\n"
                + "- Coding Score: " + (attempt != null ? attempt.getProgrammingScore() : 0.0) + " pts\n"
                + "- Violations: " + (attempt != null ? attempt.getViolationCount() : 0) + "\n\n"
                + "View your assessments: " + sanitizeUrl(frontendUrl) + "/assessments\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // 3. CONTEST REGISTRATION TEMPLATES
    // =========================================================================

    public String buildContestRegistrationHtml(User user, Contest contest, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "Participant");
        String safeTitle = escape(contest != null ? contest.getTitle() : "Contest");
        String safeOrg = escape(contest != null && contest.getOrganizationName() != null ? contest.getOrganizationName() : "CodeNova");

        String dateStr = contest != null ? formatDate(contest.getStartTime()) : "TBD";
        String timeStr = contest != null ? formatTimeRange(contest.getStartTime(), contest.getEndTime()) : "TBD";
        String durationStr = contest != null ? formatDuration(contest.getStartTime(), contest.getEndTime()) : "Flexible";
        String contestUrl = sanitizeUrl(frontendUrl) + "/contests/" + (contest != null ? contest.getId() : "");

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #dcfce7; color: #15803d; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">✓ REGISTRATION CONFIRMED</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">You're registered for ").append(safeTitle).append("!</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">Hi ").append(safeName).append(", your registration has been successfully confirmed for the contest organized by <strong>").append(safeOrg).append("</strong>.</p>")

            // Details Table
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; margin-bottom: 24px;\">")
            .append(renderInfoRow("📅 Date", dateStr))
            .append(renderInfoRow("🕐 Time", timeStr))
            .append(renderInfoRow("⏱ Duration", durationStr))
            .append(renderInfoRow("🏢 Host", safeOrg))
            .append("</table>")

            .append("<div style=\"text-align: center; margin: 24px 0;\">")
            .append(renderButton("Go to Contest Page →", contestUrl))
            .append("</div>")

            .append("<div style=\"background-color: #faf5ff; border: 1px solid #e9d5ff; border-radius: 8px; padding: 14px 18px; margin-top: 20px;\">")
            .append("<p style=\"margin: 0; font-size: 12px; color: #6b21a8; line-height: 1.5;\">")
            .append("<strong>Pro-tip:</strong> Join a few minutes before start time to complete camera/microphone checks.")
            .append("</p>")
            .append("</div>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildContestRegistrationText(User user, Contest contest, String frontendUrl) {
        String name = user != null ? user.getName() : "Participant";
        String title = contest != null ? contest.getTitle() : "Contest";
        String contestUrl = sanitizeUrl(frontendUrl) + "/contests/" + (contest != null ? contest.getId() : "");

        return "Hi " + name + ",\n\n"
                + "Your registration for the contest '" + title + "' is confirmed!\n\n"
                + "Details:\n"
                + "- Date: " + (contest != null ? formatDate(contest.getStartTime()) : "TBD") + "\n"
                + "- Time: " + (contest != null ? formatTimeRange(contest.getStartTime(), contest.getEndTime()) : "TBD") + "\n"
                + "- Host: " + (contest != null && contest.getOrganizationName() != null ? contest.getOrganizationName() : "CodeNova") + "\n\n"
                + "Contest Link: " + contestUrl + "\n\n"
                + "Good luck!\nCodeNova Team";
    }

    // =========================================================================
    // 4. CONTEST RESULT / RANKING TEMPLATES
    // =========================================================================

    public String buildContestResultHtml(User user, Contest contest, Integer rank, Integer totalParticipants, Integer score, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "Participant");
        String safeTitle = escape(contest != null ? contest.getTitle() : "Contest");
        int safeRank = rank != null ? rank : 1;
        int safeTotal = totalParticipants != null ? totalParticipants : 1;
        int safeScore = score != null ? score : 0;
        String contestUrl = sanitizeUrl(frontendUrl) + "/contests/" + (contest != null ? contest.getId() : "");

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #fef3c7; color: #b45309; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">🏆 CONTEST FINAL STANDINGS</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Final Rankings: ").append(safeTitle).append("</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">Hi ").append(safeName).append(", the official leaderboard for <strong>").append(safeTitle).append("</strong> has been finalized.</p>")

            // Rank Card
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 12px; margin-bottom: 24px;\">")
            .append("<tr><td style=\"padding: 22px 16px; text-align: center;\">")
            .append("<div style=\"font-size: 11px; font-weight: 800; color: #64748b; letter-spacing: 0.12em; text-transform: uppercase;\">YOUR RANK</div>")
            .append("<div style=\"font-size: 36px; font-weight: 900; color: ").append(BRAND_PURPLE_DARK).append("; margin: 8px 0;\">#").append(safeRank).append(" <span style=\"font-size: 18px; color: #64748b; font-weight: 600;\">of ").append(safeTotal).append("</span></div>")
            .append("<div style=\"font-size: 13px; color: #334155; font-weight: 600;\">Total Score: ").append(safeScore).append(" pts</div>")
            .append("</td></tr></table>")

            .append("<div style=\"text-align: center; margin: 24px 0;\">")
            .append(renderButton("View Full Leaderboard →", contestUrl))
            .append("</div>")

            .append("<p style=\"margin: 20px 0 0 0; font-size: 12px; color: ").append(TEXT_MUTED).append("; text-align: center;\">")
            .append("Thank you for participating! Keep sharpening your skills on CodeNova.")
            .append("</p>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildContestResultText(User user, Contest contest, Integer rank, Integer totalParticipants, Integer score, String frontendUrl) {
        String name = user != null ? user.getName() : "Participant";
        String title = contest != null ? contest.getTitle() : "Contest";
        int safeRank = rank != null ? rank : 1;
        int safeTotal = totalParticipants != null ? totalParticipants : 1;
        int safeScore = score != null ? score : 0;
        String contestUrl = sanitizeUrl(frontendUrl) + "/contests/" + (contest != null ? contest.getId() : "");

        return "Hi " + name + ",\n\n"
                + "The official results for '" + title + "' are in!\n\n"
                + "Your Performance:\n"
                + "- Rank: #" + safeRank + " out of " + safeTotal + " participants\n"
                + "- Total Score: " + safeScore + " pts\n\n"
                + "View leaderboard: " + contestUrl + "\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // 5. CERTIFICATE ISSUED TEMPLATES
    // =========================================================================

    public String buildCertificateIssuedHtml(User user, Certificate certificate, String frontendUrl) {
        String safeName = escape(certificate != null && certificate.getRecipientName() != null ? certificate.getRecipientName() : (user != null ? user.getName() : "Coder"));
        String safeTitle = escape(certificate != null ? certificate.getTitle() : "Achievement Certificate");
        String certNumber = escape(certificate != null ? certificate.getCertificateNumber() : "");
        String verifyCode = certificate != null ? certificate.getVerificationCode() : "";
        String verifyUrl = sanitizeUrl(frontendUrl) + "/verify-certificate/" + verifyCode;
        String certUrl = sanitizeUrl(frontendUrl) + "/certificates";

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #ede9fe; color: ").append(BRAND_PURPLE_DARK).append("; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">🎓 CERTIFICATE ISSUED</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Congratulations, ").append(safeName).append("!</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">You have earned the official CodeNova verified milestone certificate: <strong>").append(safeTitle).append("</strong>.</p>")

            // Certificate Details Box
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; margin-bottom: 24px;\">")
            .append(renderInfoRow("📜 Certificate Title", safeTitle))
            .append(renderInfoRow("🔢 Certificate No.", certNumber))
            .append(renderInfoRow("🔑 Verification Code", verifyCode))
            .append(renderInfoRow("📅 Issued At", formatDate(LocalDateTime.now())))
            .append("</table>")

            .append("<div style=\"text-align: center; margin: 26px 0;\">")
            .append(renderButton("View & Download Certificate →", certUrl))
            .append("</div>")

            .append("<div style=\"background-color: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 14px 18px; margin-top: 20px;\">")
            .append("<p style=\"margin: 0; font-size: 12px; color: #166534; line-height: 1.5;\">")
            .append("<strong>Shareable Public Verification:</strong> Anyone can verify this certificate anytime at <a href=\"").append(verifyUrl).append("\" style=\"color: #166534; font-weight: 700;\">").append(verifyUrl).append("</a>.")
            .append("</p>")
            .append("</div>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildCertificateIssuedText(User user, Certificate certificate, String frontendUrl) {
        String name = certificate != null && certificate.getRecipientName() != null ? certificate.getRecipientName() : (user != null ? user.getName() : "Coder");
        String title = certificate != null ? certificate.getTitle() : "Achievement Certificate";
        String certNumber = certificate != null ? certificate.getCertificateNumber() : "";
        String verifyCode = certificate != null ? certificate.getVerificationCode() : "";
        String verifyUrl = sanitizeUrl(frontendUrl) + "/verify-certificate/" + verifyCode;
        String certUrl = sanitizeUrl(frontendUrl) + "/certificates";

        return "Congratulations, " + name + "!\n\n"
                + "You have earned a new CodeNova certificate: " + title + "\n\n"
                + "Details:\n"
                + "- Certificate Number: " + certNumber + "\n"
                + "- Verification Code: " + verifyCode + "\n\n"
                + "View in dashboard: " + certUrl + "\n"
                + "Public Verification: " + verifyUrl + "\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // 6. QUESTION REPORT TEMPLATES (HOST NOTIFICATION)
    // =========================================================================

    public String buildQuestionReportHtml(User host, Assessment assessment, AssessmentQuestionFeedback report, String frontendUrl) {
        String safeHost = escape(host != null ? host.getName() : "Host");
        String safeTitle = escape(assessment != null ? assessment.getTitle() : "Assessment");
        String safeIssue = escape(report != null ? report.getReason() : "Question Issue");
        String safeDesc = escape(report != null ? report.getMessage() : "");
        Long qId = report != null && report.getQuestion() != null ? report.getQuestion().getId() : null;

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #fee2e2; color: #dc2626; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">⚠️ QUESTION REPORT</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Issue Reported on Assessment Question</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">Hi ").append(safeHost).append(", a candidate reported a possible issue with a question in your assessment <strong>").append(safeTitle).append("</strong>.</p>")

            // Details Table
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; margin-bottom: 24px;\">")
            .append(renderInfoRow("Question ID", qId != null ? "#" + qId : "N/A"))
            .append(renderInfoRow("Issue Category", safeIssue))
            .append(renderInfoRow("Description", safeDesc))
            .append("</table>")

            .append("<div style=\"text-align: center; margin: 24px 0;\">")
            .append(renderButton("View Hosted Assessments →", sanitizeUrl(frontendUrl) + "/assessments/host"))
            .append("</div>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildQuestionReportText(User host, Assessment assessment, AssessmentQuestionFeedback report, String frontendUrl) {
        String hostName = host != null ? host.getName() : "Host";
        String title = assessment != null ? assessment.getTitle() : "Assessment";
        Long qId = report != null && report.getQuestion() != null ? report.getQuestion().getId() : null;

        return "Hi " + hostName + ",\n\n"
                + "A candidate reported an issue with question #" + qId + " in assessment '" + title + "':\n\n"
                + "Issue Category: " + (report != null ? report.getReason() : "N/A") + "\n"
                + "Description: " + (report != null ? report.getMessage() : "N/A") + "\n\n"
                + "Manage assessments: " + sanitizeUrl(frontendUrl) + "/assessments/host\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // 7. TEST EMAIL TEMPLATES
    // =========================================================================

    public String buildTestEmailHtml(String recipientEmail, String testNote, String frontendUrl) {
        String safeEmail = escape(recipientEmail);
        String safeNote = escape(testNote != null && !testNote.isBlank() ? testNote : "Diagnostic test executed from CodeNova Admin Control Center.");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH));

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #dbeafe; color: #1d4ed8; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">🛠️ SMTP DIAGNOSTIC TEST</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Email Delivery Service is Healthy</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">This test email confirms that your CodeNova SMTP outbound notification pipeline is properly configured and delivering messages.</p>")

            // Details Table
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; margin-bottom: 24px;\">")
            .append(renderInfoRow("Recipient", safeEmail))
            .append(renderInfoRow("Timestamp", timestamp + " IST"))
            .append(renderInfoRow("Diagnostic Note", safeNote))
            .append(renderInfoRow("Status", "<span style=\"color: #16a34a; font-weight: 700;\">✓ ACTIVE & VERIFIED</span>"))
            .append("</table>")

            .append("<div style=\"text-align: center; margin: 24px 0;\">")
            .append(renderButton("Open CodeNova Platform →", sanitizeUrl(frontendUrl)))
            .append("</div>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildTestEmailText(String recipientEmail, String testNote, String frontendUrl) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH));
        return "CodeNova SMTP Diagnostic Test\n\n"
                + "This test email confirms that your CodeNova SMTP outbound notification service is active and operating normally.\n\n"
                + "Details:\n"
                + "- Recipient: " + recipientEmail + "\n"
                + "- Timestamp: " + timestamp + " IST\n"
                + "- Note: " + (testNote != null ? testNote : "N/A") + "\n\n"
                + "CodeNova: " + sanitizeUrl(frontendUrl) + "\n\n"
                + "Regards,\nCodeNova System";
    }

    // =========================================================================
    // 8. CONTEST ANNOUNCEMENT TEMPLATES
    // =========================================================================

    public String buildContestAnnouncementHtml(User user, Contest contest, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "Candidate");
        String safeTitle = escape(contest != null ? contest.getTitle() : "Upcoming Contest");
        String safeOrg = escape(contest != null && contest.getOrganizationName() != null ? contest.getOrganizationName() : "CodeNova");
        String safeDesc = escape(contest != null && contest.getDescription() != null && !contest.getDescription().isBlank()
                ? contest.getDescription()
                : "Join this exciting coding competition to test and showcase your problem-solving skills against peers!");

        String dateStr = contest != null ? formatDate(contest.getStartTime()) : "TBD";
        String timeStr = contest != null ? formatTimeRange(contest.getStartTime(), contest.getEndTime()) : "TBD";
        String durationStr = contest != null ? formatDuration(contest.getStartTime(), contest.getEndTime()) : "Flexible";
        int problemsCount = 0;
        try {
            if (contest != null && contest.getContestProblems() != null) {
                problemsCount = contest.getContestProblems().size();
            }
        } catch (Throwable ignored) {
            problemsCount = 0;
        }
        String problemsStr = problemsCount > 0 ? (problemsCount + (problemsCount == 1 ? " Problem" : " Problems")) : "Curated Problems";

        String contestUrl = sanitizeUrl(frontendUrl) + "/contests/" + (contest != null ? contest.getId() : "");

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Hero Card
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: ")
            .append(CARD_BG).append("; border-bottom: 1px solid ").append(BORDER_COLOR).append("; padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #ede9fe; color: ").append(BRAND_PURPLE_DARK).append("; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">🎯 NEW CODING CONTEST</div>")
            .append("<h2 style=\"margin: 0 0 6px 0; font-size: 23px; font-weight: 800; color: ").append(TEXT_MAIN).append("; line-height: 1.3;\">")
            .append(safeTitle).append("</h2>")
            .append("<p style=\"margin: 0 0 16px 0; font-size: 14px; color: ").append(TEXT_MUTED).append(";\">Organized by <strong style=\"color: ").append(TEXT_MAIN).append(";\">").append(safeOrg).append("</strong></p>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">").append(safeDesc).append("</p>")
            .append("</td></tr></table>");

        // Contest Details Grid
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 24px 28px; background-color: #fcfcfd;\">")
            .append("<tr><td>")
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; overflow: hidden;\">")
            .append("<tr>")
            .append(renderDetailBox("📅 Date", dateStr, false))
            .append(renderDetailBox("🕐 Time", timeStr, true))
            .append("</tr><tr>")
            .append(renderDetailBox("⏱ Duration", durationStr, false))
            .append(renderDetailBox("💻 Problems", problemsStr, true))
            .append("</tr></table>")
            .append("</td></tr></table>");

        // Upcoming Banner & CTA
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 10px 28px 24px 28px; text-align: center;\">")
            .append("<tr><td>")
            .append("<div style=\"background-color: #f1f5f9; border-radius: 8px; padding: 12px 16px; margin-bottom: 22px;\">")
            .append("<span style=\"font-weight: 700; color: ").append(BRAND_PURPLE_DARK).append("; font-size: 12px; letter-spacing: 0.05em; text-transform: uppercase;\">UPCOMING CONTEST</span>")
            .append("<p style=\"margin: 4px 0 0 0; font-size: 13px; color: ").append(TEXT_MUTED).append(";\">Get ready to test your coding skills. Complete the system check before the contest begins.</p>")
            .append("</div>")
            .append(renderButton("Enter Contest →", contestUrl))
            .append("</td></tr></table>");

        // Proctoring Requirements Card
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 0 28px 20px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"background-color: #faf5ff; border: 1px solid #e9d5ff; border-radius: 10px; padding: 18px 20px;\">")
            .append("<div style=\"font-size: 13px; font-weight: 800; color: #6b21a8; margin-bottom: 8px;\">🛡 Before entering the contest:</div>")
            .append("<ul style=\"margin: 0; padding-left: 18px; font-size: 13px; color: #581c87; line-height: 1.6;\">")
            .append("<li><strong>Camera access</strong> required throughout the contest</li>")
            .append("<li><strong>Microphone access</strong> required throughout the contest</li>")
            .append("<li><strong>Fullscreen mode</strong> required during the test</li>")
            .append("<li><strong>Tab switching</strong> or window minimizing may record a security strike</li>")
            .append("</ul>")
            .append("</div>")
            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildContestAnnouncementText(User user, Contest contest, String frontendUrl) {
        String name = user != null ? user.getName() : "Candidate";
        String contestUrl = sanitizeUrl(frontendUrl) + "/contests/" + (contest != null ? contest.getId() : "");

        return "Hello " + name + ",\n\n"
                + "A new coding contest has been announced on CodeNova!\n\n"
                + "Contest: " + (contest != null ? contest.getTitle() : "Upcoming Contest") + "\n"
                + (contest != null && contest.getOrganizationName() != null ? "Organized by: " + contest.getOrganizationName() + "\n" : "")
                + "Date: " + (contest != null ? formatDate(contest.getStartTime()) : "TBD") + "\n"
                + "Time: " + (contest != null ? formatTimeRange(contest.getStartTime(), contest.getEndTime()) : "TBD") + "\n"
                + "Duration: " + (contest != null ? formatDuration(contest.getStartTime(), contest.getEndTime()) : "Flexible") + "\n\n"
                + (contest != null && contest.getDescription() != null ? contest.getDescription() + "\n\n" : "")
                + "Enter contest here: " + contestUrl + "\n\n"
                + "Good luck!\nCodeNova Team\nLearn. Code. Solve.";
    }

    // =========================================================================
    // 9. HOST VERIFICATION SUBMITTED TEMPLATES
    // =========================================================================

    public String buildHostVerificationSubmittedHtml(User user, AssessmentHostVerification verification, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "User");
        String safeOrg = escape(verification != null && verification.getOrganizationName() != null ? verification.getOrganizationName() : "Organization");
        String safeType = escape(verification != null && verification.getOrganizationType() != null ? verification.getOrganizationType() : "Not specified");
        String safeCountry = escape(verification != null && verification.getCountry() != null ? verification.getCountry() : "Not specified");
        String safeWebsite = escape(verification != null && verification.getWebsite() != null ? verification.getWebsite() : "Not provided");

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #fef3c7; color: #b45309; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">🟡 Verification Request Received</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Hi ").append(safeName).append(",</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">We've received your request to become an assessment host on CodeNova. Our verification team will review your submitted organization details and credentials.</p>")

            // Organization Card
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; margin-bottom: 24px;\">")
            .append(renderInfoRow("Organization", safeOrg))
            .append(renderInfoRow("Organization Type", safeType))
            .append(renderInfoRow("Country/Jurisdiction", safeCountry))
            .append(renderInfoRow("Official Website", safeWebsite))
            .append(renderInfoRow("Verification Status", "<span style=\"color: #b45309; font-weight: 700;\">🟡 UNDER REVIEW</span>"))
            .append("</table>")

            .append("<p style=\"margin: 0 0 8px 0; font-size: 13px; color: ").append(TEXT_MUTED).append("; line-height: 1.5;\">")
            .append("Our verification team will carefully inspect your credentials to ensure platform integrity. You will receive another email once your verification request has been reviewed.")
            .append("</p>")
            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildHostVerificationSubmittedText(User user, AssessmentHostVerification verification, String frontendUrl) {
        String name = user != null ? user.getName() : "User";
        return "Hi " + name + ",\n\n"
                + "We've received your request to become an assessment host on CodeNova.\n\n"
                + "Organization Details:\n"
                + "- Organization: " + (verification != null && verification.getOrganizationName() != null ? verification.getOrganizationName() : "N/A") + "\n"
                + "- Organization Type: " + (verification != null && verification.getOrganizationType() != null ? verification.getOrganizationType() : "N/A") + "\n"
                + "- Country/Jurisdiction: " + (verification != null && verification.getCountry() != null ? verification.getCountry() : "N/A") + "\n"
                + "- Official Website: " + (verification != null && verification.getWebsite() != null ? verification.getWebsite() : "N/A") + "\n"
                + "- Status: UNDER REVIEW\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // 10. HOST VERIFICATION CODE / ENTRANCE CODE TEMPLATES
    // =========================================================================

    public String buildHostVerificationCodeHtml(User user, AssessmentHostVerification verification, String code, int expiryMinutes, String frontendUrl) {
        return buildHostVerificationCodeHtml(user, verification, code, expiryMinutes, frontendUrl, null);
    }

    public String buildHostVerificationCodeHtml(User user, AssessmentHostVerification verification, String code, int expiryMinutes, String frontendUrl, String token) {
        String safeName = escape(user != null ? user.getName() : "Candidate");
        String safeOrg = escape(verification != null && verification.getOrganizationName() != null && !verification.getOrganizationName().isBlank()
                ? verification.getOrganizationName()
                : "Assessment Host");
        String rawEmail = user != null && user.getEmail() != null ? user.getEmail() : "user@codenova.com";
        String safeEmail = escape(rawEmail);

        String rawCode = code != null ? code.trim() : "000000";
        String digitsOnly = rawCode.startsWith("CN-") ? rawCode.substring(3) : rawCode;
        if (digitsOnly.length() != 6 && rawCode.length() == 6) {
            digitsOnly = rawCode;
        }
        String spacedCode = String.join(" ", digitsOnly.split(""));

        String verifyUrl = sanitizeUrl(frontendUrl) + "/assessments/host?code=" + digitsOnly;
        if (token != null && !token.isBlank()) {
            verifyUrl += "&token=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter scheduleFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
        String scheduleStr = now.format(scheduleFormatter);
        String validityStr = (expiryMinutes > 0 ? expiryMinutes : 30) + " minutes";

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body Content
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #ede9fe; color: ").append(BRAND_PURPLE_DARK).append("; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">🔑 Host Assessment Verification</div>")
            .append("<h2 style=\"margin: 0 0 8px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Assessment Host Verification</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">Hi ").append(safeName).append(", your organization verification has been approved by the platform administrator. Use your unique 6-digit entrance code below to complete activation.</p>")

            // Details Card (Organization, Validity, Schedule)
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; margin-bottom: 24px;\">")
            .append(renderInfoRow("🏢 Organization", safeOrg))
            .append(renderInfoRow("⏱️ Validity", validityStr))
            .append(renderInfoRow("📅 Schedule", scheduleStr))
            .append("</table>")

            // Dashed Code Container
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 2px dashed #93c5fd; border-radius: 12px; margin-bottom: 24px;\">")
            .append("<tr><td style=\"padding: 24px 16px; text-align: center;\">")
            .append("<div style=\"font-size: 11px; font-weight: 800; color: #64748b; letter-spacing: 0.12em; text-transform: uppercase; margin-bottom: 8px;\">YOUR 6-DIGIT ENTRANCE CODE</div>")
            .append("<div style=\"font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Courier New', monospace; font-size: 36px; font-weight: 800; letter-spacing: 0.25em; color: #0f172a; margin: 12px 0;\">")
            .append(spacedCode).append("</div>")
            .append("<div style=\"font-size: 12px; color: #64748b; margin-top: 6px;\">Enter this 6-digit code on the assessment verification page.</div>")
            .append("</td></tr></table>")

            // Proctored Environment Callout Box
            .append("<div style=\"background-color: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 10px; padding: 14px 18px; margin-bottom: 24px; text-align: left;\">")
            .append("<p style=\"margin: 0; font-size: 12px; line-height: 1.6; color: #166534;\">")
            .append("<strong>🔒 Proctored Environment:</strong> Please ensure you take this assessment on a desktop/laptop with a functioning webcam and microphone. Fullscreen and tab switching are actively monitored.")
            .append("</p></div>")

            // Primary Blue Action Button
            .append("<div style=\"text-align: center; margin: 26px 0;\">")
            .append("<a href=\"").append(verifyUrl).append("\" target=\"_blank\" style=\"display: inline-block; background-color: #2563eb; color: #ffffff; text-decoration: none; font-size: 14px; font-weight: 700; padding: 13px 32px; border-radius: 8px; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);\">")
            .append("Verify &amp; Enter Assessment &rarr;</a>")
            .append("</div>")

            // Personalized Footer Notice
            .append("<p style=\"margin: 20px 0 0 0; font-size: 11px; color: #94a3b8; text-align: center; font-style: italic;\">")
            .append("This invitation link is unique and personalized for ").append(safeEmail).append(".")
            .append("</p>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildHostVerificationCodeText(User user, AssessmentHostVerification verification, String code, int expiryMinutes, String frontendUrl) {
        return buildHostVerificationCodeText(user, verification, code, expiryMinutes, frontendUrl, null);
    }

    public String buildHostVerificationCodeText(User user, AssessmentHostVerification verification, String code, int expiryMinutes, String frontendUrl, String token) {
        String name = user != null ? user.getName() : "Candidate";
        String org = verification != null && verification.getOrganizationName() != null && !verification.getOrganizationName().isBlank()
                ? verification.getOrganizationName()
                : "Assessment Host";
        String email = user != null && user.getEmail() != null ? user.getEmail() : "user@codenova.com";

        String rawCode = code != null ? code.trim() : "000000";
        String digitsOnly = rawCode.startsWith("CN-") ? rawCode.substring(3) : rawCode;
        if (digitsOnly.length() != 6 && rawCode.length() == 6) {
            digitsOnly = rawCode;
        }
        String spacedCode = String.join(" ", digitsOnly.split(""));

        String verifyUrl = sanitizeUrl(frontendUrl) + "/assessments/host?code=" + digitsOnly;
        if (token != null && !token.isBlank()) {
            verifyUrl += "&token=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter scheduleFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
        String scheduleStr = now.format(scheduleFormatter);

        return "Hi " + name + ",\n\n"
                + "Your assessment host verification request has been approved by the platform administrator.\n\n"
                + "Details:\n"
                + "- Organization: " + org + "\n"
                + "- Validity: " + (expiryMinutes > 0 ? expiryMinutes : 30) + " minutes\n"
                + "- Schedule: " + scheduleStr + "\n\n"
                + "--------------------------------------------------\n"
                + "YOUR 6-DIGIT ENTRANCE CODE:\n"
                + spacedCode + "  (Code: " + rawCode + ")\n"
                + "Enter this 6-digit code on the assessment verification page.\n"
                + "--------------------------------------------------\n\n"
                + "Verify & Enter Assessment:\n"
                + verifyUrl + "\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // 11. HOST VERIFICATION APPROVED TEMPLATES
    // =========================================================================

    public String buildHostVerificationApprovedHtml(User user, AssessmentHostVerification verification, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "Host");
        String safeOrg = escape(verification != null && verification.getOrganizationName() != null ? verification.getOrganizationName() : "Your Organization");
        String createUrl = sanitizeUrl(frontendUrl) + "/host-assessment";

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #dcfce7; color: #15803d; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">🎉 Verification Approved</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Congratulations, ").append(safeName).append("!</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">Your organization has been successfully verified as a CodeNova assessment host.</p>")

            // Organization Card
            .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border: 1px solid ").append(BORDER_COLOR).append("; border-radius: 10px; margin-bottom: 24px;\">")
            .append(renderInfoRow("Organization", safeOrg))
            .append(renderInfoRow("Status", "<span style=\"color: #15803d; font-weight: 700;\">✓ VERIFIED</span>"))
            .append("</table>")

            .append("<p style=\"margin: 0 0 24px 0; font-size: 14px; color: #334155;\">You can now create and host assessments on CodeNova.</p>")

            .append("<div style=\"text-align: center; margin: 24px 0;\">")
            .append(renderButton("Create Assessment →", createUrl))
            .append("</div>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildHostVerificationApprovedText(User user, AssessmentHostVerification verification, String frontendUrl) {
        String name = user != null ? user.getName() : "Host";
        String createUrl = sanitizeUrl(frontendUrl) + "/host-assessment";

        return "Congratulations, " + name + "!\n\n"
                + "Your organization has been successfully verified as a CodeNova assessment host.\n\n"
                + "Organization: " + (verification != null && verification.getOrganizationName() != null ? verification.getOrganizationName() : "N/A") + "\n"
                + "Status: VERIFIED\n\n"
                + "Create assessment: " + createUrl + "\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // 12. HOST VERIFICATION REJECTED TEMPLATES
    // =========================================================================

    public String buildHostVerificationRejectedHtml(User user, AssessmentHostVerification verification, String rejectionReason, String frontendUrl) {
        String safeName = escape(user != null ? user.getName() : "Applicant");
        String safeReason = escape(rejectionReason != null && !rejectionReason.isBlank()
                ? rejectionReason
                : "The submitted business documentation was insufficient or could not be validated.");
        String verifyUrl = sanitizeUrl(frontendUrl) + "/host-assessment";

        StringBuilder html = new StringBuilder();
        html.append(startDocument());
        html.append(renderHeader());

        // Body
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 32px 28px;\">")
            .append("<tr><td>")
            .append("<div style=\"display: inline-block; background-color: #fee2e2; color: #dc2626; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.08em; padding: 4px 12px; border-radius: 9999px; margin-bottom: 12px;\">Verification Requires Correction</div>")
            .append("<h2 style=\"margin: 0 0 10px 0; font-size: 22px; font-weight: 800; color: ").append(TEXT_MAIN).append(";\">Hi ").append(safeName).append(",</h2>")
            .append("<p style=\"margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #334155;\">We reviewed your request to become an assessment host on CodeNova. Unfortunately, we could not approve the request at this time.</p>")

            // Reason Box
            .append("<div style=\"background-color: #fff1f2; border: 1px solid #fecdd3; border-radius: 10px; padding: 16px 20px; margin-bottom: 24px;\">")
            .append("<div style=\"font-size: 13px; font-weight: 800; color: #9f1239; margin-bottom: 6px;\">Reason for Review Outcome:</div>")
            .append("<p style=\"margin: 0; font-size: 14px; color: #881337; line-height: 1.6;\">").append(safeReason).append("</p>")
            .append("</div>")

            .append("<div style=\"text-align: center; margin: 24px 0;\">")
            .append(renderButton("Review Verification →", verifyUrl))
            .append("</div>")

            .append("</td></tr></table>");

        html.append(renderFooter());
        html.append(endDocument());
        return html.toString();
    }

    public String buildHostVerificationRejectedText(User user, AssessmentHostVerification verification, String rejectionReason, String frontendUrl) {
        String name = user != null ? user.getName() : "Applicant";
        String verifyUrl = sanitizeUrl(frontendUrl) + "/host-assessment";

        return "Hi " + name + ",\n\n"
                + "We reviewed your request to become an assessment host on CodeNova. Unfortunately, we could not approve the request at this time.\n\n"
                + "Reason:\n"
                + (rejectionReason != null ? rejectionReason : "Information provided requires correction.") + "\n\n"
                + "Review your verification details: " + verifyUrl + "\n\n"
                + "Regards,\nCodeNova Team";
    }

    // =========================================================================
    // HELPER BUILDERS
    // =========================================================================

    private String startDocument() {
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>CodeNova</title></head>"
                + "<body style=\"margin: 0; padding: 24px 12px; background-color: " + BRAND_BG + "; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: " + TEXT_MAIN + "; -webkit-font-smoothing: antialiased;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width: 600px; margin: 0 auto; background-color: " + CARD_BG + "; border: 1px solid " + BORDER_COLOR + "; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);\">"
                + "<tr><td>";
    }

    private String endDocument() {
        return "</td></tr></table></body></html>";
    }

    private String renderHeader() {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: " + BRAND_PURPLE + "; padding: 26px 28px; text-align: center;\">"
                + "<tr><td>"
                + "<h1 style=\"margin: 0; font-size: 24px; font-weight: 900; letter-spacing: 0.04em; color: #ffffff;\">CODENOVA</h1>"
                + "<p style=\"margin: 4px 0 0 0; font-size: 13px; font-weight: 600; color: #e9d5ff; letter-spacing: 0.05em;\">Learn. Code. Solve.</p>"
                + "</td></tr></table>";
    }

    private String renderFooter() {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8fafc; border-top: 1px solid " + BORDER_COLOR + "; padding: 24px 28px; text-align: center;\">"
                + "<tr><td>"
                + "<p style=\"margin: 0 0 4px 0; font-size: 13px; font-weight: 700; color: " + TEXT_MAIN + ";\">CodeNova</p>"
                + "<p style=\"margin: 0 0 10px 0; font-size: 12px; color: " + TEXT_MUTED + ";\">Learn. Code. Solve.</p>"
                + "<p style=\"margin: 0 0 6px 0; font-size: 11px; color: #94a3b8;\">You are receiving this email because you have a registered CodeNova account.</p>"
                + "<p style=\"margin: 0; font-size: 11px; color: #94a3b8;\">&copy; 2026 CodeNova. All rights reserved.</p>"
                + "</td></tr></table>";
    }

    private String renderButton(String label, String url) {
        return "<a href=\"" + url + "\" target=\"_blank\" style=\"display: inline-block; background-color: " + BRAND_PURPLE + "; color: #ffffff; text-decoration: none; font-size: 14px; font-weight: 700; padding: 12px 28px; border-radius: 8px; box-shadow: 0 3px 10px rgba(124, 58, 237, 0.35);\">"
                + label + "</a>";
    }

    private String renderDetailBox(String label, String value, boolean isRight) {
        String borderStyle = isRight ? "border-left: 1px solid " + BORDER_COLOR + ";" : "";
        return "<td style=\"width: 50%; padding: 14px 18px; " + borderStyle + " border-bottom: 1px solid " + BORDER_COLOR + ";\">"
                + "<div style=\"font-size: 11px; font-weight: 700; color: " + TEXT_MUTED + "; text-transform: uppercase; letter-spacing: 0.04em;\">" + label + "</div>"
                + "<div style=\"font-size: 14px; font-weight: 700; color: " + TEXT_MAIN + "; margin-top: 4px;\">" + value + "</div>"
                + "</td>";
    }

    private String renderInfoRow(String label, String valueHtml) {
        return "<tr>"
                + "<td style=\"padding: 10px 16px; border-bottom: 1px solid " + BORDER_COLOR + "; font-size: 13px; font-weight: 600; color: " + TEXT_MUTED + "; width: 38%;\">" + label + "</td>"
                + "<td style=\"padding: 10px 16px; border-bottom: 1px solid " + BORDER_COLOR + "; font-size: 13px; font-weight: 600; color: " + TEXT_MAIN + ";\">" + valueHtml + "</td>"
                + "</tr>";
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) return "TBD";
        return dt.format(DATE_FORMAT);
    }

    private String formatTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) return "TBD";
        if (start != null && end != null) {
            return start.format(TIME_FORMAT) + " – " + end.format(TIME_FORMAT) + " IST";
        }
        return (start != null ? start.format(TIME_FORMAT) : "") + " IST";
    }

    private String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "Flexible Duration";
        long minutes = Math.max(0, Duration.between(start, end).toMinutes());
        if (minutes >= 60 && minutes % 60 == 0) {
            long hours = minutes / 60;
            return hours + (hours == 1 ? " hour" : " hours") + " (" + minutes + " mins)";
        } else if (minutes >= 60) {
            long hours = minutes / 60;
            long rem = minutes % 60;
            return hours + " hr " + rem + " min (" + minutes + " mins)";
        }
        return minutes + " minutes";
    }

    private String escape(String text) {
        if (text == null) return "";
        return HtmlUtils.htmlEscape(text);
    }

    private String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) return "http://localhost:5173";
        String trimmed = url.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
