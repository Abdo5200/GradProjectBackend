package com.example.gradproject.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.gradproject.exception.EmailServiceException;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${from.email}")
    private String fromEmail;

    @Value("${from.name}")
    private String fromName;

    @Value("${spring.mail.username}")
    private String gmailUsername;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @PostConstruct
    public void logConfig() {
        logger.info("Email Service Configuration:");
        logger.info("Gmail Username: {}",
                gmailUsername != null && !gmailUsername.isEmpty()
                        ? "SET (" + gmailUsername.substring(0, Math.min(5, gmailUsername.length())) + "...)"
                        : "NOT SET");
        logger.info("Frontend URL: {}", frontendUrl);
        logger.info("From Email: {}", fromEmail);
        logger.info("From Name: {}", fromName);
    }

    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        try {
            logger.debug("Sending password reset email to: {}", toEmail);

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            String htmlContent = createPasswordResetEmailTemplate(firstName, resetLink);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - Todo App");
            helper.setText(htmlContent, true); // true indicates HTML content

            javaMailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new EmailServiceException("Error sending password reset email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending password reset email to {}: {}", toEmail, e.getMessage());
            throw new EmailServiceException("Error sending password reset email: " + e.getMessage(), e);
        }
    }

    private String createPasswordResetEmailTemplate(String firstName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        .email-container {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            border: 1px solid #ddd;
                            border-radius: 10px;
                        }
                        .header {
                            background-color: #007bff;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 10px 10px 0 0;
                        }
                        .content {
                            padding: 30px 20px;
                        }
                        .reset-button {
                            display: inline-block;
                            background-color: #28a745;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                        }
                        .footer {
                            text-align: center;
                            color: #666;
                            font-size: 12px;
                            margin-top: 30px;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>Password Reset Request</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s,</h2>
                            <p>You requested to reset your password for your Medical System account.</p>
                            <p>Click the button below to reset your password:</p>
                            <a href="%s" class="reset-button">Reset Password</a>
                            <p>Or copy and paste this link in your browser:</p>
                            <p style="word-break: break-all; color: #007bff;">%s</p>
                            <p><strong>This link will expire in 1 hour.</strong></p>
                            <p>If you didn't request this password reset, please ignore this email.</p>
                            <p>Best regards,<br>Todo App Team</p>
                        </div>
                        <div class="footer">
                            <p>This is an automated email. Please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(firstName, resetLink, resetLink);
    }
}
