package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.User;

/**
 * SharedPageView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class SharedPageView {

    /**
     * document.
     */
    private Document document;

    /**
     * parentDocuments.
     */
    private List<Document> parentDocuments;

    /**
     * pageContent.
     */
    private String pageContent;

    /**
     * createUser.
     */
    private User createUser;

    /**
     * editUser.
     */
    private User editUser;

    public SharedPageView() {
    }

    public SharedPageView(
            Document document,
            List<Document> parentDocuments,
            String pageContent,
            User createUser,
            User editUser
    ) {
        this.document = document;
        this.parentDocuments = parentDocuments;
        this.pageContent = pageContent;
        this.createUser = createUser;
        this.editUser = editUser;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public List<Document> getParentDocuments() {
        return parentDocuments;
    }

    public void setParentDocuments(List<Document> parentDocuments) {
        this.parentDocuments = parentDocuments;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }

    public User getCreateUser() {
        return createUser;
    }

    public void setCreateUser(User createUser) {
        this.createUser = createUser;
    }

    public User getEditUser() {
        return editUser;
    }

    public void setEditUser(User editUser) {
        this.editUser = editUser;
    }

}
