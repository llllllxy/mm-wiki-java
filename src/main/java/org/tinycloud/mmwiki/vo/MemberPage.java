package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MemberPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class MemberPage {

    /**
     * users.
     */
    private List<MemberView> users;

    /**
     * paginator.
     */
    private Paginator paginator;

    /**
     * manager.
     */
    private boolean manager;

    /**
     * otherUsers.
     */
    private List<User> otherUsers;

    public MemberPage() {
    }

    public MemberPage(
            List<MemberView> users,
            Paginator paginator,
            boolean manager,
            List<User> otherUsers
    ) {
        this.users = users;
        this.paginator = paginator;
        this.manager = manager;
        this.otherUsers = otherUsers;
    }

    public List<MemberView> getUsers() {
        return users;
    }

    public void setUsers(List<MemberView> users) {
        this.users = users;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

    public List<User> getOtherUsers() {
        return otherUsers;
    }

    public void setOtherUsers(List<User> otherUsers) {
        this.otherUsers = otherUsers;
    }

}
