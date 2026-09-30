package com.oj.platform.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class RealSmtpConnectivityTest {

    @Test
    void testRealGmailSmtpSend() throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("shreyakrishnagb@gmail.com");
        mailSender.setPassword("ijjkiylxwvmzofwi");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        EmailTemplateService templateService = new EmailTemplateService();
        com.oj.platform.entity.User user = new com.oj.platform.entity.User();
        user.setName("likhil");
        user.setEmail("shreyak8225@gmail.com");

        com.oj.platform.entity.AssessmentHostVerification v = new com.oj.platform.entity.AssessmentHostVerification();
        v.setOrganizationName("ABC Tech");

        String code = "414776";
        String htmlBody = templateService.buildHostVerificationCodeHtml(user, v, code, 30, "http://localhost:5173");
        String textBody = templateService.buildHostVerificationCodeText(user, v, code, 30, "http://localhost:5173");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("CodeNova <shreyakrishnagb@gmail.com>");
        helper.setTo("shreyak8225@gmail.com");
        helper.setReplyTo("shreyakrishnagb@gmail.com");
        helper.setSubject("Coding Assessment Invitation — CodeNova Assessment Host Verification");
        helper.setText(textBody, htmlBody);

        System.out.println("Attempting to send official verification email to shreyak8225@gmail.com via smtp.gmail.com:587...");
        mailSender.send(message);
        System.out.println("SUCCESS! Email sent successfully to shreyak8225@gmail.com");
    }
}
