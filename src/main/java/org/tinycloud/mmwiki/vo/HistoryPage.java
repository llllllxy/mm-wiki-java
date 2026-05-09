package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.DocumentHistoryView;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * HistoryPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class HistoryPage {

    /**
     * items.
     */
    private List<DocumentHistoryView> items;

    /**
     * paginator.
     */
    private Paginator paginator;

    public HistoryPage() {
    }

    public HistoryPage(
            List<DocumentHistoryView> items,
            Paginator paginator
    ) {
        this.items = items;
        this.paginator = paginator;
    }

    public List<DocumentHistoryView> getItems() {
        return items;
    }

    public void setItems(List<DocumentHistoryView> items) {
        this.items = items;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
