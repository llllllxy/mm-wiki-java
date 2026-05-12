package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 角色信息实体。
 *
 * <p>对应数据库表：mw_role。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Role {

    /**
     * 角色ID
     */
    private Integer roleId;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色类型
     */
    private Integer type;
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
    /**
     * 更新时间文本，页面展示扩展字段
     */
    private String updateTimeText;

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public String getUpdateTimeText() {
        return updateTimeText;
    }

    public void setUpdateTimeText(String updateTimeText) {
        this.updateTimeText = updateTimeText;
    }
}

