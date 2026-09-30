package com.oj.platform.service;

import com.oj.platform.entity.User;
import com.oj.platform.repository.AssessmentHostVerificationRepository;
import com.oj.platform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ResetLikhilVerificationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssessmentHostVerificationRepository verificationRepository;

    @Test
    @Transactional
    @Rollback(false)
    void deleteLikhilVerification() {
        Optional<User> likhilOpt = userRepository.findByUsername("likhil");
        assertTrue(likhilOpt.isPresent(), "User likhil must exist");
        User likhil = likhilOpt.get();
        System.out.println("Found user likhil id=" + likhil.getId() + ", email=" + likhil.getEmail());

        verificationRepository.findByUserId(likhil.getId()).ifPresent(v -> {
            System.out.println("Deleting verification record id=" + v.getId() + " for user likhil...");
            verificationRepository.delete(v);
            System.out.println("Verification record deleted successfully.");
        });

        Optional<?> check = verificationRepository.findByUserId(likhil.getId());
        assertFalse(check.isPresent(), "Likhil verification record must be deleted");
        System.out.println("CONFIRMED: User likhil now has NO verification record in DB (status=NOT_SUBMITTED).");
    }
}
