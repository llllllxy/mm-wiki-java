package org.tinycloud.mmwiki.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private TimeUtils() {
    }

    public static String formatUnix(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return "";
        }
        return DEFAULT_FORMATTER.format(Instant.ofEpochSecond(epochSeconds.longValue()));
    }
}
