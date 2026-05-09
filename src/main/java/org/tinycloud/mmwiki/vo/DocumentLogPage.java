package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * DocumentLogPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class DocumentLogPage {

    /**
     * logDocuments.
     */
    private List<LogDocumentView> logDocuments;

    /**
     * users.
     */
    private List<User> users;

    /**
     * userId.
     */
    private Integer userId;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * paginator.
     */
    private Paginator paginator;

    public DocumentLogPage() {
    }

    public DocumentLogPage(
            List<LogDocumentView> logDocuments,
            List<User> users,
            Integer userId,
            String keyword,
            Paginator paginator
    ) {
        this.logDocuments = logDocuments;
        this.users = users;
        this.userId = userId;
        this.keyword = keyword;
        this.paginator = paginator;
    }

    public List<LogDocumentView> getLogDocuments() {
        return logDocuments;
    }

    public void setLogDocuments(List<LogDocumentView> logDocuments) {
        this.logDocuments = logDocuments;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
