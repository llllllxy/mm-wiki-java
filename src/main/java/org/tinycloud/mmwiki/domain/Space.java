package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 空间信息实体。
 *
 * <p>对应数据库表：mw_space。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Space {

    /**
     * 空间ID
     */
    private Integer spaceId;
    /**
     * 空间名称
     */
    private String name;
    /**
     * 空间描述
     */
    private String description;
    /**
     * 空间标签
     */
    private String tags;
    /**
     * 访问级别
     */
    private String visitLevel;
    /**
     * 是否允许分享，0否1是
     */
    private Integer isShare;
    /**
     * 是否允许导出，0否1是
     */
    private Integer isExport;
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
     * 当前用户是否收藏，页面展示扩展字段
     */
    private boolean collection;
    /**
     * 当前用户收藏ID，页面展示扩展字段
     */
    private Integer collectionId;
    /**
     * 创建日期文本，页面展示扩展字段
     */
    private String createDateText;

    public Integer getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getVisitLevel() {
        return visitLevel;
    }

    public void setVisitLevel(String visitLevel) {
        this.visitLevel = visitLevel;
    }

    public Integer getIsShare() {
        return isShare;
    }

    public void setIsShare(Integer isShare) {
        this.isShare = isShare;
    }

    public Integer getIsExport() {
        return isExport;
    }

    public void setIsExport(Integer isExport) {
        this.isExport = isExport;
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

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public String getCreateDateText() {
        return createDateText;
    }

    public void setCreateDateText(String createDateText) {
        this.createDateText = createDateText;
    }
}

