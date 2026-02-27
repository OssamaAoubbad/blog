package net.aoubbad.blog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        try {
            String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationUrl", verificationUrl);
            String htmlContent = templateEngine.process("email-verification", context);
            sendEmail(to, "Verification de votre compte aoubbad-blog", htmlContent);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending verification email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        try {
            String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetUrl", resetUrl);
            String htmlContent = templateEngine.process("password-reset", context);
            sendEmail(to, "Password reset", htmlContent);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending password reset email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            String htmlContent = templateEngine.process("welcome-email", context);
            sendEmail(to, "Bienvenue sur aoubbad-blog", htmlContent);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending welcome email to {}: {}", to, e.getMessage(), e);
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
            message,
            MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
            StandardCharsets.UTF_8.name()
        );
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
