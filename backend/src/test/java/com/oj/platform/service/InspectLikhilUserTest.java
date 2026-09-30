package com.oj.platform.service;

import com.oj.platform.entity.AssessmentHostVerification;
import com.oj.platform.entity.User;
import com.oj.platform.repository.AssessmentHostVerificationRepository;
import com.oj.platform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class InspectLikhilUserTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssessmentHostVerificationRepository verificationRepository;

    @Test
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    void inspectUsersAndVerifications() {
        System.out.println("========== USERS IN DB ==========");
        List<User> users = userRepository.findAll();
        for (User u : users) {
            System.out.println("User id=" + u.getId() + ", username=" + u.getUsername() + ", email=" + u.getEmail() + ", role=" + u.getRole());
        }

        System.out.println("========== HOST VERIFICATIONS IN DB ==========");
        List<AssessmentHostVerification> verifs = verificationRepository.findAll();
        for (AssessmentHostVerification v : verifs) {
            System.out.println("Verif id=" + v.getId() + ", user=" + (v.getUser() != null ? v.getUser().getUsername() + "(" + v.getUser().getEmail() + ")" : "null")
                    + ", status=" + v.getStatus() + ", org=" + v.getOrganizationName() + ", rejectionReason=" + v.getRejectionReason());
        }
        System.out.println("=========================================");
    }
}
