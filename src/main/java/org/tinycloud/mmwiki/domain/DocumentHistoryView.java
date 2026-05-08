package org.tinycloud.mmwiki.domain;

/**
 * 文档历史记录视图模型。
 *
 * <p>基于 mw_log_document 关联 mw_user 查询生成。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class DocumentHistoryView {

    /**
     * 文档日志ID
     */
    private Integer logDocumentId;
    /**
     * 文档ID
     */
    private String documentId;
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
    private Integer createTime;
    /**
     * 操作用户名，页面展示扩展字段
     */
    private String username;
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

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCreateTimeText() {
        return createTimeText;
    }

    public void setCreateTimeText(String createTimeText) {
        this.createTimeText = createTimeText;
    }
}
