package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.tinycloud.mmwiki.vo.MainDefaultView;
import org.tinycloud.mmwiki.vo.SearchView;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.CollectionEntry;
import org.tinycloud.mmwiki.domain.Contact;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Link;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.mapper.CollectionMapper;
import org.tinycloud.mmwiki.mapper.ContactMapper;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.LinkMapper;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class MainService {

    private static final int COLLECTION_TYPE_DOC = 1;

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

    public List<Document> loadCollectedDocuments(CurrentUser currentUser) {
        List<CollectionEntry> collections = collectionMapper.findByUserIdAndType(currentUser.getUserId(), COLLECTION_TYPE_DOC);
        if (collections.isEmpty()) {
            return List.of();
        }
        List<String> documentIds = new ArrayList<>(collections.size());
        for (CollectionEntry collection : collections) {
            documentIds.add(collection.getResourceId());
        }
        return documentMapper.findVisibleByIds(currentUser.getUserId(), isRoot(currentUser), documentIds);
    }

    public MainDefaultView loadDefaultView() {
        List<Link> links = linkMapper.findAllOrderBySequence();
        List<Contact> contacts = contactMapper.findAll();
        String panelTitle = configService.getValue("main_title", "");
        String panelDescription = configService.getValue("main_description", "");

        return new MainDefaultView(panelTitle, panelDescription, links, contacts);
    }

    public PageModel<LogDocumentView> recentDocumentPage(CurrentUser currentUser, int pageNum, int pageSize) {
        PageInfo<LogDocumentView> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> logDocumentMapper.pageVisibleByUserId(currentUser.getUserId(), isRoot(currentUser)));
        for (LogDocumentView logDocument : pageInfo.getList()) {
            logDocument.setCreateTimeText(TimeUtils.format(logDocument.getCreateTime()));
        }
        return PageModel.from(pageInfo);
    }

    public SearchView searchDocuments(CurrentUser currentUser, String keyword, String searchType) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        String cleanSearchType = "title";
        List<Document> documents = cleanKeyword.isEmpty()
                ? List.of()
                : documentMapper.findVisibleByNameLike(currentUser.getUserId(), isRoot(currentUser), cleanKeyword);
        return new SearchView(cleanSearchType, cleanKeyword, documents, documents.size());
    }

    private boolean isRoot(CurrentUser currentUser) {
        return currentUser != null
                && currentUser.getRoleId() != null
                && currentUser.getRoleId() == AccessService.ROLE_ROOT_ID;
    }
}
