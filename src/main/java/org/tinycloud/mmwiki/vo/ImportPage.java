package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * ImportPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class ImportPage {

    /**
     * users.
     */
    private List<User> users;

    /**
     * username.
     */
    private String username;

    /**
     * paginator.
     */
    private Paginator paginator;

    public ImportPage() {
    }

    public ImportPage(
            List<User> users,
            String username,
            Paginator paginator
    ) {
        this.users = users;
        this.username = username;
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

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
