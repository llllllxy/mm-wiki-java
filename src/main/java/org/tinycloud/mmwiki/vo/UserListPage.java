package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * UserListPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class UserListPage {

    /**
     * users.
     */
    private List<User> users;

    /**
     * username.
     */
    private String username;

    /**
     * count.
     */
    private long count;

    /**
     * paginator.
     */
    private Paginator paginator;

    public UserListPage() {
    }

    public UserListPage(
            List<User> users,
            String username,
            long count,
            Paginator paginator
    ) {
        this.users = users;
        this.username = username;
        this.count = count;
        this.paginator = paginator;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
