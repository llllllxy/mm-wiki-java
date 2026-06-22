package org.tinycloud.mmwiki.vo;

import java.util.List;
import java.util.Map;

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
     * snippets.
     */
    private Map<String, String> snippets;

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
        this(searchType, keyword, documents, Map.of(), count);
    }

    /**
     * 创建带全文搜索摘要的搜索视图对象。
     *
     * @param searchType 搜索类型
     * @param keyword    搜索关键字
     * @param documents  搜索结果文档
     * @param snippets   以文档ID为键的搜索摘要
     * @param count      结果数量
     */
    public SearchView(
            String searchType,
            String keyword,
            List<Document> documents,
            Map<String, String> snippets,
            int count
    ) {
        this.searchType = searchType;
        this.keyword = keyword;
        this.documents = documents;
        this.snippets = snippets;
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

    /**
     * 获取以文档ID为键的搜索摘要。
     */
    public Map<String, String> getSnippets() {
        return snippets;
    }

    /**
     * 设置以文档ID为键的搜索摘要。
     */
    public void setSnippets(Map<String, String> snippets) {
        this.snippets = snippets;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
