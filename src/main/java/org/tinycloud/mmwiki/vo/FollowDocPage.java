package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * FollowDocPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class FollowDocPage {

    /**
     * user.
     */
    private User user;

    /**
     * followDocuments.
     */
    private List<ProfileFollowedDocument> followDocuments;

    /**
     * count.
     */
    private int count;

    /**
     * autoFollowDoc.
     */
    private String autoFollowDoc;

    /**
     * paginator.
     */
    private Paginator paginator;

    public FollowDocPage() {
    }

    public FollowDocPage(
            User user,
            List<ProfileFollowedDocument> followDocuments,
            int count,
            String autoFollowDoc,
            Paginator paginator
    ) {
        this.user = user;
        this.followDocuments = followDocuments;
        this.count = count;
        this.autoFollowDoc = autoFollowDoc;
        this.paginator = paginator;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ProfileFollowedDocument> getFollowDocuments() {
        return followDocuments;
    }

    public void setFollowDocuments(List<ProfileFollowedDocument> followDocuments) {
        this.followDocuments = followDocuments;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAutoFollowDoc() {
        return autoFollowDoc;
    }

    public void setAutoFollowDoc(String autoFollowDoc) {
        this.autoFollowDoc = autoFollowDoc;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
