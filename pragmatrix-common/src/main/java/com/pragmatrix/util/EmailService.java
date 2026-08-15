package com.pragmatrix.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility for sending emails via SMTP (Jakarta Mail / Angus Mail).
 * <p>
 * Configuration is loaded from {@code email.properties} on the classpath,
 * with fallback to environment variables (e.g. SMTP_USERNAME, SMTP_PASSWORD).
 * All send operations are wrapped in try/catch — failures are logged
 * server-side and never break calling code.
 * </p>
 */
public class EmailService {

    private static Properties emailProps;
    private static boolean initialized = false;

    private EmailService() {} // utility class

    /**
     * Lazily load email configuration from classpath.
     */
    private static synchronized void loadConfig() {
        if (initialized) return;
        emailProps = new Properties();
        try (InputStream is = EmailService.class.getClassLoader()
                .getResourceAsStream("email.properties")) {
            if (is != null) {
                emailProps.load(is);
                System.out.println("[PRAGMATRIX-EMAIL] Email configuration loaded from email.properties.");
            } else {
                System.out.println("[PRAGMATRIX-EMAIL] email.properties not found; checking environment variables.");
            }
        } catch (IOException e) {
            System.err.println("[PRAGMATRIX-EMAIL] Failed to read email.properties: " + e.getMessage());
        }
        initialized = true;
    }

    private static String getPropOrEnv(String propKey, String envKey, String defaultValue) {
        String val = null;
        if (emailProps != null) {
            val = emailProps.getProperty(propKey);
        }
        if (val == null || val.trim().isEmpty()) {
            val = System.getenv(envKey);
        }
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultValue;
    }

    /**
     * Send registration confirmation email to a team lead.
     */
    public static boolean sendRegistrationConfirmationEmail(String toEmail, String teamLeadName, String collegeName, String quizName, String teamCode) {
        String subject = "PRAGMATRIX 2026 \u2013 Registration Confirmed (" + teamCode + ")";
        String body = "Dear " + (teamLeadName != null && !teamLeadName.isEmpty() ? teamLeadName : "Team Lead") + ",\n\n"
                + "Greetings from Team PRAGMATRIX 2026!\n\n"
                + "We are pleased to inform you that your registration for PRAGMATRIX 2026 \u2013 Applied\n"
                + "Management Carnival, organized by the Post Graduate Department of Business Administration,\n"
                + "Seshadripuram College, Bengaluru, has been successfully completed.\n\n"
                + "--------------------------------------------------\n"
                + "REGISTRATION DETAILS:\n"
                + "Event:       " + quizName + "\n"
                + "College:     " + collegeName + "\n"
                + "Team Lead:   " + teamLeadName + "\n"
                + "Team Code:   " + teamCode + "\n"
                + "--------------------------------------------------\n\n"
                + "Your Team Code (" + teamCode + ") is your login credential to check your live\n"
                + "status, round results, and score updates on the PRAGMATRIX portal.\n\n"
                + "Event Schedule:\n"
                + "Date:  24th August 2026\n"
                + "Time:  9:00 A.M. onwards\n"
                + "Venue: Conference Hall, Seshadripuram College, Bengaluru\n\n"
                + "We look forward to welcoming you and wish your team the very best!\n\n"
                + "Warm regards,\n"
                + "Team PRAGMATRIX 2026\n"
                + "Post Graduate Department of Business Administration\n"
                + "Seshadripuram College, Bengaluru";

        return sendEmail(toEmail, subject, body);
    }

    /**
     * Backward-compatible participant ID email sender.
     */
    public static boolean sendParticipantIdEmail(String toEmail, String participantId) {
        return sendRegistrationConfirmationEmail(toEmail, "Participant", "Registered Institution", "PRAGMATRIX 2026", participantId);
    }

    /**
     * Core email-sending method.
     */
    private static boolean sendEmail(String toEmail, String subject, String body) {
        loadConfig();

        String host = getPropOrEnv("email.smtp.host", "SMTP_HOST", "smtp.gmail.com");
        String port = getPropOrEnv("email.smtp.port", "SMTP_PORT", "587");
        String username = getPropOrEnv("email.smtp.username", "SMTP_USERNAME", "");
        if (username.isEmpty()) username = getPropOrEnv("email.smtp.username", "EMAIL_USERNAME", "");
        String password = getPropOrEnv("email.smtp.password", "SMTP_PASSWORD", "");
        if (password.isEmpty()) password = getPropOrEnv("email.smtp.password", "EMAIL_PASSWORD", "");
        String fromEmail = getPropOrEnv("email.from.address", "EMAIL_FROM", username);
        String fromName = getPropOrEnv("email.from.name", "EMAIL_FROM_NAME", "PRAGMATRIX 2026");

        if (username.isEmpty() || password.isEmpty()) {
            System.err.println("[PRAGMATRIX-EMAIL] SMTP username/password not configured. Skipping send to: " + toEmail);
            return false;
        }

        try {
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.auth", "true");
            mailProps.put("mail.smtp.starttls.enable", "true");
            mailProps.put("mail.smtp.host", host);
            mailProps.put("mail.smtp.port", port);
            mailProps.put("mail.smtp.ssl.trust", host);

            final String finalUsername = username;
            final String finalPassword = password;

            Session session = Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalUsername, finalPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("[PRAGMATRIX-EMAIL] Email sent successfully to: " + toEmail + " | Subject: " + subject);
            return true;

        } catch (Exception e) {
            System.err.println("[PRAGMATRIX-EMAIL] Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
