package org.tinycloud.mmwiki.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MM-Wiki 数据模型。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class DocumentTreeNode {

    private String id;
    @JsonProperty("pId")
    private String pId;
    private String name;
    private Integer spaceId;
    private boolean open;
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
