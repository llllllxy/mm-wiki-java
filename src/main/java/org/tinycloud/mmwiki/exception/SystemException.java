package org.tinycloud.mmwiki.exception;

/**
 * <p>
 * 自定义运行时异常 属性：message、url
 * </p>
 *
 * @author liuxingyu01
 * @since 2026/5/13 21:57
 */
public class SystemException extends RuntimeException {

    // 四个自定义字段
    private final String url;

    // 全参构造：四个参数一次性传入
    public SystemException(String message, String url) {
        super(message);
        this.url = url;
    }

    // 可选：方便重载的构造器
    public SystemException(String message) {
        super(message);
        this.url = null;
    }

    public String getUrl() {
        return url;
    }

}