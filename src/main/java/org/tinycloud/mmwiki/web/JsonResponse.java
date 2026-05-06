package org.tinycloud.mmwiki.web;

import java.util.Map;

public class JsonResponse<T> {

    private int code;
    private Object message;
    private T data;
    private Map<String, Object> redirect;

    public static <T> JsonResponse<T> success(Object message, T data, String url, int sleep) {
        JsonResponse<T> response = new JsonResponse<>();
        response.code = 1;
        response.message = message;
        response.data = data;
        response.redirect = Map.of("url", url == null ? "" : url, "sleep", sleep);
        return response;
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
