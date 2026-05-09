package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.User;

/**
 * DocumentViewData view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class DocumentViewData {

    /**
     * space.
     */
    private Space space;

    /**
     * document.
     */
    private Document document;

    /**
     * spaceDocument.
     */
    private Document spaceDocument;

    /**
     * documents.
     */
    private List<Document> documents;

    /**
     * parentDocuments.
     */
    private List<Document> parentDocuments;

    /**
     * createUser.
     */
    private User createUser;

    /**
     * editUser.
     */
    private User editUser;

    /**
     * pageContent.
     */
    private String pageContent;

    /**
     * collectionId.
     */
    private Integer collectionId;

    /**
     * editor.
     */
    private boolean editor;

    /**
     * manager.
     */
    private boolean manager;

    public DocumentViewData() {
    }

    public DocumentViewData(
            Space space,
            Document document,
            Document spaceDocument,
            List<Document> documents,
            List<Document> parentDocuments,
            User createUser,
            User editUser,
            String pageContent,
            Integer collectionId,
            boolean editor,
            boolean manager
    ) {
        this.space = space;
        this.document = document;
        this.spaceDocument = spaceDocument;
        this.documents = documents;
        this.parentDocuments = parentDocuments;
        this.createUser = createUser;
        this.editUser = editUser;
        this.pageContent = pageContent;
        this.collectionId = collectionId;
        this.editor = editor;
        this.manager = manager;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getSpaceDocument() {
        return spaceDocument;
    }

    public void setSpaceDocument(Document spaceDocument) {
        this.spaceDocument = spaceDocument;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<Document> getParentDocuments() {
        return parentDocuments;
    }

    public void setParentDocuments(List<Document> parentDocuments) {
        this.parentDocuments = parentDocuments;
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

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public boolean isEditor() {
        return editor;
    }

    public void setEditor(boolean editor) {
        this.editor = editor;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

}
