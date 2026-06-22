package org.tinycloud.mmwiki.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.vo.DocumentSearchHit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档全文搜索索引服务，使用 Lucene 在本地磁盘维护 Markdown 文档索引。
 *
 * @author liuxingyu01
 * @since 2026-06-22
 */
@Service
public class DocumentSearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSearchIndexService.class);
    private static final int MAX_SEARCH_RESULT = 200;

    private final Analyzer analyzer = new SmartChineseAnalyzer();

    @Autowired
    private MmwikiProperties properties;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentFileService documentFileService;

    @Autowired
    private ConfigService configService;

    /**
     * 按关键字搜索本地 Lucene 索引，索引不存在时会先尝试全量重建。
     *
     * @param keyword    搜索关键字
     * @param searchType 搜索类型，支持 content 和 all
     * @return Lucene 命中的文档ID、得分和摘要
     */
    public List<DocumentSearchHit> search(String keyword, String searchType) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        if (cleanKeyword.isEmpty() || !isFullTextSearchOpen()) {
            return Collections.emptyList();
        }
        try {
            ensureIndexReady();
            Path indexDir = searchIndexDir();
            try (FSDirectory directory = FSDirectory.open(indexDir); DirectoryReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = buildQuery(cleanKeyword, searchType);
                TopDocs topDocs = searcher.search(query, MAX_SEARCH_RESULT);
                List<DocumentSearchHit> hits = new ArrayList<>();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    org.apache.lucene.document.Document luceneDocument = searcher.storedFields().document(scoreDoc.doc);
                    String documentId = luceneDocument.get("documentId");
                    String content = luceneDocument.get("content");
                    hits.add(new DocumentSearchHit(documentId, scoreDoc.score, snippet(content, cleanKeyword)));
                }
                return hits;
            }
        } catch (Exception ex) {
            log.warn("全文搜索失败，keyword={}", cleanKeyword, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 全量重建所有有效文档的本地索引，单个文档读取失败时会跳过并继续处理后续文档。
     */
    public synchronized void rebuildAll() {
        if (!isFullTextSearchOpen()) {
            return;
        }
        List<Document> documents = this.documentMapper.findAllActive();
        try {
            Files.createDirectories(searchIndexDir());
            try (IndexWriter writer = new IndexWriter(FSDirectory.open(searchIndexDir()), writerConfig())) {
                writer.deleteAll();
                for (Document document : documents) {
                    addDocument(writer, document);
                }
                writer.commit();
            }
            log.info("全文搜索索引重建完成，文档数={}", documents.size());
        } catch (Exception ex) {
            log.warn("全文搜索索引重建失败", ex);
        }
    }

    /**
     * 更新指定文档的本地索引，文档不存在时会删除对应旧索引。
     *
     * @param documentId 文档ID
     */
    public synchronized void updateDocument(String documentId) {
        if (!StringUtils.hasText(documentId) || !isFullTextSearchOpen()) {
            return;
        }
        Document document = this.documentMapper.findActiveById(documentId);
        if (document == null) {
            this.deleteDocument(documentId);
            return;
        }
        try {
            Files.createDirectories(searchIndexDir());
            try (IndexWriter writer = new IndexWriter(FSDirectory.open(searchIndexDir()), writerConfig())) {
                writer.deleteDocuments(new Term("documentId", documentId));
                addDocument(writer, document);
                writer.commit();
            }
        } catch (Exception ex) {
            log.warn("更新文档全文索引失败，documentId={}", documentId, ex);
        }
    }

    /**
     * 在当前事务提交后更新指定文档索引；没有事务时立即更新，用于业务服务安全触发索引同步。
     *
     * @param documentId 文档ID
     */
    public void updateDocumentAfterCommit(String documentId) {
        runAfterCommit(() -> updateDocument(documentId));
    }

    /**
     * 删除指定文档的本地索引。
     *
     * @param documentId 文档ID
     */
    public synchronized void deleteDocument(String documentId) {
        if (!StringUtils.hasText(documentId)) {
            return;
        }
        try {
            Files.createDirectories(this.searchIndexDir());
            try (IndexWriter writer = new IndexWriter(FSDirectory.open(this.searchIndexDir()), writerConfig())) {
                writer.deleteDocuments(new Term("documentId", documentId));
                writer.commit();
            }
        } catch (Exception ex) {
            log.warn("删除文档全文索引失败，documentId={}", documentId, ex);
        }
    }

    /**
     * 在当前事务提交后删除指定文档索引；没有事务时立即删除，用于业务服务安全触发索引同步。
     *
     * @param documentId 文档ID
     */
    public void deleteDocumentAfterCommit(String documentId) {
        runAfterCommit(() -> deleteDocument(documentId));
    }

    /**
     * 确保索引目录已经存在且包含 Lucene 索引，不存在时执行一次全量重建。
     *
     * @throws IOException 检查索引目录失败时抛出
     */
    private void ensureIndexReady() throws IOException {
        Path indexDir = this.searchIndexDir();
        if (!Files.exists(indexDir)) {
            this.rebuildAll();
            return;
        }
        try (FSDirectory directory = FSDirectory.open(indexDir)) {
            if (!DirectoryReader.indexExists(directory)) {
                this.rebuildAll();
            }
        }
    }

    /**
     * 构造 Lucene 查询对象，content 只查正文，all 同时查标题和正文。
     *
     * @param keyword    搜索关键字
     * @param searchType 搜索类型
     * @return Lucene 查询对象
     * @throws Exception 查询语法解析失败时抛出
     */
    private Query buildQuery(String keyword, String searchType) throws Exception {
        String[] fields = "content".equals(searchType) ? new String[]{"content"} : new String[]{"name", "content"};
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        return parser.parse(QueryParser.escape(keyword));
    }

    /**
     * 将单个业务文档写入 Lucene 索引，目录和页面都会索引名称，正文从 Markdown 文件读取。
     *
     * @param writer   Lucene 写入器
     * @param document 业务文档
     */
    private void addDocument(IndexWriter writer, Document document) {
        try {
            String pageFile = documentFileService.resolvePageFile(document, parentDocuments(document));
            String content = documentFileService.readPage(pageFile);
            org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
            luceneDocument.add(new StringField("documentId", document.getDocumentId(), Field.Store.YES));
            luceneDocument.add(new StringField("spaceId", String.valueOf(document.getSpaceId()), Field.Store.YES));
            luceneDocument.add(new TextField("name", document.getName() == null ? "" : document.getName(), Field.Store.YES));
            luceneDocument.add(new TextField("content", content == null ? "" : content, Field.Store.YES));
            writer.addDocument(luceneDocument);
        } catch (Exception ex) {
            log.warn("写入单个文档全文索引失败，documentId={}", document.getDocumentId(), ex);
        }
    }

    /**
     * 解析文档的父级链路，返回顺序与 Markdown 文件路径一致。
     *
     * @param document 当前文档
     * @return 父级文档列表
     */
    private List<Document> parentDocuments(Document document) {
        if ("0".equals(document.getParentId())) {
            return List.of(document);
        }
        if (!StringUtils.hasText(document.getPath())) {
            return Collections.emptyList();
        }
        List<String> ids = List.of(document.getPath().split(",")).stream()
                .filter(id -> !"0".equals(id) && StringUtils.hasText(id))
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Document> documents = documentMapper.findActiveByIds(ids);
        Map<String, Document> index = documents.stream().collect(Collectors.toMap(Document::getDocumentId, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<Document> ordered = new ArrayList<>();
        for (String id : ids) {
            Document parent = index.get(id);
            if (parent != null) {
                ordered.add(parent);
            }
        }
        return ordered;
    }

    /**
     * 根据正文和关键字生成搜索结果摘要。
     *
     * @param content 正文内容
     * @param keyword 搜索关键字
     * @return 搜索摘要
     */
    private String snippet(String content, String keyword) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String lowerContent = content.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int index = lowerContent.indexOf(lowerKeyword);
        if (index < 0) {
            return content.length() > 120 ? content.substring(0, 120) + "..." : content;
        }
        int start = Math.max(0, index - 45);
        int end = Math.min(content.length(), index + keyword.length() + 75);
        return (start > 0 ? "..." : "") + content.substring(start, end) + (end < content.length() ? "..." : "");
    }

    /**
     * 创建 Lucene 写入器配置。
     *
     * @return Lucene 写入器配置
     */
    private IndexWriterConfig writerConfig() {
        return new IndexWriterConfig(this.analyzer);
    }

    /**
     * 返回全文搜索索引目录，目录位于文档根目录 search-index 下。
     *
     * @return 本地索引目录
     */
    private Path searchIndexDir() {
        return Path.of(this.properties.getDocumentRootDir()).toAbsolutePath().normalize().resolve("search-index");
    }

    /**
     * 判断全文搜索开关是否开启，配置表不可用时安全返回 false。
     *
     * @return true 表示开启全文搜索
     */
    public boolean isFullTextSearchOpen() {
        try {
            return "1".equals(this.configService.getValue("fulltext_search_open", "0"));
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 注册事务提交后的索引动作，避免业务事务回滚后提前更新 Lucene 索引。
     *
     * @param action 需要在事务提交后执行的索引动作
     */
    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
