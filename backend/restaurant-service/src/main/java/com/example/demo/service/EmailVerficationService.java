package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final int TOKEN_EXPIRY_HOURS = 24;

    @Transactional
    public void sendVerificationEmail(User user) {
        // Generate verification token
        String token = UUID.randomUUID().toString();

        // Set token and expiry
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        user.setEmailVerified(false);

        userRepository.save(user);

        // Send email
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), token);

        log.info("Verification email sent to user: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        // Check if token is expired
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Verification token has expired. Please request a new one.");
        }

        // Check if already verified
        if (user.getEmailVerified()) {
            log.info("Email already verified for user: {}", user.getEmail());
            return;
        }

        // Mark as verified
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);

        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Check if already verified
        if (user.getEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }

        // Check if a recent token was sent (rate limiting)
        if (user.getEmailVerificationTokenExpiry() != null &&
            user.getEmailVerificationTokenExpiry().minusHours(23).isAfter(LocalDateTime.now())) {
            throw new ValidationException("Verification email was recently sent. Please check your inbox or wait a few minutes.");
        }

        // Send new verification email
        sendVerificationEmail(user);
    }
}
