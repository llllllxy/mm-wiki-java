package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;

/**
 * MemberPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class MemberPage {

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

    public MemberPage(boolean manager, List<User> otherUsers) {
        this.manager = manager;
        this.otherUsers = otherUsers;
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
