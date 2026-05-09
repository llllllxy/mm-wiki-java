package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Document;

/**
 * SearchView view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class SearchView {

    /**
     * searchType.
     */
    private String searchType;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * documents.
     */
    private List<Document> documents;

    /**
     * count.
     */
    private int count;

    public SearchView() {
    }

    public SearchView(
            String searchType,
            String keyword,
            List<Document> documents,
            int count
    ) {
        this.searchType = searchType;
        this.keyword = keyword;
        this.documents = documents;
        this.count = count;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
