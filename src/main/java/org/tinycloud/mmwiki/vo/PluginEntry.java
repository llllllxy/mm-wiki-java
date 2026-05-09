package org.tinycloud.mmwiki.vo;

/**
 * PluginEntry view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class PluginEntry {

    /**
     * pluginId.
     */
    private Integer pluginId;

    /**
     * title.
     */
    private String title;

    /**
     * description.
     */
    private String description;

    /**
     * coverUrl.
     */
    private String coverUrl;

    /**
     * pluginKey.
     */
    private String pluginKey;

    /**
     * confValue.
     */
    private String confValue;

    public PluginEntry() {
    }

    public PluginEntry(
            Integer pluginId,
            String title,
            String description,
            String coverUrl,
            String pluginKey,
            String confValue
    ) {
        this.pluginId = pluginId;
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.pluginKey = pluginKey;
        this.confValue = confValue;
    }

    public Integer getPluginId() {
        return pluginId;
    }

    public void setPluginId(Integer pluginId) {
        this.pluginId = pluginId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getPluginKey() {
        return pluginKey;
    }

    public void setPluginKey(String pluginKey) {
        this.pluginKey = pluginKey;
    }

    public String getConfValue() {
        return confValue;
    }

    public void setConfValue(String confValue) {
        this.confValue = confValue;
    }

}
