package org.tinycloud.mmwiki.web;

import java.io.Serial;
import java.io.Serializable;
import org.tinycloud.mmwiki.domain.User;

public class CurrentUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String username;
    private String passwordHash;
    private Integer roleId;
    private String givenName;

    public static CurrentUser from(User user) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(user.getUserId());
        currentUser.setUsername(user.getUsername());
        currentUser.setPasswordHash(user.getPassword());
        currentUser.setRoleId(user.getRoleId());
        currentUser.setGivenName(user.getGivenName());
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
}
