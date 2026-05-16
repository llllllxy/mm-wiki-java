package org.tinycloud.mmwiki.constant;

/**
 * 系统统一错误码枚举。
 * <p>
 * 后端接口统一返回 HTTP 200，前端通过 JsonResponse.code 判断业务结果，
 * 所以常用错误码集中放在这里，避免散落在 JsonResponse 或业务代码中。
 *
 * @author liuxingyu01
 * @since 2026-05-16
 */
public enum ErrorCodeEnum {

    /**
     * 请求处理成功。
     */
    SUCCESS(1, "成功"),

    /**
     * 默认业务错误。
     */
    FAILURE(0, "失败"),

    /**
     * 请求参数不合法。
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未登录或登录已失效。
     */
    UNAUTHORIZED(401, "未登录或登录已失效"),

    /**
     * 当前用户没有权限访问资源。
     */
    FORBIDDEN(403, "没有权限"),

    /**
     * 请求资源不存在。
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 服务端处理异常。
     */
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String desc;

    ErrorCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
