package org.tinycloud.mmwiki.web;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class EditorImageResponse {

    private int success;
    private String message;
    private String url;

    public static EditorImageResponse success(String message, String url) {
        EditorImageResponse response = new EditorImageResponse();
        response.success = 1;
        response.message = message;
        response.url = url;
        return response;
    }

    public static EditorImageResponse error(String message) {
        EditorImageResponse response = new EditorImageResponse();
        response.success = 0;
        response.message = message;
        response.url = "";
        return response;
    }

    public int getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }
}
