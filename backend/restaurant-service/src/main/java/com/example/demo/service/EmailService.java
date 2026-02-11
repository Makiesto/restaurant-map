package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@restaurant-app.com}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String firstName, String verificationToken) {
        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your Email Address");
            message.setText(buildVerificationEmailBody(firstName, verificationLink));

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Restaurant App!");
            message.setText(buildWelcomeEmailBody(firstName));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw - welcome email is not critical
        }
    }

    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset Your Password");
            message.setText(buildPasswordResetEmailBody(firstName, resetLink));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildVerificationEmailBody(String firstName, String verificationLink) {
        return String.format("""
            Hello %s,
            
            Thank you for registering with Restaurant App!
            
            Please verify your email address by clicking the link below:
            %s
            
            This link will expire in 24 hours.
            
            If you didn't create an account, please ignore this email.
            
            Best regards,
            The Restaurant App Team
            """, firstName, verificationLink);
    }

    private String buildWelcomeEmailBody(String firstName) {
        return String.format("""
            Hello %s,
            
            Welcome to Restaurant App! Your email has been verified successfully.
            
            You can now:
            - Browse and review restaurants
            - Add your own restaurants
            - Set your allergen preferences
            - And much more!
            
            Start exploring: %s
            
            Best regards,
            The Restaurant App Team
            """, firstName, frontendUrl);
    }

    private String buildPasswordResetEmailBody(String firstName, String resetLink) {
        return String.format("""
            Hello %s,
            
            We received a request to reset your password.
            
            Click the link below to reset your password:
            %s
            
            This link will expire in 1 hour.
            
            If you didn't request a password reset, please ignore this email.
            
            Best regards,
            The Restaurant App Team
            """, firstName, resetLink);
    }
}