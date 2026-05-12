package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 附件信息实体。
 *
 * <p>对应数据库表：mw_attachment。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Attachment {

    /**
     * 附件ID
     */
    private Integer attachmentId;
    /**
     * 上传用户ID
     */
    private Integer userId;
    /**
     * 关联文档ID
     */
    private String documentId;
    /**
     * 附件名称
     */
    private String name;
    /**
     * 附件存储路径
     */
    private String path;
    /**
     * 附件来源
     */
    private Integer source;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 上传用户名，页面展示扩展字段
     */
    private String username;

    public Integer getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Integer attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

