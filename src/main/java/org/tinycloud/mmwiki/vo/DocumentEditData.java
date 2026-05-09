package org.tinycloud.mmwiki.vo;

import org.tinycloud.mmwiki.domain.Document;

/**
 * DocumentEditData view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class DocumentEditData {

    /**
     * document.
     */
    private Document document;

    /**
     * pageContent.
     */
    private String pageContent;

    /**
     * sendEmail.
     */
    private String sendEmail;

    /**
     * autoFollowDoc.
     */
    private String autoFollowDoc;

    public DocumentEditData() {
    }

    public DocumentEditData(
            Document document,
            String pageContent,
            String sendEmail,
            String autoFollowDoc
    ) {
        this.document = document;
        this.pageContent = pageContent;
        this.sendEmail = sendEmail;
        this.autoFollowDoc = autoFollowDoc;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }

    public String getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(String sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getAutoFollowDoc() {
        return autoFollowDoc;
    }

    public void setAutoFollowDoc(String autoFollowDoc) {
        this.autoFollowDoc = autoFollowDoc;
    }

}
