package org.tinycloud.mmwiki.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * NanoId 工具类。
 *
 * <p>默认使用数字 + 小写字母字符集：0123456789abcdefghijklmnopqrstuvwxyz。</p>
 *
 * <p>这样设计的原因是：很多 MySQL 数据库默认 collation 是大小写不敏感的，
 * 例如 utf8mb4_general_ci、utf8mb4_unicode_ci、utf8mb4_0900_ai_ci。
 * 如果使用 NanoID 默认的大小写混合字符集，数据库主键或唯一索引在判重时可能会把 abc 和 ABC
 * 识别为同一个值，从而导致实际可用 ID 空间变小，甚至出现唯一键冲突。</p>
 *
 * <p>使用纯小写 Base36 字符集后，可以避免依赖数据库大小写敏感配置。
 * 默认长度设置为 24 位，随机空间约等于 124 bit，略高于 UUID v4 的 122 bit，
 * 适合用作对外不可预测的业务 ID。</p>
 */
public final class NanoIdUtils {

    /**
     * 默认安全随机数生成器。
     */
    private static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();

    /**
     * 默认字符集：数字 + 小写字母，避免数据库大小写不敏感导致唯一索引判重问题。
     */
    private static final char[] DEFAULT_ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * 默认 ID 长度，24 位 Base36 随机空间略高于 UUID v4。
     */
    private static final int DEFAULT_SIZE = 24;

    private NanoIdUtils() {
    }

    /**
     * 生成默认长度的 NanoId。
     *
     * @return 24 位数字加小写字母组成的随机 ID
     */
    public static String randomNanoId() {
        return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, DEFAULT_SIZE);
    }

    /**
     * 生成指定长度的 NanoId。
     *
     * @param size ID 长度，必须大于 0
     * @return 指定长度的数字加小写字母随机 ID
     */
    public static String randomNanoId(final int size) {
        return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, size);
    }

    /**
     * 根据指定随机数生成器、字符集和长度生成 NanoId。
     *
     * @param random   随机数生成器，安全场景建议使用 SecureRandom
     * @param alphabet ID 字符集，字符数量必须在 1 到 255 之间
     * @param size     ID 长度，必须大于 0
     * @return 随机生成的 NanoId
     */
    private static String randomNanoId(final Random random, final char[] alphabet, final int size) {
        if (random == null) {
            throw new IllegalArgumentException("random cannot be null.");
        }
        if (alphabet == null) {
            throw new IllegalArgumentException("alphabet cannot be null.");
        }
        if (alphabet.length == 0 || alphabet.length >= 256) {
            throw new IllegalArgumentException("alphabet must contain between 1 and 255 symbols.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero.");
        }

        final int mask = (2 << (int) Math.floor(Math.log(alphabet.length - 1) / Math.log(2))) - 1;
        final int step = (int) Math.ceil(1.6 * mask * size / alphabet.length);

        final StringBuilder idBuilder = new StringBuilder(size);
        final byte[] bytes = new byte[step];

        while (true) {
            random.nextBytes(bytes);

            for (int i = 0; i < step; i++) {
                final int alphabetIndex = bytes[i] & mask;

                if (alphabetIndex < alphabet.length) {
                    idBuilder.append(alphabet[alphabetIndex]);

                    if (idBuilder.length() == size) {
                        return idBuilder.toString();
                    }
                }
            }
        }
    }
}