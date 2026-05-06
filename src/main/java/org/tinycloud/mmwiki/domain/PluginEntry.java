package org.tinycloud.mmwiki.domain;

/**
 * MM-Wiki 数据模型。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public record PluginEntry(
    Integer pluginId,
    String title,
    String description,
    String coverUrl,
    String pluginKey,
    String confValue
) {
}
