package org.tinycloud.mmwiki.web;

import java.util.ArrayList;
import java.util.List;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Paginator {

    private final long nums;
    private final int currentPage;
    private final int perPageNums;
    private final int totalPages;
    private final boolean hasPrev;
    private final boolean hasNext;
    private final String pageLinkFirst;
    private final String pageLinkPrev;
    private final String pageLinkNext;
    private final String pageLinkLast;
    private final List<PageLink> pages;
    private final List<PerPageOption> perPageOptions;

    public static Paginator of(int currentPage, int perPage, long totalCount, String basePath) {
        int safePerPage = Math.max(1, perPage);
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / safePerPage);
        int safeCurrentPage = totalPages == 0 ? 1 : Math.min(Math.max(currentPage, 1), totalPages);
        boolean hasPrev = safeCurrentPage > 1;
        boolean hasNext = safeCurrentPage < totalPages;

        List<PageLink> pageLinks = new ArrayList<>();
        int start = Math.max(1, safeCurrentPage - 2);
        int end = Math.min(totalPages, safeCurrentPage + 2);
        for (int page = start; page <= end; page++) {
            pageLinks.add(new PageLink(page, buildLink(basePath, page, safePerPage), page == safeCurrentPage));
        }

        List<PerPageOption> options = new ArrayList<>();
        for (int option : List.of(10, 20, 50, 100)) {
            options.add(new PerPageOption(option, buildLink(basePath, 1, option), option == safePerPage));
        }

        return new Paginator(
            totalCount,
            safeCurrentPage,
            safePerPage,
            totalPages,
            hasPrev,
            hasNext,
            buildLink(basePath, 1, safePerPage),
            buildLink(basePath, Math.max(1, safeCurrentPage - 1), safePerPage),
            buildLink(basePath, Math.min(Math.max(totalPages, 1), safeCurrentPage + 1), safePerPage),
            buildLink(basePath, Math.max(totalPages, 1), safePerPage),
            pageLinks,
            options
        );
    }

    private static String buildLink(String basePath, int page, int perPage) {
        String separator = basePath.contains("?") ? "&" : "?";
        return basePath + separator + "page=" + page + "&number=" + perPage;
    }

    private Paginator(
        long nums,
        int currentPage,
        int perPageNums,
        int totalPages,
        boolean hasPrev,
        boolean hasNext,
        String pageLinkFirst,
        String pageLinkPrev,
        String pageLinkNext,
        String pageLinkLast,
        List<PageLink> pages,
        List<PerPageOption> perPageOptions
    ) {
        this.nums = nums;
        this.currentPage = currentPage;
        this.perPageNums = perPageNums;
        this.totalPages = totalPages;
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
        this.pageLinkFirst = pageLinkFirst;
        this.pageLinkPrev = pageLinkPrev;
        this.pageLinkNext = pageLinkNext;
        this.pageLinkLast = pageLinkLast;
        this.pages = pages;
        this.perPageOptions = perPageOptions;
    }

    public long getNums() {
        return nums;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPerPageNums() {
        return perPageNums;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public String getPageLinkFirst() {
        return pageLinkFirst;
    }

    public String getPageLinkPrev() {
        return pageLinkPrev;
    }

    public String getPageLinkNext() {
        return pageLinkNext;
    }

    public String getPageLinkLast() {
        return pageLinkLast;
    }

    public List<PageLink> getPages() {
        return pages;
    }

    public List<PerPageOption> getPerPageOptions() {
        return perPageOptions;
    }

    public static class PageLink {
        private final int page;
        private final String url;
        private final boolean active;

        public PageLink(int page, String url, boolean active) {
            this.page = page;
            this.url = url;
            this.active = active;
        }

        public int getPage() {
            return page;
        }

        public String getUrl() {
            return url;
        }

        public boolean isActive() {
            return active;
        }
    }

    public static class PerPageOption {
        private final int value;
        private final String url;
        private final boolean selected;

        public PerPageOption(int value, String url, boolean selected) {
            this.value = value;
            this.url = url;
            this.selected = selected;
        }

        public int getValue() {
            return value;
        }

        public String getUrl() {
            return url;
        }

        public boolean isSelected() {
            return selected;
        }
    }
}
