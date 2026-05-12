package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 登录认证配置实体。
 *
 * <p>对应数据库表：mw_login_auth。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class LoginAuth {

    /**
     * 登录认证ID
     */
    private Integer loginAuthId;
    /**
     * 登录认证名称
     */
    private String name;
    /**
     * 用户名前缀
     */
    private String usernamePrefix;
    /**
     * 认证接口地址
     */
    private String url;
    /**
     * 扩展配置数据
     */
    private String extData;
    /**
     * 是否启用，0否1是
     */
    private Integer isUsed;
    /**
     * 是否删除，0否1是
     */
    private Integer isDelete;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Integer getLoginAuthId() {
        return loginAuthId;
    }

    public void setLoginAuthId(Integer loginAuthId) {
        this.loginAuthId = loginAuthId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsernamePrefix() {
        return usernamePrefix;
    }

    public void setUsernamePrefix(String usernamePrefix) {
        this.usernamePrefix = usernamePrefix;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExtData() {
        return extData;
    }

    public void setExtData(String extData) {
        this.extData = extData;
    }

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

