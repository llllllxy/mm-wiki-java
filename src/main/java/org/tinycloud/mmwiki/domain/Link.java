package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 系统链接信息实体。
 *
 * <p>对应数据库表：mw_link。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Link {

    /**
     * 链接ID
     */
    private Integer linkId;
    /**
     * 链接名称
     */
    private String name;
    /**
     * 链接地址
     */
    private String url;
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

    public Integer getLinkId() {
        return linkId;
    }

    public void setLinkId(Integer linkId) {
        this.linkId = linkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

