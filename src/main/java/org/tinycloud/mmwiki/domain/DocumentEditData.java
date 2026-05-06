package org.tinycloud.mmwiki.domain;

/**
 * MM-Wiki 数据模型。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public record DocumentEditData(
    Document document,
    String pageContent,
    String sendEmail,
    String autoFollowDoc
) {
}
