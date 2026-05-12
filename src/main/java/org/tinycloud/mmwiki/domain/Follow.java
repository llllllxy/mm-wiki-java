package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 关注信息实体。
 *
 * <p>对应数据库表：mw_follow。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Follow {

    /**
     * 关注ID
     */
    private Integer followId;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 关注类型
     */
    private Integer type;
    /**
     * 关注对象ID
     */
    private String objectId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public Integer getFollowId() {
        return followId;
    }

    public void setFollowId(Integer followId) {
        this.followId = followId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

