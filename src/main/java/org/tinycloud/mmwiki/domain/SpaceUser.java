package org.tinycloud.mmwiki.domain;

/**
 * 空间用户权限关系实体。
 *
 * <p>对应数据库表：mw_space_user。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class SpaceUser {

    /**
     * 空间用户关系ID
     */
    private Integer spaceUserId;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 空间ID
     */
    private Integer spaceId;
    /**
     * 空间权限
     */
    private Integer privilege;
    /**
     * 创建时间
     */
    private Integer createTime;
    /**
     * 更新时间
     */
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
