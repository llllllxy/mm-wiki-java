package org.tinycloud.mmwiki.domain;

/**
 * 文档编辑页数据模型。
 *
 * <p>页面聚合数据，无直接对应数据库表。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public record DocumentEditData(
        /**
         * 文档信息
         */
    Document document,
        /**
         * 页面 Markdown 内容
         */
    String pageContent,
        /**
         * 是否发送邮件通知
         */
    String sendEmail,
        /**
         * 是否自动关注文档
         */
    String autoFollowDoc
) {
}
