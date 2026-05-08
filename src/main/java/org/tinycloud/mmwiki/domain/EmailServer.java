package org.tinycloud.mmwiki.domain;

/**
 * 邮件服务器配置实体。
 *
 * <p>对应数据库表：mw_email。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class EmailServer {

    /**
     * 邮件服务器ID
     */
    private Integer emailId;
    /**
     * 邮件服务器名称
     */
    private String name;
    /**
     * 发件邮箱地址
     */
    private String senderAddress;
    /**
     * 发件人名称
     */
    private String senderName;
    /**
     * 邮件标题前缀
     */
    private String senderTitlePrefix;
    /**
     * SMTP服务器地址
     */
    private String host;
    /**
     * SMTP服务器端口
     */
    private Integer port;
    /**
     * SMTP登录用户名
     */
    private String username;
    /**
     * SMTP登录密码或授权码
     */
    private String password;
    /**
     * 是否启用SSL，0否1是
     */
    private Integer isSsl;
    /**
     * 是否启用，0否1是
     */
    private Integer isUsed;
    /**
     * 创建时间
     */
    private Integer createTime;
    /**
     * 更新时间
     */
    private Integer updateTime;

    public Integer getEmailId() {
        return emailId;
    }

    public void setEmailId(Integer emailId) {
        this.emailId = emailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderTitlePrefix() {
        return senderTitlePrefix;
    }

    public void setSenderTitlePrefix(String senderTitlePrefix) {
        this.senderTitlePrefix = senderTitlePrefix;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIsSsl() {
        return isSsl;
    }

    public void setIsSsl(Integer isSsl) {
        this.isSsl = isSsl;
    }

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Integer getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }
}
