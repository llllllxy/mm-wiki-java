package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.CollectionTypeEnum;
import org.tinycloud.mmwiki.domain.*;
import org.tinycloud.mmwiki.mapper.*;
import org.tinycloud.mmwiki.vo.DocumentSearchHit;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.vo.MainDefaultView;
import org.tinycloud.mmwiki.vo.SearchView;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.PageModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页聚合服务，负责首页动态、搜索结果和常用展示数据的组装。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class MainService {

    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private LogDocumentMapper logDocumentMapper;
    @Autowired
    private LinkMapper linkMapper;
    @Autowired
    private ContactMapper contactMapper;
    @Autowired
    private ConfigService configService;
    @Autowired
    private DocumentSearchIndexService documentSearchIndexService;

    /**
     * 加载当前用户收藏且仍可访问的文档列表。
     *
     * @param currentUser 当前登录用户
     * @return 用户可见的收藏文档列表
     */
    public List<Document> loadCollectedDocuments(CurrentUser currentUser) {
        List<CollectionEntry> collections = collectionMapper.findByUserIdAndType(currentUser.getUserId(), CollectionTypeEnum.DOCUMENT.getCode());
        if (collections.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> documentIds = new ArrayList<>(collections.size());
        for (CollectionEntry collection : collections) {
            documentIds.add(collection.getResourceId());
        }
        return documentMapper.findVisibleByIds(currentUser.getUserId(), AccessService.isRoot(currentUser), documentIds);
    }

    /**
     * 加载首页默认视图数据，包括链接、联系人和首页面板配置。
     *
     * @return 首页默认视图数据
     */
    public MainDefaultView loadDefaultView() {
        List<Link> links = linkMapper.findAllOrderBySequence();
        List<Contact> contacts = contactMapper.findAll();
        String panelTitle = configService.getValue("main_title", "");
        String panelDescription = configService.getValue("main_description", "");

        return new MainDefaultView(panelTitle, panelDescription, links, contacts);
    }

    /**
     * 分页加载当前用户可见的最近文档动态。
     *
     * @param currentUser 当前登录用户
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @return 最近文档动态分页数据
     */
    public PageModel<LogDocumentView> recentDocumentPage(CurrentUser currentUser, int pageNum, int pageSize) {
        Page<LogDocumentView> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> logDocumentMapper.pageVisibleByUserId(currentUser.getUserId(), AccessService.isRoot(currentUser)));
        for (LogDocumentView logDocument : pageInfo.getRecords()) {
            logDocument.setCreateTimeText(TimeUtils.format(logDocument.getCreateTime()));
        }
        return PageModel.from(pageInfo);
    }

    /**
     * 按标题或全文搜索当前用户可见的文档。
     *
     * @param currentUser 当前登录用户
     * @param keyword     搜索关键字
     * @param searchType  搜索类型参数，支持 title、content、all
     * @return 搜索结果视图数据
     */
    public SearchView searchDocuments(CurrentUser currentUser, String keyword, String searchType) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        String cleanSearchType = normalizeSearchType(searchType);
        if (cleanKeyword.isEmpty()) {
            return new SearchView(cleanSearchType, cleanKeyword, new ArrayList<>(), 0);
        }
        if ("title".equals(cleanSearchType)) {
            List<Document> documents = documentMapper.findVisibleByNameLike(currentUser.getUserId(), AccessService.isRoot(currentUser), cleanKeyword);
            return new SearchView(cleanSearchType, cleanKeyword, documents, documents.size());
        }
        List<DocumentSearchHit> hits = documentSearchIndexService.search(cleanKeyword, cleanSearchType);
        List<String> documentIds = hits.stream().map(DocumentSearchHit::getDocumentId).distinct().toList();
        if (documentIds.isEmpty()) {
            if ("all".equals(cleanSearchType)) {
                List<Document> documents = documentMapper.findVisibleByNameLike(currentUser.getUserId(), AccessService.isRoot(currentUser), cleanKeyword);
                return new SearchView(cleanSearchType, cleanKeyword, documents, documents.size());
            }
            return new SearchView(cleanSearchType, cleanKeyword, new ArrayList<>(), 0);
        }
        List<Document> visibleDocuments = documentMapper.findVisibleByIds(currentUser.getUserId(), AccessService.isRoot(currentUser), documentIds);
        Map<String, Document> visibleIndex = visibleDocuments.stream()
                .collect(Collectors.toMap(Document::getDocumentId, item -> item, (left, right) -> left, LinkedHashMap::new));
        Map<String, String> snippets = hits.stream()
                .filter(hit -> visibleIndex.containsKey(hit.getDocumentId()))
                .collect(Collectors.toMap(DocumentSearchHit::getDocumentId, DocumentSearchHit::getSnippet, (left, right) -> left, LinkedHashMap::new));
        List<Document> orderedDocuments = documentIds.stream()
                .map(visibleIndex::get)
                .filter(item -> item != null)
                .toList();
        if (orderedDocuments.isEmpty() && "all".equals(cleanSearchType)) {
            List<Document> documents = documentMapper.findVisibleByNameLike(currentUser.getUserId(), AccessService.isRoot(currentUser), cleanKeyword);
            return new SearchView(cleanSearchType, cleanKeyword, documents, documents.size());
        }
        return new SearchView(cleanSearchType, cleanKeyword, orderedDocuments, snippets, orderedDocuments.size());
    }

    /**
     * 规范化搜索类型，空值和未知类型默认使用 all，作为顶部全局搜索的全文体验。
     *
     * @param searchType 原始搜索类型
     * @return 规范化后的搜索类型
     */
    private String normalizeSearchType(String searchType) {
        String cleanSearchType = searchType == null ? "" : searchType.trim().toLowerCase();
        if ("title".equals(cleanSearchType) || "content".equals(cleanSearchType) || "all".equals(cleanSearchType)) {
            return cleanSearchType;
        }
        return "all";
    }
}
