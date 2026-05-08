package org.tinycloud.mmwiki.domain;

import java.util.List;

/**
 * 文档详情页数据模型。
 *
 * <p>聚合 mw_space、mw_document、mw_user 等数据，无直接对应数据库表。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public record DocumentViewData(
        /**
         * 当前空间信息
         */
    Space space,
        /**
         * 当前文档信息
         */
    Document document,
        /**
         * 空间根文档信息
         */
    Document spaceDocument,
        /**
         * 子文档列表
         */
    List<Document> documents,
        /**
         * 父级文档路径列表
         */
    List<Document> parentDocuments,
        /**
         * 文档创建用户
         */
    User createUser,
        /**
         * 文档最后编辑用户
         */
    User editUser,
        /**
         * 页面 Markdown 内容
         */
    String pageContent,
        /**
         * 当前用户收藏ID
         */
    Integer collectionId,
        /**
         * 当前用户是否可编辑
         */
    boolean editor,
        /**
         * 当前用户是否可管理
         */
    boolean manager
) {
}
