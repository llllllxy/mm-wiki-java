package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;

/**
 * FollowUserListPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class FollowUserListPage {

    /**
     * users.
     */
    private List<User> users;

    /**
     * count.
     */
    private int count;

    public FollowUserListPage() {
    }

    public FollowUserListPage(
            List<User> users,
            int count
    ) {
        this.users = users;
        this.count = count;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
