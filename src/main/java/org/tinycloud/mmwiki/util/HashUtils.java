package org.tinycloud.mmwiki.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MM-Wiki 通用工具类。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public final class HashUtils {

    private HashUtils() {
    }

    public static String md5(String text) {
        return digest("MD5", text);
    }

    public static String sha256(String text) {
        return digest("SHA-256", text);
    }

    private static String digest(String algorithm, String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Missing digest algorithm: " + algorithm, ex);
        }
    }

    public static void main(String[] args) {
        System.out.println(sha256("123456"));
    }
}
