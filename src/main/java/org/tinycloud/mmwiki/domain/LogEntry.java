package org.tinycloud.mmwiki.domain;

/**
 * 系统日志实体。
 *
 * <p>对应数据库表：mw_log。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class LogEntry {

    /**
     * 系统日志ID
     */
    private Long logId;
    /**
     * 日志级别
     */
    private Integer level;
    /**
     * 请求路径
     */
    private String path;
    /**
     * GET请求参数
     */
    private String get;
    /**
     * POST请求参数
     */
    private String post;
    /**
     * 日志消息
     */
    private String message;
    /**
     * 请求IP
     */
    private String ip;
    /**
     * 用户代理
     */
    private String userAgent;
    /**
     * 请求来源
     */
    private String referer;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 创建时间
     */
    private Integer createTime;
    /**
     * 创建时间文本，页面展示扩展字段
     */
    private String createTimeText;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGet() {
        return get;
    }

    public void setGet(String get) {
        this.get = get;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String getCreateTimeText() {
        return createTimeText;
    }

    public void setCreateTimeText(String createTimeText) {
        this.createTimeText = createTimeText;
    }

    public String getLevelText() {
        return switch (level == null ? 0 : level) {
            case 3 -> "ERROR";
            case 4 -> "WARNING";
            case 6 -> "INFO";
            case 7 -> "DEBUG";
            default -> String.valueOf(level == null ? "" : level);
        };
    }

    public String getLevelCss() {
        return switch (level == null ? 0 : level) {
            case 3 -> "label-danger";
            case 4 -> "label-warning";
            case 6 -> "label-info";
            default -> "label-default";
        };
    }
}
