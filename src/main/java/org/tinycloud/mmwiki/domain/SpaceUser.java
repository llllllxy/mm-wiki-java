package org.tinycloud.mmwiki.domain;

public class SpaceUser {

    private Integer spaceUserId;
    private Integer userId;
    private Integer spaceId;
    private Integer privilege;
    private Integer createTime;
    private Integer updateTime;

    public Integer getSpaceUserId() {
        return spaceUserId;
    }

    public void setSpaceUserId(Integer spaceUserId) {
        this.spaceUserId = spaceUserId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }

    public Integer getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Integer privilege) {
        this.privilege = privilege;
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
