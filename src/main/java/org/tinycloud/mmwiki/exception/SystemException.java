package org.tinycloud.mmwiki.exception;

import org.tinycloud.mmwiki.constant.ErrorCodeEnum;

/**
 * <p>
 * 自定义运行时异常，携带前端业务错误码和可选跳转地址。
 * </p>
 *
 * @author liuxingyu01
 * @since 2026/5/13 21:57
 */
public class SystemException extends RuntimeException {

    // 前端 JsonResponse 使用的业务错误码。
    private final ErrorCodeEnum errorCode;

    // 异常处理后需要前端跳转时使用的目标地址。
    private final String url;

    // 默认业务异常，code 使用 ErrorCodeEnum.ERROR。
    public SystemException(String message) {
        this(ErrorCodeEnum.FAILURE, message, null);
    }

    // 默认业务异常，并携带前端跳转地址。
    public SystemException(String message, String url) {
        this(ErrorCodeEnum.FAILURE, message, url);
    }

    // 自定义业务错误码，不需要跳转地址。
    public SystemException(ErrorCodeEnum errorCode, String message) {
        this(errorCode, message, null);
    }

    // 自定义业务错误码，并携带前端跳转地址。
    public SystemException(ErrorCodeEnum errorCode, String message, String url) {
        super(message);
        this.errorCode = errorCode == null ? ErrorCodeEnum.FAILURE : errorCode;
        this.url = url;
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }

    public String getUrl() {
        return url;
    }

}