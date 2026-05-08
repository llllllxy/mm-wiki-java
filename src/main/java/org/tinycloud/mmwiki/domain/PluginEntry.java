package org.tinycloud.mmwiki.domain;

/**
 * 插件配置展示模型。
 *
 * <p>基于 mw_config 中的插件配置组装，无独立数据库表。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public record PluginEntry(
        /**
         * 插件ID
         */
    Integer pluginId,
        /**
         * 插件标题
         */
    String title,
        /**
         * 插件描述
         */
    String description,
        /**
         * 插件封面地址
         */
    String coverUrl,
        /**
         * 插件配置键
         */
    String pluginKey,
        /**
         * 插件配置值
         */
    String confValue
) {
}
