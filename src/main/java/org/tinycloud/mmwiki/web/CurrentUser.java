package org.tinycloud.mmwiki.web;

import java.io.Serial;
import java.io.Serializable;
import org.tinycloud.mmwiki.domain.User;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class CurrentUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String username;
    private Integer roleId;
    private String givenName;
    private long statusRefreshTime;

    public static CurrentUser from(User user) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(user.getUserId());
        currentUser.setUsername(user.getUsername());
        currentUser.setRoleId(user.getRoleId());
        currentUser.setGivenName(user.getGivenName());
        currentUser.setStatusRefreshTime(System.currentTimeMillis());
        return currentUser;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public long getStatusRefreshTime() {
        return statusRefreshTime;
    }

    public void setStatusRefreshTime(long statusRefreshTime) {
        this.statusRefreshTime = statusRefreshTime;
    }
}
