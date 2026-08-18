package com.pragmatrix.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

/**
 * Enterprise Email Delivery Service for PRAGMATRIX 2026.
 * <p>
 * Primary Delivery: Brevo Transactional Email REST API (https://api.brevo.com/v3/smtp/email)
 * Fallback Delivery: SMTP (for local development or offline testing)
 * </p>
 * <p>
 * Configuration Precedence:
 * <ol>
 *   <li><b>Brevo HTTPS API via Environment Variables</b>: BREVO_API_KEY / EMAIL_API_KEY</li>
 *   <li><b>Brevo HTTPS API via Properties File</b>: {@code email.properties} ({@code brevo.api.key} / {@code email.api.key})</li>
 *   <li><b>SMTP via Environment Variables</b>: SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD</li>
 *   <li><b>SMTP via Properties File</b>: {@code email.properties}</li>
 * </ol>
 * </p>
 */
public class EmailService {

    public enum TransportMode {
        HTTPS_BREVO,
        SMTP,
        NONE
    }

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static Properties emailProps;
    private static volatile boolean initialized = false;
    private static final Object LOCK = new Object();

    // Resolved configuration
    private static TransportMode transportMode = TransportMode.NONE;
    private static String resolvedApiKey;
    private static String resolvedFromEmail;
    private static String resolvedFromName;

    // SMTP fallback configuration
    private static String smtpHost;
    private static String smtpPort;
    private static String smtpUsername;
    private static String smtpPassword;

    private EmailService() {} // utility class

    /**
     * Lazily load email configuration prioritizing Brevo HTTPS API environment variables.
     */
    private static void loadConfig() {
        if (!initialized) {
            synchronized (LOCK) {
                if (!initialized) {
                    // 1. Probe Brevo API Key Environment Variables
                    String apiKey = getFromEnvOrSystem("BREVO_API_KEY", "EMAIL_API_KEY", "SENDINBLUE_API_KEY");

                    // 2. Probe Sender Email & Name
                    String fromEmail = getFromEnvOrSystem("SENDER_EMAIL", "EMAIL_FROM_ADDRESS", "EMAIL_FROM", "SMTP_FROM_EMAIL", "SMTP_FROM", "email.from.address");
                    String fromName = getFromEnvOrSystem("SENDER_NAME", "EMAIL_FROM_NAME", "SMTP_FROM_NAME", "SMTP_NAME", "email.from.name");

                    // 3. Probe SMTP Environment Variables
                    String host = getFromEnvOrSystem("SMTP_HOST", "email.smtp.host", "MAIL_HOST");
                    String port = getFromEnvOrSystem("SMTP_PORT", "email.smtp.port", "MAIL_PORT");
                    String smtpUser = getFromEnvOrSystem("SMTP_USERNAME", "EMAIL_USERNAME", "email.smtp.username", "MAIL_USERNAME");
                    String smtpPass = getFromEnvOrSystem("SMTP_PASSWORD", "EMAIL_PASSWORD", "email.smtp.password", "MAIL_PASSWORD");

                    // 4. Fallback to email.properties if nothing in env
                    if (apiKey == null && (smtpUser == null || smtpPass == null)) {
                        emailProps = loadFallbackProperties();
                        if (apiKey == null && emailProps != null) {
                            apiKey = emailProps.getProperty("brevo.api.key");
                            if (apiKey == null || apiKey.trim().isEmpty()) {
                                apiKey = emailProps.getProperty("email.api.key");
                            }
                        }
                        if (fromEmail == null && emailProps != null) {
                            fromEmail = emailProps.getProperty("email.from.address");
                        }
                        if (fromName == null && emailProps != null) {
                            fromName = emailProps.getProperty("email.from.name");
                        }
                        if (host == null && emailProps != null) {
                            host = emailProps.getProperty("email.smtp.host");
                        }
                        if (port == null && emailProps != null) {
                            port = emailProps.getProperty("email.smtp.port");
                        }
                        if (smtpUser == null && emailProps != null) {
                            smtpUser = emailProps.getProperty("email.smtp.username");
                        }
                        if (smtpPass == null && emailProps != null) {
                            smtpPass = emailProps.getProperty("email.smtp.password");
                        }
                    }

                    // Resolve From details with sensible defaults
                    resolvedFromName = (fromName != null && !fromName.trim().isEmpty()) ? fromName.trim() : "PRAGMATRIX 2026";
                    resolvedFromEmail = (fromEmail != null && !fromEmail.trim().isEmpty()) ? fromEmail.trim() : "pragmatrix2k26@gmail.com";

                    System.out.println("================================================================================");
                    System.out.println("[PRAGMATRIX-EMAIL] Initialising Email Delivery Service...");

                    if (apiKey != null && !apiKey.trim().isEmpty()) {
                        resolvedApiKey = apiKey.trim();
                        transportMode = TransportMode.HTTPS_BREVO;

                        String maskedKey = resolvedApiKey.length() > 8
                                ? resolvedApiKey.substring(0, 4) + "..." + resolvedApiKey.substring(resolvedApiKey.length() - 4) + " (length " + resolvedApiKey.length() + ")"
                                : "[SET (length " + resolvedApiKey.length() + ")]";

                        System.out.println("[PRAGMATRIX-EMAIL] Transport Mode       : HTTPS REST API (Brevo v3)");
                        System.out.println("[PRAGMATRIX-EMAIL] API Endpoint         : " + BREVO_API_URL);
                        System.out.println("[PRAGMATRIX-EMAIL] BREVO_API_KEY        : " + maskedKey);
                        System.out.println("[PRAGMATRIX-EMAIL] SENDER_EMAIL         : " + resolvedFromEmail);
                        System.out.println("[PRAGMATRIX-EMAIL] SENDER_NAME          : " + resolvedFromName);

                    } else if (smtpUser != null && !smtpUser.trim().isEmpty() && smtpPass != null && !smtpPass.trim().isEmpty()) {
                        transportMode = TransportMode.SMTP;
                        smtpHost = (host != null && !host.trim().isEmpty()) ? host.trim() : "smtp.gmail.com";
                        smtpPort = (port != null && !port.trim().isEmpty()) ? port.trim() : "587";
                        smtpUsername = smtpUser.trim();
                        smtpPassword = smtpPass.trim();
                        if (resolvedFromEmail.isEmpty()) {
                            resolvedFromEmail = smtpUsername;
                        }

                        System.out.println("[PRAGMATRIX-EMAIL] Transport Mode       : SMTP (Legacy / Local Fallback)");
                        System.out.println("[PRAGMATRIX-EMAIL] SMTP_HOST            : " + smtpHost);
                        System.out.println("[PRAGMATRIX-EMAIL] SMTP_PORT            : " + smtpPort);
                        System.out.println("[PRAGMATRIX-EMAIL] SMTP_USERNAME        : " + smtpUsername);
                        System.out.println("[PRAGMATRIX-EMAIL] SMTP_PASSWORD        : [SET (length " + smtpPassword.length() + ")]");
                        System.out.println("[PRAGMATRIX-EMAIL] SENDER_EMAIL         : " + resolvedFromEmail);
                        System.out.println("[PRAGMATRIX-EMAIL] SENDER_NAME          : " + resolvedFromName);

                    } else {
                        transportMode = TransportMode.NONE;
                        System.out.println("[PRAGMATRIX-EMAIL] Transport Mode       : NONE (No BREVO_API_KEY or SMTP credentials provided)");
                    }

                    System.out.println("================================================================================");
                    initialized = true;
                }
            }
        }
    }

    private static Properties loadFallbackProperties() {
        Properties props = new Properties();
        try (InputStream is = EmailService.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("[PRAGMATRIX-EMAIL] Loaded fallback configuration from classpath: email.properties");
            }
        } catch (IOException e) {
            System.err.println("[PRAGMATRIX-EMAIL] Failed to read email.properties: " + e.getMessage());
        }
        return props;
    }

    private static String getFromEnvOrSystem(String... keys) {
        if (keys == null) return null;
        for (String key : keys) {
            String val = System.getenv(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        for (String key : keys) {
            String val = System.getProperty(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }

    /**
     * Send registration confirmation email to a team lead.
     */
    public static boolean sendRegistrationConfirmationEmail(String toEmail, String teamLeadName, String collegeName, String quizName, String teamCode) {
        String publicAppUrl = getFromEnvOrSystem("PUBLIC_APP_URL", "APP_URL");
        if (publicAppUrl == null || publicAppUrl.trim().isEmpty()) {
            publicAppUrl = "https://pragmatrix.onrender.com";
        }

        String subject = "PRAGMATRIX 2026 \u2013 Registration Confirmed (" + teamCode + ")";

        String textBody = "Dear " + (teamLeadName != null && !teamLeadName.isEmpty() ? teamLeadName : "Team Lead") + ",\n\n"
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
                + "status, round results, and score updates on the PRAGMATRIX portal:\n"
                + publicAppUrl + "\n\n"
                + "Event Schedule:\n"
                + "Date:  24th August 2026\n"
                + "Time:  9:00 A.M. onwards\n"
                + "Venue: Conference Hall, Seshadripuram College, Bengaluru\n\n"
                + "We look forward to welcoming you and wish your team the very best!\n\n"
                + "Warm regards,\n"
                + "Team PRAGMATRIX 2026\n"
                + "Post Graduate Department of Business Administration\n"
                + "Seshadripuram College, Bengaluru";

        String htmlBody = "<!DOCTYPE html><html><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">"
                + "<div style=\"text-align: center; margin-bottom: 20px;\">"
                + "<h2 style=\"color: #1a1a2e; margin: 0;\">PRAGMATRIX 2026</h2>"
                + "<p style=\"color: #d4af37; font-weight: bold; margin: 5px 0;\">Applied Management Carnival</p>"
                + "<p style=\"font-size: 12px; color: #666; margin: 0;\">Seshadripuram College, Bengaluru</p>"
                + "</div>"
                + "<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">"
                + "<p>Dear <strong>" + (teamLeadName != null && !teamLeadName.isEmpty() ? teamLeadName : "Team Lead") + "</strong>,</p>"
                + "<p>Greetings from Team PRAGMATRIX 2026!</p>"
                + "<p>We are pleased to inform you that your registration for <strong>PRAGMATRIX 2026</strong> has been successfully confirmed.</p>"
                + "<div style=\"background: #f8f9fa; border-left: 4px solid #d4af37; padding: 15px; margin: 20px 0; border-radius: 4px;\">"
                + "<h3 style=\"margin-top: 0; color: #1a1a2e; font-size: 16px;\">Registration Details</h3>"
                + "<table style=\"width: 100%; border-collapse: collapse; font-size: 14px;\">"
                + "<tr><td style=\"padding: 4px 0; color: #666; width: 120px;\">Event:</td><td><strong>" + quizName + "</strong></td></tr>"
                + "<tr><td style=\"padding: 4px 0; color: #666;\">College:</td><td><strong>" + collegeName + "</strong></td></tr>"
                + "<tr><td style=\"padding: 4px 0; color: #666;\">Team Lead:</td><td><strong>" + teamLeadName + "</strong></td></tr>"
                + "<tr><td style=\"padding: 4px 0; color: #666;\">Team Code:</td><td><strong style=\"color: #4a154b; font-size: 16px;\">" + teamCode + "</strong></td></tr>"
                + "</table>"
                + "</div>"
                + "<p>Your <strong>Team Code (" + teamCode + ")</strong> is your credential to log in and monitor your live scores and standings on the portal:</p>"
                + "<p style=\"text-align: center; margin: 25px 0;\">"
                + "<a href=\"" + publicAppUrl + "\" style=\"background: #1a1a2e; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;\">Access Team Portal</a>"
                + "</p>"
                + "<p style=\"font-size: 13px; color: #555;\"><strong>Event Schedule:</strong><br>"
                + "Date: 24th August 2026 | Time: 9:00 A.M. onwards<br>"
                + "Venue: Conference Hall, Seshadripuram College, Bengaluru</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">"
                + "<p style=\"font-size: 12px; color: #888; text-align: center;\">"
                + "Warm regards,<br><strong>Team PRAGMATRIX 2026</strong><br>Post Graduate Department of Business Administration, Seshadripuram College"
                + "</p>"
                + "</div></body></html>";

        return sendEmail(toEmail, subject, textBody, htmlBody);
    }

    /**
     * Backward-compatible participant ID email sender.
     */
    public static boolean sendParticipantIdEmail(String toEmail, String participantId) {
        return sendRegistrationConfirmationEmail(toEmail, "Participant", "Registered Institution", "PRAGMATRIX 2026", participantId);
    }

    /**
     * Send 6-digit OTP email for Admin Login.
     */
    public static boolean sendAdminOtpEmail(String toEmail, String otpCode) {
        String subject = "PRAGMATRIX 2026 Admin Login OTP";

        String textBody = "Dear Admin,\n\n"
                + "Your 6-digit OTP for PRAGMATRIX 2026 Admin Login is:\n\n"
                + "      " + otpCode + "\n\n"
                + "This OTP is valid for 5 minutes. Do not share this code with anyone.\n\n"
                + "If you did not request this login code, please ignore this email.\n\n"
                + "Warm regards,\n"
                + "Team PRAGMATRIX 2026";

        String htmlBody = "<!DOCTYPE html><html><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                + "<div style=\"max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">"
                + "<div style=\"text-align: center; margin-bottom: 20px;\">"
                + "<h2 style=\"color: #1a1a2e; margin: 0;\">PRAGMATRIX 2026</h2>"
                + "<p style=\"color: #d4af37; font-weight: bold; margin: 5px 0;\">Admin Portal Authentication</p>"
                + "</div>"
                + "<p>Dear Admin,</p>"
                + "<p>Your 6-digit One-Time Password (OTP) for admin dashboard login is:</p>"
                + "<div style=\"background: #f4f4f6; text-align: center; padding: 18px; margin: 20px 0; border-radius: 6px; letter-spacing: 6px; font-size: 28px; font-weight: bold; color: #4a154b;\">"
                + otpCode
                + "</div>"
                + "<p style=\"font-size: 13px; color: #666;\">This OTP is valid for <strong>5 minutes</strong>. Do not share this code with anyone.</p>"
                + "<p style=\"font-size: 12px; color: #999;\">If you did not request this login code, please ignore this email or contact the administrator.</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">"
                + "<p style=\"font-size: 12px; color: #888; text-align: center;\">Team PRAGMATRIX 2026</p>"
                + "</div></body></html>";

        return sendEmail(toEmail, subject, textBody, htmlBody);
    }

    /**
     * Core email dispatch router (routes to Brevo HTTPS API or SMTP fallback).
     */
    private static boolean sendEmail(String toEmail, String subject, String textBody, String htmlBody) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("[PRAGMATRIX-EMAIL] Cannot send email: recipient address is empty.");
            return false;
        }

        loadConfig();

        if (transportMode == TransportMode.NONE) {
            System.err.println("[PRAGMATRIX-EMAIL] Email service unconfigured (no BREVO_API_KEY or SMTP credentials). Skipping send to: " + toEmail);
            return false;
        }

        if (transportMode == TransportMode.SMTP) {
            return sendViaSmtp(toEmail.trim(), subject, textBody, htmlBody);
        } else {
            return sendViaBrevoApi(toEmail.trim(), subject, textBody, htmlBody);
        }
    }

    /**
     * Dispatches email via Brevo Transactional Email REST API.
     * POST https://api.brevo.com/v3/smtp/email
     */
    private static boolean sendViaBrevoApi(String toEmail, String subject, String textBody, String htmlBody) {
        try {
            JsonObject payload = new JsonObject();

            // Sender object: {"name": "<SENDER_NAME>", "email": "<SENDER_EMAIL>"}
            JsonObject sender = new JsonObject();
            sender.addProperty("name", resolvedFromName);
            sender.addProperty("email", resolvedFromEmail);
            payload.add("sender", sender);

            // To array: [{"email": "<recipient>"}]
            JsonArray toArray = new JsonArray();
            JsonObject recipient = new JsonObject();
            recipient.addProperty("email", toEmail);
            toArray.add(recipient);
            payload.add("to", toArray);

            // Subject and HTML Content
            payload.addProperty("subject", subject);
            payload.addProperty("htmlContent", (htmlBody != null && !htmlBody.isEmpty()) ? htmlBody : "<p>" + textBody.replace("\n", "<br>") + "</p>");
            if (textBody != null && !textBody.isEmpty()) {
                payload.addProperty("textContent", textBody);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("accept", "application/json")
                    .header("api-key", resolvedApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("[PRAGMATRIX-EMAIL] Email sent successfully via Brevo API to: " + toEmail + " [HTTP " + statusCode + "]");
                return true;
            } else {
                System.err.println("[PRAGMATRIX-EMAIL] Brevo API email dispatch failed for " + toEmail + " [HTTP " + statusCode + "]: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("[PRAGMATRIX-EMAIL] Brevo API email exception for " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fallback SMTP sender (if Brevo API key is not configured).
     */
    private static boolean sendViaSmtp(String toEmail, String subject, String textBody, String htmlBody) {
        try {
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.auth", "true");
            mailProps.put("mail.smtp.starttls.enable", "true");
            mailProps.put("mail.smtp.host", smtpHost);
            mailProps.put("mail.smtp.port", smtpPort);
            mailProps.put("mail.smtp.ssl.trust", smtpHost);
            mailProps.put("mail.smtp.connectiontimeout", "8000");
            mailProps.put("mail.smtp.timeout", "10000");

            final String finalUsername = smtpUsername;
            final String finalPassword = smtpPassword;

            Session session = Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalUsername, finalPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(resolvedFromEmail, resolvedFromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            if (htmlBody != null && !htmlBody.isEmpty()) {
                MimeMultipart multipart = new MimeMultipart("alternative");

                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(textBody, "utf-8");
                multipart.addBodyPart(textPart);

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
                multipart.addBodyPart(htmlPart);

                message.setContent(multipart);
            } else {
                message.setText(textBody);
            }

            Transport.send(message);

            System.out.println("[PRAGMATRIX-EMAIL] Email sent successfully via SMTP to: " + toEmail + " | Subject: " + subject);
            return true;

        } catch (Exception e) {
            System.err.println("[PRAGMATRIX-EMAIL] SMTP send failed for " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
