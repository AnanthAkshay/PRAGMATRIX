package com.pragmatrix.util;

import java.security.SecureRandom;

/**
 * Utility for generating and masking OTPs.
 */
public class OtpUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OtpUtil() {} // utility class

    /**
     * Generate a 6-digit numeric OTP using a cryptographically secure random source.
     *
     * @return a 6-digit OTP string (e.g. "047291")
     */
    public static String generateOtp() {
        int otp = SECURE_RANDOM.nextInt(900000) + 100000; // 100000–999999
        return String.valueOf(otp);
    }

    /**
     * Mask an email address for display (e.g. "teamlead@gmail.com" → "t*******@gmail.com").
     *
     * @param email the full email address
     * @return the masked email, or the original if format is unexpected
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (local.length() <= 1) {
            return local + "***" + domain;
        }
        return local.charAt(0) + "***" + domain;
    }
}
