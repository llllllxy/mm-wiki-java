package org.tinycloud.mmwiki.vo;

import org.tinycloud.mmwiki.domain.User;

/**
 * MemberView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class MemberView {

    /**
     * user.
     */
    private User user;

    /**
     * privilege.
     */
    private Integer privilege;

    /**
     * spaceUserId.
     */
    private Integer spaceUserId;

    public MemberView() {
    }

    public MemberView(User user, Integer privilege, Integer spaceUserId) {
        this.user = user;
        this.privilege = privilege;
        this.spaceUserId = spaceUserId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Integer privilege) {
        this.privilege = privilege;
    }

    public Integer getSpaceUserId() {
        return spaceUserId;
    }

    public void setSpaceUserId(Integer spaceUserId) {
        this.spaceUserId = spaceUserId;
    }

}
