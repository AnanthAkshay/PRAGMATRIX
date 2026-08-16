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
 * Supports both modern HTTPS-based Transactional Email APIs (Resend, Brevo, SendGrid,
 * or generic REST API) and legacy SMTP fallback.
 * </p>
 * <p>
 * HTTPS REST API delivery avoids port blocks (SMTP 25/465/587) on container platforms
 * like Render Free.
 * </p>
 * <p>
 * Configuration Precedence:
 * <ol>
 *   <li><b>HTTPS API via Environment Variables</b>: EMAIL_API_KEY / RESEND_API_KEY / BREVO_API_KEY / SENDGRID_API_KEY</li>
 *   <li><b>HTTPS API via Properties File</b>: {@code email.properties} ({@code email.api.key})</li>
 *   <li><b>SMTP via Environment Variables</b>: SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD</li>
 *   <li><b>SMTP via Properties File</b>: {@code email.properties}</li>
 * </ol>
 * </p>
 */
public class EmailService {

    public enum TransportMode {
        HTTPS_RESEND,
        HTTPS_BREVO,
        HTTPS_SENDGRID,
        HTTPS_CUSTOM,
        SMTP,
        NONE
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static Properties emailProps;
    private static volatile boolean initialized = false;
    private static final Object LOCK = new Object();

    // Resolved configuration
    private static TransportMode transportMode = TransportMode.NONE;
    private static String resolvedApiKey;
    private static String resolvedApiUrl;
    private static String resolvedFromEmail;
    private static String resolvedFromName;

    // SMTP fallback configuration
    private static String smtpHost;
    private static String smtpPort;
    private static String smtpUsername;
    private static String smtpPassword;

    private EmailService() {} // utility class

    /**
     * Lazily load email configuration prioritizing HTTPS API environment variables.
     */
    private static void loadConfig() {
        if (!initialized) {
            synchronized (LOCK) {
                if (!initialized) {
                    // 1. Probe HTTPS API Environment Variables
                    String apiKey = getFromEnvOrSystem("EMAIL_API_KEY", "RESEND_API_KEY", "BREVO_API_KEY", "SENDINBLUE_API_KEY", "SENDGRID_API_KEY");
                    String customApiUrl = getFromEnvOrSystem("EMAIL_API_URL");
                    String providerHint = getFromEnvOrSystem("EMAIL_PROVIDER");

                    // From Email & Name
                    String fromEmail = getFromEnvOrSystem("EMAIL_FROM_ADDRESS", "EMAIL_FROM", "SMTP_FROM_EMAIL", "SMTP_FROM", "email.from.address");
                    String fromName = getFromEnvOrSystem("EMAIL_FROM_NAME", "SMTP_FROM_NAME", "SMTP_NAME", "email.from.name");

                    // 2. Probe SMTP Environment Variables
                    String host = getFromEnvOrSystem("SMTP_HOST", "email.smtp.host", "MAIL_HOST");
                    String port = getFromEnvOrSystem("SMTP_PORT", "email.smtp.port", "MAIL_PORT");
                    String smtpUser = getFromEnvOrSystem("SMTP_USERNAME", "EMAIL_USERNAME", "email.smtp.username", "MAIL_USERNAME");
                    String smtpPass = getFromEnvOrSystem("SMTP_PASSWORD", "EMAIL_PASSWORD", "email.smtp.password", "MAIL_PASSWORD");

                    // 3. Fallback to email.properties if nothing in env
                    if (apiKey == null && (smtpUser == null || smtpPass == null)) {
                        emailProps = loadFallbackProperties();
                        if (apiKey == null && emailProps != null) {
                            apiKey = emailProps.getProperty("email.api.key");
                            if (apiKey == null || apiKey.trim().isEmpty()) {
                                apiKey = emailProps.getProperty("resend.api.key");
                            }
                        }
                        if (customApiUrl == null && emailProps != null) {
                            customApiUrl = emailProps.getProperty("email.api.url");
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

                    // Resolve From details
                    resolvedFromName = (fromName != null && !fromName.trim().isEmpty()) ? fromName.trim() : "PRAGMATRIX 2026";
                    resolvedFromEmail = (fromEmail != null && !fromEmail.trim().isEmpty()) ? fromEmail.trim() : "";

                    System.out.println("================================================================================");
                    System.out.println("[PRAGMATRIX-EMAIL] Initialising Email Delivery Service...");

                    if (apiKey != null && !apiKey.trim().isEmpty()) {
                        resolvedApiKey = apiKey.trim();

                        if (customApiUrl != null && !customApiUrl.trim().isEmpty()) {
                            transportMode = TransportMode.HTTPS_CUSTOM;
                            resolvedApiUrl = customApiUrl.trim();
                        } else if ("brevo".equalsIgnoreCase(providerHint) || "sendinblue".equalsIgnoreCase(providerHint)
                                || resolvedApiKey.startsWith("xkeysib-")
                                || System.getenv("BREVO_API_KEY") != null
                                || System.getenv("SENDINBLUE_API_KEY") != null) {
                            transportMode = TransportMode.HTTPS_BREVO;
                            resolvedApiUrl = "https://api.brevo.com/v3/smtp/email";
                        } else if ("sendgrid".equalsIgnoreCase(providerHint)
                                || resolvedApiKey.startsWith("SG.")
                                || System.getenv("SENDGRID_API_KEY") != null) {
                            transportMode = TransportMode.HTTPS_SENDGRID;
                            resolvedApiUrl = "https://api.sendgrid.com/v3/mail/send";
                        } else {
                            // Default modern HTTPS provider: Resend
                            transportMode = TransportMode.HTTPS_RESEND;
                            resolvedApiUrl = "https://api.resend.com/emails";
                            if (resolvedFromEmail.isEmpty()) {
                                resolvedFromEmail = "onboarding@resend.dev";
                            }
                        }

                        if (resolvedFromEmail.isEmpty()) {
                            resolvedFromEmail = "pragmatrix2k26@gmail.com";
                        }

                        System.out.println("[PRAGMATRIX-EMAIL] Transport Mode       : HTTPS REST API (" + transportMode + ")");
                        System.out.println("[PRAGMATRIX-EMAIL] API Endpoint         : " + resolvedApiUrl);
                        System.out.println("[PRAGMATRIX-EMAIL] EMAIL_API_KEY        : [SET (length " + resolvedApiKey.length() + ")]");
                        System.out.println("[PRAGMATRIX-EMAIL] EMAIL_FROM_ADDRESS   : " + resolvedFromEmail);
                        System.out.println("[PRAGMATRIX-EMAIL] EMAIL_FROM_NAME      : " + resolvedFromName);

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
                        System.out.println("[PRAGMATRIX-EMAIL] SMTP_FROM_EMAIL      : " + resolvedFromEmail);
                        System.out.println("[PRAGMATRIX-EMAIL] SMTP_FROM_NAME       : " + resolvedFromName);

                    } else {
                        transportMode = TransportMode.NONE;
                        System.out.println("[PRAGMATRIX-EMAIL] Transport Mode       : NONE (No EMAIL_API_KEY or SMTP credentials provided)");
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
     * Send 6-digit OTP email for Admin Login.
     */
    public static boolean sendAdminOtpEmail(String toEmail, String otpCode) {
        String subject = "PRAGMATRIX 2026 Admin Login OTP";
        String body = "Dear Admin,\n\n"
                + "Your 6-digit OTP for PRAGMATRIX 2026 Admin Login is:\n\n"
                + "      " + otpCode + "\n\n"
                + "This OTP is valid for 5 minutes. Do not share this code with anyone.\n\n"
                + "If you did not request this login code, please ignore this email.\n\n"
                + "Warm regards,\n"
                + "Team PRAGMATRIX 2026";
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Core email dispatch router (routes to HTTPS API or SMTP fallback).
     */
    private static boolean sendEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("[PRAGMATRIX-EMAIL] Cannot send email: recipient address is empty.");
            return false;
        }

        loadConfig();

        if (transportMode == TransportMode.NONE) {
            System.err.println("[PRAGMATRIX-EMAIL] Email service unconfigured (no EMAIL_API_KEY or SMTP credentials). Skipping send to: " + toEmail);
            return false;
        }

        if (transportMode == TransportMode.SMTP) {
            return sendViaSmtp(toEmail.trim(), subject, body);
        } else {
            return sendViaHttpsApi(toEmail.trim(), subject, body);
        }
    }

    /**
     * Dispatches email via HTTPS REST API with 10s connect and 15s request timeouts.
     */
    private static boolean sendViaHttpsApi(String toEmail, String subject, String body) {
        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(resolvedApiUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json");

            JsonObject payload;

            switch (transportMode) {
                case HTTPS_BREVO:
                    reqBuilder.header("api-key", resolvedApiKey);
                    payload = buildBrevoPayload(toEmail, subject, body);
                    break;

                case HTTPS_SENDGRID:
                    reqBuilder.header("Authorization", "Bearer " + resolvedApiKey);
                    payload = buildSendGridPayload(toEmail, subject, body);
                    break;

                case HTTPS_RESEND:
                case HTTPS_CUSTOM:
                default:
                    reqBuilder.header("Authorization", "Bearer " + resolvedApiKey);
                    payload = buildResendPayload(toEmail, subject, body);
                    break;
            }

            HttpRequest request = reqBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("[PRAGMATRIX-EMAIL] Email sent successfully via HTTPS API (" + transportMode + ") to: " + toEmail + " [HTTP " + statusCode + "]");
                return true;
            } else {
                System.err.println("[PRAGMATRIX-EMAIL] HTTPS email dispatch failed for " + toEmail + " [HTTP " + statusCode + "]: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("[PRAGMATRIX-EMAIL] HTTPS email exception for " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static JsonObject buildResendPayload(String toEmail, String subject, String body) {
        JsonObject json = new JsonObject();
        String fromHeader = (resolvedFromName != null && !resolvedFromName.isEmpty())
                ? resolvedFromName + " <" + resolvedFromEmail + ">"
                : resolvedFromEmail;
        json.addProperty("from", fromHeader);

        JsonArray toArray = new JsonArray();
        toArray.add(toEmail);
        json.add("to", toArray);

        json.addProperty("subject", subject);
        json.addProperty("text", body);
        return json;
    }

    private static JsonObject buildBrevoPayload(String toEmail, String subject, String body) {
        JsonObject json = new JsonObject();

        JsonObject sender = new JsonObject();
        sender.addProperty("name", resolvedFromName);
        sender.addProperty("email", resolvedFromEmail);
        json.add("sender", sender);

        JsonArray toArray = new JsonArray();
        JsonObject recipient = new JsonObject();
        recipient.addProperty("email", toEmail);
        toArray.add(recipient);
        json.add("to", toArray);

        json.addProperty("subject", subject);
        json.addProperty("textContent", body);
        return json;
    }

    private static JsonObject buildSendGridPayload(String toEmail, String subject, String body) {
        JsonObject json = new JsonObject();

        JsonArray personalizations = new JsonArray();
        JsonObject p = new JsonObject();
        JsonArray toArray = new JsonArray();
        JsonObject recipient = new JsonObject();
        recipient.addProperty("email", toEmail);
        toArray.add(recipient);
        p.add("to", toArray);
        personalizations.add(p);
        json.add("personalizations", personalizations);

        JsonObject fromObj = new JsonObject();
        fromObj.addProperty("email", resolvedFromEmail);
        fromObj.addProperty("name", resolvedFromName);
        json.add("from", fromObj);

        json.addProperty("subject", subject);

        JsonArray contentArray = new JsonArray();
        JsonObject c = new JsonObject();
        c.addProperty("type", "text/plain");
        c.addProperty("value", body);
        contentArray.add(c);
        json.add("content", contentArray);

        return json;
    }

    /**
     * Fallback SMTP sender (if HTTPS API is not configured).
     */
    private static boolean sendViaSmtp(String toEmail, String subject, String body) {
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
            message.setText(body);

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
