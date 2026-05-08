package org.tinycloud.mmwiki.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.domain.CollectionEntry;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.DocumentEditData;
import org.tinycloud.mmwiki.domain.DocumentHistoryView;
import org.tinycloud.mmwiki.domain.DocumentTreeNode;
import org.tinycloud.mmwiki.domain.DocumentViewData;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class DocumentService {

    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Pattern INVALID_NAME = Pattern.compile("[\\\\/:*?\"<>|]");

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private AccessService accessService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private DocumentFileService documentFileService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private LogDocumentMapper logDocumentMapper;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private FollowService followService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThreadPoolTaskExecutor asyncServiceExecutor;

    /**
     * 按文档 ID 查询未删除的文档。
     */
    public Document findActiveById(String documentId) {
        return documentMapper.findActiveById(documentId);
    }

    /**
     * 查询空间默认文档。
     */
    public Document findSpaceDefaultDocument(Integer spaceId) {
        return requireSpaceDefaultDocument(spaceId);
    }

    /**
     * 加载文档浏览页所需的正文、权限、附件与导航数据。
     */
    public DocumentViewData loadDocumentView(String documentId, CurrentUser currentUser) throws IOException {
        Document document = requireDocument(documentId);
        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.visit()) {
            throw new IllegalStateException("您没有权限访问该空间文档。");
        }

        Document spaceDocument = requireSpaceDefaultDocument(space.getSpaceId());
        List<Document> documents = documentMapper.findAllSpaceDocuments(space.getSpaceId());
        List<Document> parentDocuments = getParentDocuments(document);
        String pageFile = documentFileService.resolvePageFile(document, parentDocuments);
        String pageContent = documentFileService.readPage(pageFile);

        Map<Integer, User> users = loadUsers(document.getCreateUserId(), document.getEditUserId());
        CollectionEntry collection = collectionService.findByUserTypeAndResourceId(currentUser.getUserId(), CollectionService.TYPE_DOC, documentId);

        return new DocumentViewData(
            space,
            document,
            spaceDocument,
            documents,
            parentDocuments,
            users.get(document.getCreateUserId()),
            users.get(document.getEditUserId()),
            pageContent,
            collection == null ? 0 : collection.getCollectionId(),
            access.editor(),
            access.manager()
        );
    }

    /**
     * 加载文档分享页所需的数据。
     */
    public SharedPageView loadSharedView(String documentId) throws IOException {
        Document document = requireDocument(documentId);
        Space space = spaceService.requireSpace(document.getSpaceId());
        if (!Objects.equals(space.getIsShare(), 1)) {
            throw new IllegalStateException("该文档不允许分享。");
        }
        List<Document> parentDocuments = getParentDocuments(document);
        String pageFile = documentFileService.resolvePageFile(document, parentDocuments);
        String pageContent = documentFileService.readPage(pageFile);
        Map<Integer, User> users = loadUsers(document.getCreateUserId(), document.getEditUserId());
        return new SharedPageView(
            document,
            parentDocuments,
            pageContent,
            users.get(document.getCreateUserId()),
            users.get(document.getEditUserId())
        );
    }

    /**
     * 导出 Markdown 文档内容。
     */
    public ExportPayload exportDocument(String documentId, CurrentUser currentUser) throws IOException {
        Document document = requireDocument(documentId);
        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.visit()) {
            throw new IllegalStateException("您没有权限导出该空间文档。");
        }
        if (!Objects.equals(space.getIsExport(), 1)) {
            throw new IllegalStateException("该文档不允许导出。");
        }

        List<Document> parentDocuments = getParentDocuments(document);
        String pageFile = documentFileService.resolvePageFile(document, parentDocuments);
        List<Attachment> attachments = attachmentService.findByDocumentId(documentId);
        return new ExportPayload(document.getName() + ".zip", new ByteArrayResource(zipDocument(document, pageFile, attachments)));
    }

    /**
     * 将文档列表转换为前端树组件使用的 JSON。
     */
    public String toTreeJson(List<Document> documents) {
        List<DocumentTreeNode> treeNodes = documents.stream().map(document -> {
            DocumentTreeNode node = new DocumentTreeNode();
            node.setId(document.getDocumentId());
            node.setPId(document.getParentId());
            node.setName(document.getName());
            node.setSpaceId(document.getSpaceId());
            node.setOpen(false);
            node.setParent(document.getType() != null && document.getType() == DocumentFileService.DOCUMENT_TYPE_DIR);
            return node;
        }).toList();
        try {
            return objectMapper.writeValueAsString(treeNodes);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize document tree", ex);
        }
    }

    /**
     * 计算当前文档的父级路径链路。
     */
    public List<Document> getParentDocuments(Document document) {
        if ("0".equals(document.getParentId())) {
            return List.of(document);
        }
        if (!StringUtils.hasText(document.getPath())) {
            return List.of();
        }
        List<String> ids = List.of(document.getPath().split(","));
        List<String> filteredIds = ids.stream().filter(id -> !"0".equals(id) && StringUtils.hasText(id)).toList();
        if (filteredIds.isEmpty()) {
            return List.of();
        }
        List<Document> documents = documentMapper.findActiveByIds(filteredIds);
        Map<String, Document> index = documents.stream().collect(Collectors.toMap(Document::getDocumentId, item -> item));
        List<Document> ordered = new ArrayList<>();
        for (String id : filteredIds) {
            Document current = index.get(id);
            if (current != null) {
                ordered.add(current);
            }
        }
        return ordered;
    }

    /**
     * 加载文档编辑页所需的数据。
     */
    public DocumentEditData loadEditData(String documentId, CurrentUser currentUser) throws IOException {
        DocumentViewData view = loadDocumentView(documentId, currentUser);
        if (!view.editor()) {
            throw new IllegalStateException("您没有权限修改该空间文档。");
        }
        return new DocumentEditData(
            view.document(),
            view.pageContent(),
            configService.getValue("send_email_open", "0"),
            configService.getValue("auto_follow_doc_open", "0")
        );
    }

    @Transactional
    /**
     * 创建页面或目录文档，并同步初始化磁盘文件。
     */
    public JsonResponse<Void> createDocument(CurrentUser currentUser, Integer spaceId, String parentId, Integer type, String name) throws IOException {
        String cleanName = name == null ? "" : name.trim();
        if (spaceId == null || !StringUtils.hasText(parentId) || cleanName.isBlank()) {
            return JsonResponse.error("参数错误。");
        }
        if (!isValidName(cleanName)) {
            return JsonResponse.error("文档名称格式不正确。");
        }
        if (!Objects.equals(type, DocumentFileService.DOCUMENT_TYPE_PAGE) && !Objects.equals(type, DocumentFileService.DOCUMENT_TYPE_DIR)) {
            return JsonResponse.error("文档类型错误。");
        }

        Space space = spaceService.requireSpace(spaceId);
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.editor()) {
            return JsonResponse.error("您没有权限在该空间创建文档。");
        }

        Document parentDocument = requireDocument(parentId);
        if (parentDocument.getType() == null || parentDocument.getType() != DocumentFileService.DOCUMENT_TYPE_DIR) {
            return JsonResponse.error("父文档不是目录。");
        }
        if (documentMapper.findByNameParentIdAndSpaceId(cleanName, parentId, spaceId, type) != null) {
            return JsonResponse.error("该文档名称已经存在。");
        }

        int now = Math.toIntExact(Instant.now().getEpochSecond());
        int nextSequence = Objects.requireNonNullElse(documentMapper.findMaxSequence(parentId, spaceId), 0) + 1;
        Document document = new Document();
        document.setDocumentId(UUID.randomUUID().toString().replace("-", ""));
        document.setParentId(parentId);
        document.setSpaceId(spaceId);
        document.setName(cleanName);
        document.setType(type);
        document.setPath(parentDocument.getPath() + "," + parentId);
        document.setSequence(nextSequence);
        document.setCreateUserId(currentUser.getUserId());
        document.setEditUserId(currentUser.getUserId());
        document.setCreateTime(now);
        document.setUpdateTime(now);
        documentMapper.insert(document);

        List<Document> parents = getParentDocuments(document);
        String pageFile = documentFileService.resolvePageFile(document, parents);
        documentFileService.createEmptyPage(pageFile);
        logDocumentMapper.insert(document.getDocumentId(), spaceId, currentUser.getUserId(), 1, "创建了文档", now);
        followService.autoFollowDocument(currentUser.getUserId(), document.getDocumentId());

        return JsonResponse.success("创建文档成功", "/document/index?document_id=" + document.getDocumentId());
    }

    @Transactional
    /**
     * 保存文档正文与基础属性，并记录编辑历史。
     */
    public JsonResponse<Void> modifyPage(
        CurrentUser currentUser,
        String documentId,
        String newName,
        String content,
        String comment,
        boolean followDocument,
        boolean noticeUsers,
        String documentUrl
    ) throws IOException {
        Document document = requireDocument(documentId);
        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.editor()) {
            return JsonResponse.error("您没有权限修改该空间文档。");
        }

        String targetName = StringUtils.hasText(newName) ? newName.trim() : document.getName();
        if ("0".equals(document.getParentId())) {
            targetName = document.getName();
        } else {
            if (!isValidName(targetName)) {
                return JsonResponse.error("文档名称格式不正确。");
            }
            if (!targetName.equals(document.getName())) {
                Document duplicate = documentMapper.findByNameParentIdAndSpaceId(targetName, document.getParentId(), document.getSpaceId(), document.getType());
                if (duplicate != null) {
                    return JsonResponse.error("该文档名称已经存在。");
                }
            }
        }

        List<Document> parents = getParentDocuments(document);
        String oldPageFile = documentFileService.resolvePageFile(document, parents);
        documentFileService.writePage(oldPageFile, content);
        boolean nameChanged = !targetName.equals(document.getName());
        documentFileService.renamePageOrDirectory(oldPageFile, targetName, document.getType(), nameChanged);

        int now = Math.toIntExact(Instant.now().getEpochSecond());
        document.setName(targetName);
        document.setEditUserId(currentUser.getUserId());
        document.setUpdateTime(now);
        documentMapper.updateNameAndEditor(document);
        logDocumentMapper.insert(documentId, document.getSpaceId(), currentUser.getUserId(), 2, comment == null ? "" : comment, now);
        if (followDocument) {
            followService.followDocument(currentUser.getUserId(), documentId);
        }
        if (noticeUsers) {
            asyncServiceExecutor.execute(() -> emailService.sendDocumentUpdateNotice(document, currentUser.getUsername(), content, comment, documentUrl));
        }

        return JsonResponse.success("文档修改成功", "/document/index?document_id=" + documentId);
    }

    @Transactional
    /**
     * 移动文档到新的目录位置并维护层级路径。
     */
    public JsonResponse<Void> moveDocument(CurrentUser currentUser, String documentId, String targetId, String moveType) throws IOException {
        if (!StringUtils.hasText(documentId) || !StringUtils.hasText(targetId)) {
            return JsonResponse.error("缺少文档参数。");
        }
        Document document = requireDocument(documentId);
        Document targetDocument = requireDocument(targetId);
        if (!Objects.equals(document.getSpaceId(), targetDocument.getSpaceId())) {
            return JsonResponse.error("文档和目标文档不在同一空间。");
        }

        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.editor()) {
            return JsonResponse.error("您没有权限移动该空间文档。");
        }

        int now = Math.toIntExact(Instant.now().getEpochSecond());
        if ("next".equals(moveType) || "prev".equals(moveType)) {
            int updateSequence = Objects.requireNonNullElse(targetDocument.getSequence(), 0);
            if ("next".equals(moveType)) {
                updateSequence += 1;
            }
            documentMapper.bumpSequenceBySpaceIdFrom(document.getSpaceId(), updateSequence, 1, now);
            document.setSequence(updateSequence);
            document.setEditUserId(currentUser.getUserId());
            document.setUpdateTime(now);
            documentMapper.updateSequence(document);
            return JsonResponse.success("移动文档成功", "/document/index?document_id=" + documentId);
        }

        if (Objects.equals(document.getType(), DocumentFileService.DOCUMENT_TYPE_DIR)) {
            return JsonResponse.error("目录不能移动到其他目录中。");
        }
        if (!Objects.equals(targetDocument.getType(), DocumentFileService.DOCUMENT_TYPE_DIR)) {
            return JsonResponse.error("目标文档必须是目录。");
        }

        List<Document> oldParents = getParentDocuments(document);
        String oldPageFile = documentFileService.resolvePageFile(document, oldParents);

        Document movedDocument = new Document();
        movedDocument.setDocumentId(document.getDocumentId());
        movedDocument.setParentId(targetId);
        movedDocument.setSpaceId(document.getSpaceId());
        movedDocument.setName(document.getName());
        movedDocument.setType(document.getType());
        movedDocument.setPath(targetDocument.getPath() + "," + targetId);
        movedDocument.setEditUserId(currentUser.getUserId());
        movedDocument.setUpdateTime(now);
        List<Document> newParents = getParentDocuments(movedDocument);
        String newPageFile = documentFileService.resolvePageFile(movedDocument, newParents);

        documentMapper.updateParentPathEditor(movedDocument);
        documentFileService.movePageOrDirectory(oldPageFile, newPageFile, document.getType());
        logDocumentMapper.insert(documentId, document.getSpaceId(), currentUser.getUserId(), 2, "移动文档到 " + targetDocument.getName(), now);
        return JsonResponse.success("移动文档成功", "/document/index?document_id=" + documentId);
    }

    @Transactional
    /**
     * 软删除文档并清理对应的文档资源。
     */
    public JsonResponse<Void> deleteDocument(CurrentUser currentUser, String documentId) throws IOException {
        if (!StringUtils.hasText(documentId)) {
            return JsonResponse.error("没有选择文档。");
        }
        Document document = requireDocument(documentId);
        if (Objects.equals(document.getType(), DocumentFileService.DOCUMENT_TYPE_DIR) && !documentMapper.findByParentId(documentId).isEmpty()) {
            return JsonResponse.error("请先删除或移动目录下所有文档。");
        }

        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.manager()) {
            return JsonResponse.error("您没有权限删除该空间文档。");
        }

        List<Document> parents = getParentDocuments(document);
        String pageFile = documentFileService.resolvePageFile(document, parents);
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        document.setEditUserId(currentUser.getUserId());
        document.setUpdateTime(now);
        documentMapper.markDeleted(document);
        documentFileService.deletePageOrDirectory(pageFile, document.getType());
        attachmentService.deleteByDocumentId(documentId);
        followService.deleteDocumentFollowers(documentId);
        logDocumentMapper.insert(documentId, document.getSpaceId(), currentUser.getUserId(), 2, "删除文档", now);
        return JsonResponse.success("删除文档成功", "/document/index?document_id=" + document.getParentId());
    }

    /**
     * 分页加载文档历史记录。
     */
    public HistoryPage loadHistory(String documentId, CurrentUser currentUser, int page, int number) {
        Document document = requireDocument(documentId);
        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.visit()) {
            throw new IllegalStateException("您没有权限查看该空间修改历史。");
        }

        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, number);
        int offset = (safePage - 1) * safeNumber;
        List<DocumentHistoryView> history = logDocumentMapper.findByDocumentId(documentId, offset, safeNumber);
        history.forEach(item -> item.setCreateTimeText(formatTime(item.getCreateTime())));
        long count = logDocumentMapper.countByDocumentId(documentId);
        return new HistoryPage(history, Paginator.of(safePage, safeNumber, count, "/document/history?document_id=" + documentId));
    }

    public record HistoryPage(List<DocumentHistoryView> items, Paginator paginator) {
    }

    public record SharedPageView(
        Document document,
        List<Document> parentDocuments,
        String pageContent,
        User createUser,
        User editUser
    ) {
    }

    public record ExportPayload(String fileName, ByteArrayResource resource) {
    }

    private boolean isValidName(String name) {
        return StringUtils.hasText(name)
            && !DocumentFileService.DEFAULT_FILE_NAME.equalsIgnoreCase(name)
            && !INVALID_NAME.matcher(name).find();
    }

    private Map<Integer, User> loadUsers(Integer... userIds) {
        List<Integer> ids = new ArrayList<>();
        for (Integer userId : userIds) {
            if (userId != null && !ids.contains(userId)) {
                ids.add(userId);
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userService.findActiveByIds(ids).stream()
            .collect(Collectors.toMap(User::getUserId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Document requireDocument(String documentId) {
        Document document = documentMapper.findActiveById(documentId);
        if (document == null) {
            throw new IllegalStateException("文档不存在。");
        }
        return document;
    }

    private Document requireSpaceDefaultDocument(Integer spaceId) {
        Document document = documentMapper.findSpaceDefaultDocument(spaceId);
        if (document == null) {
            throw new IllegalStateException("空间首页文档不存在。");
        }
        return document;
    }

    private byte[] zipDocument(Document document, String pageFile, List<Attachment> attachments) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(output)) {
            Path pagePath = documentFileService.resolvePagePath(pageFile);
            if (Files.exists(pagePath)) {
                zip.putNextEntry(new ZipEntry(document.getName() + ".md"));
                Files.copy(pagePath, zip);
                zip.closeEntry();
            }
            for (Attachment attachment : attachments) {
                if (!StringUtils.hasText(attachment.getPath())) {
                    continue;
                }
                Path attachmentPath = documentFileService.resolveAttachmentPath(attachment.getPath());
                if (!Files.exists(attachmentPath) || Files.isDirectory(attachmentPath)) {
                    continue;
                }
                zip.putNextEntry(new ZipEntry(attachment.getPath().replace('\\', '/')));
                Files.copy(attachmentPath, zip);
                zip.closeEntry();
            }
            zip.finish();
            return output.toByteArray();
        }
    }

    private String formatTime(Integer epoch) {
        if (epoch == null || epoch <= 0) {
            return "";
        }
        return DATE_ONLY.format(Instant.ofEpochSecond(epoch));
    }
}
