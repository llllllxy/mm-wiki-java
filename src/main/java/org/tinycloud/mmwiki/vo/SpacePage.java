package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * SpacePage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class SpacePage {

    /**
     * spaces.
     */
    private List<Space> spaces;

    /**
     * count.
     */
    private long count;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * paginator.
     */
    private Paginator paginator;

    public SpacePage() {
    }

    public SpacePage(
            List<Space> spaces,
            long count,
            String keyword,
            Paginator paginator
    ) {
        this.spaces = spaces;
        this.count = count;
        this.keyword = keyword;
        this.paginator = paginator;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<Space> spaces) {
        this.spaces = spaces;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
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
