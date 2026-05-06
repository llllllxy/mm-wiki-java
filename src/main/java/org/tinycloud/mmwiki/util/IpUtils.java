package org.tinycloud.mmwiki.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * MM-Wiki 通用工具类。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public final class IpUtils {

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
