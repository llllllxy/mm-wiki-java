package org.tinycloud.mmwiki.domain;

/**
 * MM-Wiki 数据模型。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class LogEntry {

    private Long logId;
    private Integer level;
    private String path;
    private String get;
    private String post;
    private String message;
    private String ip;
    private String userAgent;
    private String referer;
    private Integer userId;
    private String username;
    private Integer createTime;
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
