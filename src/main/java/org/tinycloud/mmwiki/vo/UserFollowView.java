package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.User;

/**
 * UserFollowView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class UserFollowView {

    /**
     * user.
     */
    private User user;

    /**
     * users.
     */
    private List<User> users;

    /**
     * fansUsers.
     */
    private List<User> fansUsers;

    /**
     * followCount.
     */
    private int followCount;

    /**
     * fansCount.
     */
    private int fansCount;

    /**
     * loginUserId.
     */
    private Integer loginUserId;

    public UserFollowView() {
    }

    public UserFollowView(
            User user,
            List<User> users,
            List<User> fansUsers,
            int followCount,
            int fansCount,
            Integer loginUserId
    ) {
        this.user = user;
        this.users = users;
        this.fansUsers = fansUsers;
        this.followCount = followCount;
        this.fansCount = fansCount;
        this.loginUserId = loginUserId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<User> getFansUsers() {
        return fansUsers;
    }

    public void setFansUsers(List<User> fansUsers) {
        this.fansUsers = fansUsers;
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public int getFansCount() {
        return fansCount;
    }

    public void setFansCount(int fansCount) {
        this.fansCount = fansCount;
    }

    public Integer getLoginUserId() {
        return loginUserId;
    }

    public void setLoginUserId(Integer loginUserId) {
        this.loginUserId = loginUserId;
    }

}
