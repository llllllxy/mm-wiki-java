package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Role;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * RolePage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class RolePage {

    /**
     * roles.
     */
    private List<Role> roles;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * paginator.
     */
    private Paginator paginator;

    public RolePage() {
    }

    public RolePage(
            List<Role> roles,
            String keyword,
            Paginator paginator
    ) {
        this.roles = roles;
        this.keyword = keyword;
        this.paginator = paginator;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
