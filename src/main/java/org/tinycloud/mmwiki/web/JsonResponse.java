package org.tinycloud.mmwiki.web;

import java.util.Map;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class JsonResponse<T> {

    private static final String DEFAULT_URL = "";
    private static final int DEFAULT_SLEEP = 2000;

    private int code;
    private Object message;
    private T data;
    private Map<String, Object> redirect;

    public static <T> JsonResponse<T> success(Object message) {
        return success(message, null, DEFAULT_URL, DEFAULT_SLEEP);
    }

    public static <T> JsonResponse<T> success(Object message, T data) {
        return success(message, data, DEFAULT_URL, DEFAULT_SLEEP);
    }

    public static <T> JsonResponse<T> success(Object message, String url) {
        return success(message, null, url, DEFAULT_SLEEP);
    }

    public static <T> JsonResponse<T> success(Object message, String url, int sleep) {
        return success(message, null, url, sleep);
    }

    public static <T> JsonResponse<T> success(Object message, T data, String url) {
        return success(message, data, url, DEFAULT_SLEEP);
    }

    public static <T> JsonResponse<T> success(Object message, T data, String url, int sleep) {
        JsonResponse<T> response = new JsonResponse<>();
        response.code = 1;
        response.message = message;
        response.data = data;
        response.redirect = Map.of("url", url == null ? "" : url, "sleep", sleep);
        return response;
    }

    public static <T> JsonResponse<T> error(Object message) {
        return error(message, null, DEFAULT_URL, DEFAULT_SLEEP);
    }

    public static <T> JsonResponse<T> error(Object message, String url) {
        return error(message, null, url, DEFAULT_SLEEP);
    }

    public static <T> JsonResponse<T> error(Object message, String url, int sleep) {
        return error(message, null, url, sleep);
    }

    public static <T> JsonResponse<T> error(Object message, T data, String url, int sleep) {
        JsonResponse<T> response = new JsonResponse<>();
        response.code = 0;
        response.message = message;
        response.data = data;
        response.redirect = Map.of("url", url == null ? "" : url, "sleep", sleep);
        return response;
    }

    public int getCode() {
        return code;
    }

    public Object getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Map<String, Object> getRedirect() {
        return redirect;
    }
}
