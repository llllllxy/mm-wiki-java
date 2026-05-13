package org.tinycloud.mmwiki.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;

import java.util.Locale;

/**
 * HTTP 请求工具类。
 *
 * @author liuxingyu01
 * @since 2026-05-13
 */
public final class RequestUtils {

    private RequestUtils() {
    }

    /**
     * 判断当前请求是否更适合返回 JSON。
     * <p>
     * XHR、JSON 请求、明确只接受 JSON 的请求，以及非 GET 提交都按异步/接口请求处理。
     *
     * @param request 当前请求
     * @return true 表示返回 JSON，false 表示返回页面或重定向
     */
    public static boolean expectsJsonResponse(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }

        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        String accept = request.getHeader("Accept");
        if (accept != null
                && accept.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE)
                && !accept.toLowerCase(Locale.ROOT).contains(MediaType.TEXT_HTML_VALUE)) {
            return true;
        }

        return !"GET".equalsIgnoreCase(request.getMethod());
    }
}
