package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 权限信息实体。
 *
 * <p>对应数据库表：mw_privilege。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Privilege {

    /**
     * 权限ID
     */
    private Integer privilegeId;
    /**
     * 权限名称
     */
    private String name;
    /**
     * 父级权限ID
     */
    private Integer parentId;
    /**
     * 权限类型
     */
    private String type;
    /**
     * 控制器名称
     */
    private String controller;
    /**
     * 方法名称
     */
    private String action;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 链接打开目标
     */
    private String target;
    /**
     * 是否展示，0否1是
     */
    private Integer isDisplay;
    /**
     * 排序序号
     */
    private Integer sequence;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Integer getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(Integer privilegeId) {
        this.privilegeId = privilegeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(Integer isDisplay) {
        this.isDisplay = isDisplay;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
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

