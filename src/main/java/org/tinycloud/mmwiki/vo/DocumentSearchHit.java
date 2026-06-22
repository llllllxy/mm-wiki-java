package org.tinycloud.mmwiki.vo;

/**
 * 文档全文搜索命中结果，保存 Lucene 返回的文档ID、相关性得分和内容摘要。
 *
 * @author liuxingyu01
 * @since 2026-06-22
 */
public class DocumentSearchHit {

    private String documentId;
    private float score;
    private String snippet;

    /**
     * 创建空的文档搜索命中对象，供框架或测试按属性赋值。
     */
    public DocumentSearchHit() {
    }

    /**
     * 创建完整的文档搜索命中对象。
     *
     * @param documentId 文档ID
     * @param score      搜索得分
     * @param snippet    搜索摘要
     */
    public DocumentSearchHit(String documentId, float score, String snippet) {
        this.documentId = documentId;
        this.score = score;
        this.snippet = snippet;
    }

    /**
     * 获取命中的文档ID。
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * 设置命中的文档ID。
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * 获取搜索相关性得分。
     */
    public float getScore() {
        return score;
    }

    /**
     * 设置搜索相关性得分。
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * 获取搜索摘要。
     */
    public String getSnippet() {
        return snippet;
    }

    /**
     * 设置搜索摘要。
     */
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
