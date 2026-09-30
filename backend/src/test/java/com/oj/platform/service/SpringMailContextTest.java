package com.oj.platform.service;

import com.oj.platform.entity.AssessmentHostVerification;
import com.oj.platform.entity.HostVerificationStatus;
import com.oj.platform.entity.Role;
import com.oj.platform.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SpringMailContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailService emailService;

    @Test
    void testMailBeansInContext() {
        System.out.println("Checking JavaMailSender bean...");
        assertNotNull(javaMailSender, "JavaMailSender bean MUST exist in Spring ApplicationContext!");
        System.out.println("JavaMailSender bean exists: " + javaMailSender.getClass().getName());

        System.out.println("Checking EmailService bean...");
        assertNotNull(emailService, "EmailService bean MUST exist!");
        System.out.println("EmailService bean exists: " + emailService.getClass().getName());

        User testUser = new User("Diagnostic User", "diaguser", "shreyakrishnagb@gmail.com", "pass", Role.ROLE_USER);
        testUser.setId(9999L);

        AssessmentHostVerification verification = new AssessmentHostVerification(testUser);
        verification.setOrganizationName("Diagnostic Org");
        verification.setOrganizationType("Startup");
        verification.setStatus(HostVerificationStatus.PENDING);

        System.out.println("Testing live sendHostVerificationSubmittedEmail...");
        EmailService.EmailResult result = emailService.sendHostVerificationSubmittedEmail(testUser, verification);
        System.out.println("Submitted email result: status=" + result.getStatus() + ", msg=" + result.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, result.getStatus(), "Email should be SENT, not " + result.getStatus() + " (" + result.getMessage() + ")");
    }
}
