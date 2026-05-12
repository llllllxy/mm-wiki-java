package org.tinycloud.mmwiki.vo;

import org.tinycloud.mmwiki.domain.User;

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
     * count.
     */
    private int count;

    /**
     * autoFollowDoc.
     */
    private String autoFollowDoc;

    public FollowDocPage() {
    }

    public FollowDocPage(User user, int count, String autoFollowDoc) {
        this.user = user;
        this.count = count;
        this.autoFollowDoc = autoFollowDoc;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
