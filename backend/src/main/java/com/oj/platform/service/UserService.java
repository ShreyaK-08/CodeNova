package com.oj.platform.service;

import com.oj.platform.dto.ChangePasswordRequest;
import com.oj.platform.dto.ProfileUpdateRequest;
import com.oj.platform.dto.UserProfileResponse;
import com.oj.platform.entity.Role;
import com.oj.platform.entity.User;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.SubmissionRepository;
import com.oj.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubmissionRepository submissionRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // If email is being changed, verify it is not already taken by another user
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            Optional<User> existingEmailUser = userRepository.findByEmail(request.getEmail());
            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(userId)) {
                throw new BadRequestException("Email is already in use by another account");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        user.setBio(request.getBio());
        user.setSkills(request.getSkills());
        user.setGithubUrl(request.getGithubUrl());
        user.setLinkedinUrl(request.getLinkedinUrl());
        user.setAvatarUrl(request.getAvatarUrl());

        User updatedUser = userRepository.save(user);

        return mapToProfileResponse(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long targetUserId) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        if (target.getRole() == Role.ROLE_ADMIN) {
            throw new BadRequestException("The admin account cannot be deleted");
        }

        // Remove the user's submissions first (submission_results cascade automatically)
        // so the foreign key from submissions -> users does not block deletion.
        submissionRepository.deleteAll(submissionRepository.findByUserIdOrderBySubmittedAtDesc(targetUserId));

        userRepository.delete(target);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setBio(user.getBio());
        response.setSkills(user.getSkills());
        response.setGithubUrl(user.getGithubUrl());
        response.setLinkedinUrl(user.getLinkedinUrl());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
