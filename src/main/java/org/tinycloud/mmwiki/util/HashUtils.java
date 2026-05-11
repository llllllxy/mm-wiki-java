package org.tinycloud.mmwiki.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希算法工具类。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public final class HashUtils {

    private HashUtils() {
    }

    /**
     * 计算MD5值。
     *
     * @param text 待计算的文本
     * @return MD5值
     */
    public static String md5(String text) {
        return digest("MD5", text);
    }

    /**
     * 计算SHA-256值。
     *
     * @param text 待计算的文本
     * @return SHA-256值
     */
    public static String sha256(String text) {
        return digest("SHA-256", text);
    }

    /**
     * 计算指定算法的摘要值。
     *
     * @param algorithm 算法名称
     * @param text      待计算的文本
     * @return 摘要值
     */
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
