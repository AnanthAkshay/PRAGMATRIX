package com.pragmatrix.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 */
public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;

    private PasswordUtil() {} // utility class

    /**
     * Hash a plain-text password with BCrypt.
     *
     * @param plainPassword the plain-text password
     * @return the BCrypt hash string
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verify a plain-text password against a BCrypt hash.
     *
     * @param plainPassword the plain-text password to check
     * @param hashedPassword the stored BCrypt hash
     * @return true if the password matches the hash
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
