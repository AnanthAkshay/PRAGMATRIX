package com.pragmatrix.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility for sending emails via SMTP (Jakarta Mail / Angus Mail).
 * <p>
 * Configuration precedence:
 * <ol>
 *   <li><b>OS Environment Variables (System.getenv)</b>: SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM_EMAIL, SMTP_FROM_NAME</li>
 *   <li><b>JVM System Properties (System.getProperty)</b>: -DSMTP_HOST=... or -Demail.smtp.host=...</li>
 *   <li><b>Classpath Properties file</b>: {@code email.properties}</li>
 * </ol>
 * All send operations are wrapped in try/catch — failures are logged
 * server-side and never break calling code.
 * </p>
 */
public class EmailService {

    private static Properties emailProps;
    private static volatile boolean initialized = false;
    private static final Object LOCK = new Object();

    private static String resolvedHost;
    private static String resolvedPort;
    private static String resolvedUsername;
    private static String resolvedPassword;
    private static String resolvedFromEmail;
    private static String resolvedFromName;

    private EmailService() {} // utility class

    /**
     * Lazily load email configuration prioritizing System.getenv() first.
     */
    private static void loadConfig() {
        if (!initialized) {
            synchronized (LOCK) {
                if (!initialized) {
                    // Step 1: Probe Environment Variables (System.getenv) & JVM System Properties directly first
                    String host = getFromEnvOrSystem("SMTP_HOST", "email.smtp.host", "MAIL_HOST");
                    String port = getFromEnvOrSystem("SMTP_PORT", "email.smtp.port", "MAIL_PORT");
                    String username = getFromEnvOrSystem("SMTP_USERNAME", "EMAIL_USERNAME", "email.smtp.username", "MAIL_USERNAME");
                    String password = getFromEnvOrSystem("SMTP_PASSWORD", "EMAIL_PASSWORD", "email.smtp.password", "MAIL_PASSWORD");
                    String fromEmail = getFromEnvOrSystem("SMTP_FROM_EMAIL", "EMAIL_FROM", "SMTP_FROM", "email.from.address");
                    String fromName = getFromEnvOrSystem("SMTP_FROM_NAME", "EMAIL_FROM_NAME", "SMTP_NAME", "email.from.name");

                    boolean usingEnvVars = (username != null && !username.trim().isEmpty() && password != null && !password.trim().isEmpty());

                    System.out.println("================================================================================");
                    System.out.println("[PRAGMATRIX-EMAIL] Initialising Email (SMTP) Configuration...");

                    if (usingEnvVars) {
                        System.out.println("[PRAGMATRIX-EMAIL] Configuration Source : ENVIRONMENT VARIABLES (System.getenv)");
                        resolvedHost = (host != null && !host.trim().isEmpty()) ? host.trim() : "smtp.gmail.com";
                        resolvedPort = (port != null && !port.trim().isEmpty()) ? port.trim() : "587";
                        resolvedUsername = username.trim();
                        resolvedPassword = password.trim();
                        resolvedFromEmail = (fromEmail != null && !fromEmail.trim().isEmpty()) ? fromEmail.trim() : resolvedUsername;
                        resolvedFromName = (fromName != null && !fromName.trim().isEmpty()) ? fromName.trim() : "PRAGMATRIX 2026";
                    } else {
                        System.out.println("[PRAGMATRIX-EMAIL] No SMTP credentials in environment variables. Checking email.properties...");
                        emailProps = loadFallbackProperties();

                        resolvedHost = getFromEnvOrSystemOrProps("SMTP_HOST", "email.smtp.host", emailProps, "smtp.gmail.com");
                        resolvedPort = getFromEnvOrSystemOrProps("SMTP_PORT", "email.smtp.port", emailProps, "587");
                        resolvedUsername = getFromEnvOrSystemOrProps("SMTP_USERNAME", "email.smtp.username", emailProps, "");
                        if (resolvedUsername.isEmpty()) {
                            resolvedUsername = getFromEnvOrSystemOrProps("EMAIL_USERNAME", "email.smtp.username", emailProps, "");
                        }
                        resolvedPassword = getFromEnvOrSystemOrProps("SMTP_PASSWORD", "email.smtp.password", emailProps, "");
                        if (resolvedPassword.isEmpty()) {
                            resolvedPassword = getFromEnvOrSystemOrProps("EMAIL_PASSWORD", "email.smtp.password", emailProps, "");
                        }
                        resolvedFromEmail = getFromEnvOrSystemOrProps("SMTP_FROM_EMAIL", "email.from.address", emailProps, resolvedUsername);
                        if (resolvedFromEmail == null || resolvedFromEmail.isEmpty()) {
                            resolvedFromEmail = getFromEnvOrSystemOrProps("EMAIL_FROM", "email.from.address", emailProps, resolvedUsername);
                        }
                        resolvedFromName = getFromEnvOrSystemOrProps("SMTP_FROM_NAME", "email.from.name", emailProps, "PRAGMATRIX 2026");
                        if (resolvedFromName == null || resolvedFromName.isEmpty()) {
                            resolvedFromName = getFromEnvOrSystemOrProps("EMAIL_FROM_NAME", "email.from.name", emailProps, "PRAGMATRIX 2026");
                        }

                        if (!resolvedUsername.isEmpty() && !resolvedPassword.isEmpty()) {
                            System.out.println("[PRAGMATRIX-EMAIL] Configuration Source : PROPERTIES FILE (email.properties)");
                        } else {
                            System.out.println("[PRAGMATRIX-EMAIL] Configuration Source : NONE (SMTP credentials missing)");
                        }
                    }

                    System.out.println("[PRAGMATRIX-EMAIL] SMTP_HOST            : " + resolvedHost);
                    System.out.println("[PRAGMATRIX-EMAIL] SMTP_PORT            : " + resolvedPort);
                    System.out.println("[PRAGMATRIX-EMAIL] SMTP_USERNAME        : " + (!resolvedUsername.isEmpty() ? resolvedUsername : "[NOT SET]"));
                    System.out.println("[PRAGMATRIX-EMAIL] SMTP_PASSWORD        : " + (!resolvedPassword.isEmpty() ? "[SET (length " + resolvedPassword.length() + ")]" : "[NOT SET]"));
                    System.out.println("[PRAGMATRIX-EMAIL] SMTP_FROM_EMAIL      : " + (!resolvedFromEmail.isEmpty() ? resolvedFromEmail : "[NOT SET]"));
                    System.out.println("[PRAGMATRIX-EMAIL] SMTP_FROM_NAME       : " + resolvedFromName);
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

    private static String getFromEnvOrSystemOrProps(String envKey, String propKey, Properties props, String defaultValue) {
        String val = getFromEnvOrSystem(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        if (props != null) {
            val = props.getProperty(propKey);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
            val = props.getProperty(envKey);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return defaultValue;
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
     * Core email-sending method.
     */
    private static boolean sendEmail(String toEmail, String subject, String body) {
        loadConfig();

        if (resolvedUsername == null || resolvedUsername.isEmpty() || resolvedPassword == null || resolvedPassword.isEmpty()) {
            System.err.println("[PRAGMATRIX-EMAIL] SMTP username/password not configured. Skipping send to: " + toEmail);
            return false;
        }

        try {
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.auth", "true");
            mailProps.put("mail.smtp.starttls.enable", "true");
            mailProps.put("mail.smtp.host", resolvedHost);
            mailProps.put("mail.smtp.port", resolvedPort);
            mailProps.put("mail.smtp.ssl.trust", resolvedHost);

            final String finalUsername = resolvedUsername;
            final String finalPassword = resolvedPassword;

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

            System.out.println("[PRAGMATRIX-EMAIL] Email sent successfully to: " + toEmail + " | Subject: " + subject);
            return true;

        } catch (Exception e) {
            System.err.println("[PRAGMATRIX-EMAIL] Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
