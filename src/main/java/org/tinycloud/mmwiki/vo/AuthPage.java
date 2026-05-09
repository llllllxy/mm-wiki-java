package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * AuthPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class AuthPage {

    /**
     * auths.
     */
    private List<LoginAuth> auths;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * paginator.
     */
    private Paginator paginator;

    public AuthPage() {
    }

    public AuthPage(
            List<LoginAuth> auths,
            String keyword,
            Paginator paginator
    ) {
        this.auths = auths;
        this.keyword = keyword;
        this.paginator = paginator;
    }

    public List<LoginAuth> getAuths() {
        return auths;
    }

    public void setAuths(List<LoginAuth> auths) {
        this.auths = auths;
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
