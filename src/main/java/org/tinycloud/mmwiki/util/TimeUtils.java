package org.tinycloud.mmwiki.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public final class TimeUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeUtils() {
    }

    /**
     * 将时间戳转为字符串。
     *
     * @param epochSeconds 时间戳
     * @return 时间字符串
     */
    public static String formatUnix(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return "";
        }
        return DEFAULT_FORMATTER.format(Instant.ofEpochSecond(epochSeconds.longValue()));
    }
}
