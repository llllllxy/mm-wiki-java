package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.CollectionTypeEnum;
import org.tinycloud.mmwiki.domain.*;
import org.tinycloud.mmwiki.mapper.*;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.vo.MainDefaultView;
import org.tinycloud.mmwiki.vo.SearchView;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.PageModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * 按标题搜索当前用户可见的文档。
     *
     * @param currentUser 当前登录用户
     * @param keyword     搜索关键字
     * @param searchType  搜索类型参数，当前固定按标题搜索
     * @return 搜索结果视图数据
     */
    public SearchView searchDocuments(CurrentUser currentUser, String keyword, String searchType) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        String cleanSearchType = "title";
        List<Document> documents = cleanKeyword.isEmpty()
                ? new ArrayList<>()
                : documentMapper.findVisibleByNameLike(currentUser.getUserId(), AccessService.isRoot(currentUser), cleanKeyword);
        return new SearchView(cleanSearchType, cleanKeyword, documents, documents.size());
    }
}
