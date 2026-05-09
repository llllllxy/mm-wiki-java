package org.tinycloud.mmwiki.vo;

import org.tinycloud.mmwiki.domain.Document;

/**
 * UserFollowedDocument view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class UserFollowedDocument {

    /**
     * document.
     */
    private Document document;

    /**
     * followId.
     */
    private Integer followId;

    public UserFollowedDocument() {
    }

    public UserFollowedDocument(
            Document document,
            Integer followId
    ) {
        this.document = document;
        this.followId = followId;
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

}
