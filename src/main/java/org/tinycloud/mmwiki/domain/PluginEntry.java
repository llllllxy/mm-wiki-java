package org.tinycloud.mmwiki.domain;

public record PluginEntry(
    Integer pluginId,
    String title,
    String description,
    String coverUrl,
    String pluginKey,
    String confValue
) {
}
