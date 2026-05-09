package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.web.Paginator;

/**
 * PluginPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class PluginPage {

    /**
     * plugins.
     */
    private List<PluginEntry> plugins;

    /**
     * keyword.
     */
    private String keyword;

    /**
     * paginator.
     */
    private Paginator paginator;

    public PluginPage() {
    }

    public PluginPage(
            List<PluginEntry> plugins,
            String keyword,
            Paginator paginator
    ) {
        this.plugins = plugins;
        this.keyword = keyword;
        this.paginator = paginator;
    }

    public List<PluginEntry> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginEntry> plugins) {
        this.plugins = plugins;
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
