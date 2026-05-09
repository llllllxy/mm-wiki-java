package org.tinycloud.mmwiki.vo;

/**
 * TopUser view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class TopUser {

    /**
     * userId.
     */
    private Integer userId;

    /**
     * username.
     */
    private String username;

    public TopUser() {
    }

    public TopUser(
            Integer userId,
            String username
    ) {
        this.userId = userId;
        this.username = username;
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

}
