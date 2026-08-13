package com.pragmatrix.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility for sending emails via SMTP (Jakarta Mail / Angus Mail).
 * <p>
 * Configuration is loaded from {@code email.properties} on the classpath.
 * All send operations are wrapped in try/catch — failures are logged
 * server-side and never propagated to break calling code.
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
            if (is == null) {
                System.err.println("[PRAGMATRIX-EMAIL] email.properties not found on classpath! Email sending disabled.");
                initialized = true;
                return;
            }
            emailProps.load(is);
            initialized = true;
            System.out.println("[PRAGMATRIX-EMAIL] Email configuration loaded successfully.");
        } catch (IOException e) {
            System.err.println("[PRAGMATRIX-EMAIL] Failed to load email.properties: " + e.getMessage());
            initialized = true;
        }
    }

    /**
     * Send the Participant ID confirmation email to a team lead.
     *
     * @param toEmail       the team lead's email address
     * @param participantId the generated unique ID (e.g. "PMBZ047")
     * @return true if sent successfully, false otherwise
     */
    public static boolean sendParticipantIdEmail(String toEmail, String participantId) {
        String subject = "PRAGMATRIX 2026 \u2013 Your Participant ID";
        String body = "Dear Participant,\n\n"
                + "Greetings from Team PRAGMATRIX 2026!\n\n"
                + "We are pleased to inform you that your registration for PRAGMATRIX 2026 \u2013 Applied\n"
                + "Management Carnival, an Inter-Collegiate Fest organized by the Post Graduate\n"
                + "Department of Business Administration, Seshadripuram College, has been\n"
                + "successfully completed.\n\n"
                + "Your unique Participant ID has been generated:\n\n"
                + "Participant ID: " + participantId + "\n\n"
                + "Please keep this ID safe and use it for participant identification and\n"
                + "verification during the fest.\n\n"
                + "Event Details:\n"
                + "Date: 24th August 2026\n"
                + "Time: 9:00 A.M. onwards\n"
                + "Venue: Conference Hall, Seshadripuram College, Bengaluru\n\n"
                + "We look forward to welcoming you to PRAGMATRIX 2026 and wish you the very best!\n\n"
                + "Regards,\n"
                + "Team PRAGMATRIX 2026\n"
                + "Post Graduate Department of Business Administration\n"
                + "Seshadripuram College, Bengaluru";

        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send a login OTP email to a team lead.
     *
     * @param toEmail the team lead's email address
     * @param otpCode the 6-digit OTP
     * @return true if sent successfully, false otherwise
     */
    public static boolean sendOtpEmail(String toEmail, String otpCode) {
        String subject = "PRAGMATRIX 2026 \u2013 Your Login OTP";
        String body = "Dear Participant,\n\n"
                + "Greetings from Team PRAGMATRIX 2026!\n\n"
                + "Your one-time password (OTP) for logging into the Team Dashboard is:\n\n"
                + "OTP: " + otpCode + "\n\n"
                + "This OTP is valid for 5 minutes. Please do not share it with anyone.\n\n"
                + "If you did not request this OTP, please ignore this email.\n\n"
                + "Regards,\n"
                + "Team PRAGMATRIX 2026\n"
                + "Post Graduate Department of Business Administration\n"
                + "Seshadripuram College, Bengaluru";

        return sendEmail(toEmail, subject, body);
    }

    /**
     * Core email-sending method. Wraps all failures in try/catch.
     *
     * @param toEmail the recipient email
     * @param subject the email subject
     * @param body    the plain-text email body
     * @return true if sent successfully, false otherwise
     */
    private static boolean sendEmail(String toEmail, String subject, String body) {
        loadConfig();

        if (emailProps == null || emailProps.isEmpty()) {
            System.err.println("[PRAGMATRIX-EMAIL] Email config not available. Skipping send to: " + toEmail);
            return false;
        }

        String host = emailProps.getProperty("email.smtp.host", "smtp.gmail.com");
        String port = emailProps.getProperty("email.smtp.port", "587");
        String username = emailProps.getProperty("email.smtp.username", "");
        String password = emailProps.getProperty("email.smtp.password", "");
        String fromEmail = emailProps.getProperty("email.from.address", username);
        String fromName = emailProps.getProperty("email.from.name", "PRAGMATRIX 2026");

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

            Session session = Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
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
