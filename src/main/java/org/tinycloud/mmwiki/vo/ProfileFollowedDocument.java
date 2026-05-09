package org.tinycloud.mmwiki.vo;

import org.tinycloud.mmwiki.domain.Document;

/**
 * ProfileFollowedDocument view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class ProfileFollowedDocument {

    /**
     * document.
     */
    private Document document;

    /**
     * followId.
     */
    private Integer followId;

    /**
     * updateTimeText.
     */
    private String updateTimeText;

    public ProfileFollowedDocument() {
    }

    public ProfileFollowedDocument(
            Document document,
            Integer followId,
            String updateTimeText
    ) {
        this.document = document;
        this.followId = followId;
        this.updateTimeText = updateTimeText;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Integer getFollowId() {
        return followId;
    }

    public void setFollowId(Integer followId) {
        this.followId = followId;
    }

    public String getUpdateTimeText() {
        return updateTimeText;
    }

    public void setUpdateTimeText(String updateTimeText) {
        this.updateTimeText = updateTimeText;
    }

}
