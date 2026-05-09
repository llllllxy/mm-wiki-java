package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Link;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * LinkPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class LinkPage {

    /**
     * links.
     */
    private List<Link> links;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * paginator.
     */
    private Paginator paginator;

    public LinkPage() {
    }

    public LinkPage(
            List<Link> links,
            String keyword,
            Paginator paginator
    ) {
        this.links = links;
        this.keyword = keyword;
        this.paginator = paginator;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
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
