package org.tinycloud.mmwiki.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {

    private HashUtils() {
    }

    public static String md5(String text) {
        return digest("MD5", text);
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
}
