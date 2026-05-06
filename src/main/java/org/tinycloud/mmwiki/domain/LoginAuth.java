package org.tinycloud.mmwiki.domain;

public class LoginAuth {

    private Integer loginAuthId;
    private String name;
    private String usernamePrefix;
    private String url;
    private String extData;
    private Integer isUsed;
    private Integer isDelete;
    private Integer createTime;
    private Integer updateTime;

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
