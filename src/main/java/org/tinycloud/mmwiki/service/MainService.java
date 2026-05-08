package org.tinycloud.mmwiki.service;

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
import org.tinycloud.mmwiki.web.Paginator;

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

    public List<Document> loadCollectedDocuments(Integer userId) {
        List<CollectionEntry> collections = collectionMapper.findByUserIdAndType(userId, COLLECTION_TYPE_DOC);
        if (collections.isEmpty()) {
            return List.of();
        }
        List<String> documentIds = new ArrayList<>(collections.size());
        for (CollectionEntry collection : collections) {
            documentIds.add(collection.getResourceId());
        }
        return documentMapper.findActiveByIds(documentIds);
    }

    public MainDefaultView loadDefaultView(Integer userId, int page, int number) {
        int safePage = Math.max(page, 1);
        int safeNumber = Math.max(number, 1);
        int offset = (safePage - 1) * safeNumber;

        long total = logDocumentMapper.countVisibleByUserId(userId);
        List<LogDocumentView> logDocuments = logDocumentMapper.findVisibleByUserId(userId, offset, safeNumber);
        for (LogDocumentView logDocument : logDocuments) {
            logDocument.setCreateTimeText(TimeUtils.formatUnix(logDocument.getCreateTime()));
        }

        List<Link> links = linkMapper.findAllOrderBySequence();
        List<Contact> contacts = contactMapper.findAll();
        String panelTitle = configService.getValue("main_title", "");
        String panelDescription = configService.getValue("main_description", "");
        Paginator paginator = Paginator.of(safePage, safeNumber, total, "/main/default");

        return new MainDefaultView(panelTitle, panelDescription, logDocuments, links, contacts, paginator);
    }

    public SearchView searchDocuments(Integer userId, String keyword, String searchType) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        String cleanSearchType = "title";
        List<Document> documents = cleanKeyword.isEmpty() ? List.of() : documentMapper.findVisibleByNameLike(userId, cleanKeyword);
        return new SearchView(cleanSearchType, cleanKeyword, documents, documents.size());
    }

    public record MainDefaultView(
        String panelTitle,
        String panelDescription,
        List<LogDocumentView> logDocuments,
        List<Link> links,
        List<Contact> contacts,
        Paginator paginator
    ) {
    }

    public record SearchView(String searchType, String keyword, List<Document> documents, int count) {
    }
}
