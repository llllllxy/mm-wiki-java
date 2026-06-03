package org.tinycloud.mmwiki.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.tinycloud.mmwiki.web.JsonResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * HTTP 请求工具类。
 *
 * @author liuxingyu01
 * @since 2026-05-13
 */
public final class WebUtils {
    private final static Logger logger = LoggerFactory.getLogger(WebUtils.class);


    private WebUtils() {
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

    /**
     * 响应 JSON 信息
     *
     * @param response 当前响应
     * @param body     信息
     */
    public static void writeJson(HttpServletResponse response, JsonResponse<?> body) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter out = response.getWriter()) {
            out.write(JsonUtils.writeValueAsString(body));
        } catch (IOException e) {
            logger.error("Failed to write JSON response", e);
        }
    }

    /**
     * 响应 JSON 信息
     *
     * @param response 当前响应
     * @param json     信息
     */
    public static void writeJson(HttpServletResponse response, String json) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        } catch (IOException e) {
            logger.error("Failed to write JSON response", e);
        }
    }
}
