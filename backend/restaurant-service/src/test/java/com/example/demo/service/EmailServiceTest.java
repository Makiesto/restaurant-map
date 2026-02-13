package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private static final String FRONTEND_URL = "http://localhost:5173";
    private static final String FROM_EMAIL = "noreply@restaurant-app.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", FRONTEND_URL);
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
    }

    @Test
    void sendVerificationEmail_WithValidData_ShouldSendEmail() {
        // Given
        String toEmail = "test@example.com";
        String firstName = "John";
        String verificationToken = "test-token-123";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendVerificationEmail(toEmail, firstName, verificationToken);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getTo()).containsExactly(toEmail);
        assertThat(sentMessage.getFrom()).isEqualTo(FROM_EMAIL);
        assertThat(sentMessage.getSubject()).isEqualTo("Verify Your Email Address");
        assertThat(sentMessage.getText()).contains(firstName);
        assertThat(sentMessage.getText()).contains(FRONTEND_URL + "/verify-email?token=" + verificationToken);
    }

    @Test
    void sendVerificationEmail_ShouldIncludeVerificationLink() {
        // Given
        String toEmail = "test@example.com";
        String firstName = "Jane";
        String token = "abc123xyz";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendVerificationEmail(toEmail, firstName, token);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        String expectedLink = FRONTEND_URL + "/verify-email?token=" + token;
        assertThat(sentMessage.getText()).contains(expectedLink);
    }

    @Test
    void sendVerificationEmail_WhenMailSenderFails_ShouldThrowException() {
        // Given
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When/Then
        assertThatThrownBy(() ->
                emailService.sendVerificationEmail("test@example.com", "John", "token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send verification email");
    }

    @Test
    void sendWelcomeEmail_WithValidData_ShouldSendEmail() {
        // Given
        String toEmail = "test@example.com";
        String firstName = "John";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendWelcomeEmail(toEmail, firstName);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getTo()).containsExactly(toEmail);
        assertThat(sentMessage.getFrom()).isEqualTo(FROM_EMAIL);
        assertThat(sentMessage.getSubject()).isEqualTo("Welcome to Restaurant App!");
        assertThat(sentMessage.getText()).contains(firstName);
        assertThat(sentMessage.getText()).contains("Welcome to Restaurant App");
    }

    @Test
    void sendWelcomeEmail_WhenFails_ShouldNotThrowException() {
        // Given
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When/Then - Should not throw (welcome email is not critical)
        emailService.sendWelcomeEmail("test@example.com", "John");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetEmail_WithValidData_ShouldSendEmail() {
        // Given
        String toEmail = "test@example.com";
        String firstName = "John";
        String resetToken = "reset-token-123";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendPasswordResetEmail(toEmail, firstName, resetToken);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getTo()).containsExactly(toEmail);
        assertThat(sentMessage.getFrom()).isEqualTo(FROM_EMAIL);
        assertThat(sentMessage.getSubject()).isEqualTo("Reset Your Password");
        assertThat(sentMessage.getText()).contains(firstName);
        assertThat(sentMessage.getText()).contains(FRONTEND_URL + "/reset-password?token=" + resetToken);
    }

    @Test
    void sendPasswordResetEmail_ShouldIncludeResetLink() {
        // Given
        String toEmail = "test@example.com";
        String firstName = "Jane";
        String token = "reset-xyz789";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendPasswordResetEmail(toEmail, firstName, token);

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        String expectedLink = FRONTEND_URL + "/reset-password?token=" + token;
        assertThat(sentMessage.getText()).contains(expectedLink);
    }

    @Test
    void sendPasswordResetEmail_WhenMailSenderFails_ShouldThrowException() {
        // Given
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When/Then
        assertThatThrownBy(() ->
                emailService.sendPasswordResetEmail("test@example.com", "John", "token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send password reset email");
    }

    @Test
    void allEmails_ShouldUseCorrectFromAddress() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendVerificationEmail("test@example.com", "John", "token1");
        emailService.sendWelcomeEmail("test@example.com", "John");
        emailService.sendPasswordResetEmail("test@example.com", "John", "token2");

        // Then
        verify(mailSender, times(3)).send(messageCaptor.capture());

        messageCaptor.getAllValues().forEach(message ->
                assertThat(message.getFrom()).isEqualTo(FROM_EMAIL)
        );
    }

    @Test
    void verificationEmail_ShouldMentionExpirationTime() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendVerificationEmail("test@example.com", "John", "token");

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getText()).contains("24 hours");
    }

    @Test
    void passwordResetEmail_ShouldMentionExpirationTime() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendPasswordResetEmail("test@example.com", "John", "token");

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getText()).contains("1 hour");
    }

    @Test
    void welcomeEmail_ShouldIncludeFrontendUrl() {
        // Given
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendWelcomeEmail("test@example.com", "John");

        // Then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getText()).contains(FRONTEND_URL);
    }
}