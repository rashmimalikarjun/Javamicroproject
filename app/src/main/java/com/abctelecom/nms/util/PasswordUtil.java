package com.abctelecom.nms.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PBKDF2WithHmacSHA256 password hashing. Stored format:
 *   PBKDF2$&lt;iterations&gt;$&lt;base64 salt&gt;$&lt;base64 hash&gt;
 * Plaintext passwords are never stored or logged.
 */
public final class PasswordUtil {
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RNG = new SecureRandom();

    private PasswordUtil() {}

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        Base64.Encoder enc = Base64.getEncoder();
        return "PBKDF2$" + ITERATIONS + "$" + enc.encodeToString(salt) + "$" + enc.encodeToString(hash);
    }

    /** Constant-time-ish verification (MessageDigest.isEqual used inside). */
    public static boolean verify(String password, String stored) {
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 4 || !"PBKDF2".equals(parts[0])) return false;
            int iters = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iters, expected.length * 8);
            return java.security.MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] pwd, byte[] salt, int iterations, int bits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(pwd, salt, iterations, bits);
            return SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        }
    }

    /** Basic strength policy: >=8 chars, letter+digit. */
    public static boolean isStrongEnough(String pwd) {
        if (pwd == null || pwd.length() < 8) return false;
        boolean letter = pwd.chars().anyMatch(Character::isLetter);
        boolean digit = pwd.chars().anyMatch(Character::isDigit);
        return letter && digit;
    }
}
