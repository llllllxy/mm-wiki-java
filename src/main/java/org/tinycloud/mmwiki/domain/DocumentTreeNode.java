package org.tinycloud.mmwiki.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 文档目录树节点模型。
 *
 * <p>基于 mw_document 构建 zTree 节点数据，无直接对应数据库表。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class DocumentTreeNode {

    /**
     * 树节点ID
     */
    private String id;
    /**
     * 父级树节点ID
     */
    @JsonProperty("pId")
    private String pId;
    /**
     * 树节点名称
     */
    private String name;
    /**
     * 空间ID
     */
    private Integer spaceId;
    /**
     * 是否默认展开
     */
    private boolean open;
    /**
     * 是否父节点
     */
    @JsonProperty("isParent")
    private boolean isParent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("pId")
    public String getPId() {
        return pId;
    }

    public void setPId(String pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @JsonProperty("isParent")
    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }
}
