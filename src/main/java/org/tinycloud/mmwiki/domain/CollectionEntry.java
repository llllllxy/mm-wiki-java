package org.tinycloud.mmwiki.domain;

/**
 * 收藏信息实体。
 *
 * <p>对应数据库表：mw_collection。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class CollectionEntry {

    /**
     * 收藏ID
     */
    private Integer collectionId;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 收藏类型
     */
    private Integer type;
    /**
     * 收藏资源ID
     */
    private String resourceId;
    /**
     * 创建时间
     */
    private Integer createTime;

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}
