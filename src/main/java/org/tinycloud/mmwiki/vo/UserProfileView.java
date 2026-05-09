package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;

/**
 * UserProfileView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class UserProfileView {

    /**
     * user.
     */
    private User user;

    /**
     * logDocuments.
     */
    private List<LogDocumentView> logDocuments;

    /**
     * count.
     */
    private int count;

    public UserProfileView() {
    }

    public UserProfileView(
            User user,
            List<LogDocumentView> logDocuments,
            int count
    ) {
        this.user = user;
        this.logDocuments = logDocuments;
        this.count = count;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<LogDocumentView> getLogDocuments() {
        return logDocuments;
    }

    public void setLogDocuments(List<LogDocumentView> logDocuments) {
        this.logDocuments = logDocuments;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
