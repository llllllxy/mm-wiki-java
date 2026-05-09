package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * ActivityPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class ActivityPage {

    /**
     * logDocuments.
     */
    private List<LogDocumentView> logDocuments;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * paginator.
     */
    private Paginator paginator;

    public ActivityPage() {
    }

    public ActivityPage(
            List<LogDocumentView> logDocuments,
            String keyword,
            Paginator paginator
    ) {
        this.logDocuments = logDocuments;
        this.keyword = keyword;
        this.paginator = paginator;
    }

    public List<LogDocumentView> getLogDocuments() {
        return logDocuments;
    }

    public void setLogDocuments(List<LogDocumentView> logDocuments) {
        this.logDocuments = logDocuments;
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
