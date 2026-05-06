package org.tinycloud.mmwiki.domain;

import java.util.List;

/**
 * MM-Wiki 数据模型。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public record DocumentViewData(
    Space space,
    Document document,
    Document spaceDocument,
    List<Document> documents,
    List<Document> parentDocuments,
    User createUser,
    User editUser,
    String pageContent,
    Integer collectionId,
    boolean editor,
    boolean manager
) {
}
