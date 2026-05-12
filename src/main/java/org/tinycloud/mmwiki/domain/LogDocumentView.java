package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 文档操作日志视图模型。
 *
 * <p>基于 mw_log_document 关联 mw_document、mw_user 查询生成。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class LogDocumentView {

    /**
     * 文档日志ID
     */
    private Integer logDocumentId;
    /**
     * 文档ID
     */
    private String documentId;
    /**
     * 空间ID
     */
    private Integer spaceId;
    /**
     * 操作用户ID
     */
    private Integer userId;
    /**
     * 操作类型
     */
    private Integer action;
    /**
     * 操作备注
     */
    private String comment;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 操作用户名，页面展示扩展字段
     */
    private String username;
    /**
     * 操作用户姓名，页面展示扩展字段
     */
    private String givenName;
    /**
     * 文档名称，页面展示扩展字段
     */
    private String documentName;
    /**
     * 文档类型，页面展示扩展字段
     */
    private Integer documentType;
    /**
     * 创建时间文本，页面展示扩展字段
     */
    private String createTimeText;

    public Integer getLogDocumentId() {
        return logDocumentId;
    }

    public void setLogDocumentId(Integer logDocumentId) {
        this.logDocumentId = logDocumentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Integer getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Integer getDocumentType() {
        return documentType;
    }

    public void setDocumentType(Integer documentType) {
        this.documentType = documentType;
    }

    public String getCreateTimeText() {
        return createTimeText;
    }

    public void setCreateTimeText(String createTimeText) {
        this.createTimeText = createTimeText;
    }
}
