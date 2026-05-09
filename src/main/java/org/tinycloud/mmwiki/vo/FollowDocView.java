package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;

/**
 * FollowDocView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class FollowDocView {

    /**
     * user.
     */
    private User user;

    /**
     * pages.
     */
    private List<UserFollowedDocument> pages;

    /**
     * count.
     */
    private int count;

    public FollowDocView() {
    }

    public FollowDocView(
            User user,
            List<UserFollowedDocument> pages,
            int count
    ) {
        this.user = user;
        this.pages = pages;
        this.count = count;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<UserFollowedDocument> getPages() {
        return pages;
    }

    public void setPages(List<UserFollowedDocument> pages) {
        this.pages = pages;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
