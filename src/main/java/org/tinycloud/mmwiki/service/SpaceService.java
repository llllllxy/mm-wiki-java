package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.domain.CollectionEntry;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.SpaceUser;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.SpaceMapper;
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
public class SpaceService {

    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final Pattern INVALID_SPACE_NAME = Pattern.compile("[\\\\/:*?\"<>|]");
    private static final int SPACE_MANAGER_PRIVILEGE = 2;

    @Autowired
    private SpaceMapper spaceMapper;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private SpaceUserService spaceUserService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccessService accessService;
    @Autowired
    private DocumentFileService documentFileService;
    @Autowired
    private AttachmentService attachmentService;

    /**
     * 查询空间并在不存在时中断当前业务流程。
     */
    public Space requireSpace(Integer spaceId) {
        Space space = spaceMapper.findActiveById(spaceId);
        if (space == null) {
            throw new IllegalStateException("空间不存在！");
        }
        decorate(List.of(space));
        return space;
    }

    /**
     * 统计当前可用的空间标签。
     */
    public Set<String> listTags() {
        Set<String> tags = new LinkedHashSet<>();
        for (Space space : spaceMapper.findAllActive()) {
            if (space.getTags() == null || space.getTags().isBlank()) {
                continue;
            }
            for (String tag : space.getTags().split(",")) {
                String clean = tag.trim();
                if (!clean.isEmpty()) {
                    tags.add(clean);
                }
            }
        }
        return tags;
    }

    /**
     * 分页加载当前用户可访问的空间列表。
     */
    public SpacePage listSpaces(CurrentUser currentUser, String keyword, int page, int number) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        long count;
        List<Space> spaces;
        if (keyword != null && !keyword.isBlank()) {
            count = spaceMapper.countByKeyword(keyword.trim());
            spaces = spaceMapper.findByKeywordPaged(keyword.trim(), offset, safeNumber);
        } else {
            count = spaceMapper.countAll();
            spaces = spaceMapper.findAllPaged(offset, safeNumber);
        }
        markCollections(currentUser, spaces);
        decorate(spaces);
        return new SpacePage(spaces, count, keyword == null ? "" : keyword, Paginator.of(safePage, safeNumber, count, "/space/list"));
    }

    /**
     * 加载当前用户收藏的空间列表。
     */
    public SpaceCollectionPage listCollectedSpaces(CurrentUser currentUser) {
        List<CollectionEntry> collections = collectionService.findByUserIdAndType(currentUser.getUserId(), CollectionService.TYPE_SPACE);
        if (collections.isEmpty()) {
            return new SpaceCollectionPage(List.of(), 0);
        }
        List<Integer> ids = collections.stream().map(entry -> Integer.valueOf(entry.getResourceId())).toList();
        List<Space> spaces = spaceMapper.findActiveByIds(ids);
        Map<Integer, Integer> collectionBySpace = collections.stream()
            .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getResourceId()), CollectionEntry::getCollectionId));
        for (Space space : spaces) {
            space.setCollection(true);
            space.setCollectionId(collectionBySpace.get(space.getSpaceId()));
        }
        decorate(spaces);
        return new SpaceCollectionPage(spaces, spaces.size());
    }

    /**
     * 按标签筛选当前用户可访问的空间。
     */
    public SpaceCollectionPage searchByTag(CurrentUser currentUser, String tag) {
        List<Space> spaces = spaceMapper.findByTag(tag == null ? "" : tag.trim());
        markCollections(currentUser, spaces);
        decorate(spaces);
        return new SpaceCollectionPage(spaces, spaces.size());
    }

    /**
     * 分页加载空间成员与可添加用户。
     */
    public MemberPage listMembers(Integer spaceId, int page, int number) {
        return listMembers(null, spaceId, page, number);
    }

    /**
     * 分页加载空间成员与可添加用户。
     */
    public MemberPage listMembers(CurrentUser currentUser, Integer spaceId, int page, int number) {
        Space space = requireSpace(spaceId);
        AccessService.Access access = currentUser == null ? new AccessService.Access(true, false, false) : null;
        if (currentUser != null) {
            access = accessService.access(currentUser, space);
            if (!access.visit()) {
                throw new IllegalStateException("您没有权限访问该空间成员列表。");
            }
        }
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        long count = spaceUserService.countBySpaceId(spaceId);
        List<SpaceUser> members = spaceUserService.findBySpaceIdPaged(spaceId, offset, safeNumber);
        List<Integer> userIds = members.stream().map(SpaceUser::getUserId).toList();
        Map<Integer, User> users = userIds.isEmpty() ? Map.of() : userService.findActiveByIds(userIds).stream()
            .collect(Collectors.toMap(User::getUserId, item -> item));
        List<MemberView> views = new ArrayList<>();
        for (SpaceUser member : members) {
            User user = users.get(member.getUserId());
            if (user == null) {
                continue;
            }
            views.add(new MemberView(user, member.getPrivilege(), member.getSpaceUserId()));
        }
        boolean manager = access != null && access.manager();
        List<User> otherUsers = Collections.emptyList();
        if (manager) {
            List<Integer> allMemberIds = spaceUserService.findBySpaceId(spaceId).stream().map(SpaceUser::getUserId).toList();
            otherUsers = allMemberIds.isEmpty() ? userService.findAllActive() : userService.findActiveExcludingIds(allMemberIds);
        }
        return new MemberPage(views, Paginator.of(safePage, safeNumber, count, "/space/member?space_id=" + spaceId), manager, otherUsers);
    }

    @Transactional
    /**
     * 创建空间并初始化默认文档目录。
     */
    public JsonResponse<Void> createSpace(CurrentUser currentUser, Space space) throws IOException {
        JsonResponse<Void> validation = validateSpace(space, null);
        if (validation != null) {
            return validation;
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        space.setCreateTime(now);
        space.setUpdateTime(now);
        spaceMapper.insert(space);

        Document defaultDocument = new Document();
        defaultDocument.setDocumentId(UUID.randomUUID().toString().replace("-", ""));
        defaultDocument.setParentId("0");
        defaultDocument.setSpaceId(space.getSpaceId());
        defaultDocument.setName(space.getName());
        defaultDocument.setType(DocumentFileService.DOCUMENT_TYPE_DIR);
        defaultDocument.setPath("0");
        defaultDocument.setSequence(0);
        defaultDocument.setCreateUserId(currentUser.getUserId());
        defaultDocument.setEditUserId(currentUser.getUserId());
        defaultDocument.setCreateTime(now);
        defaultDocument.setUpdateTime(now);
        documentMapper.insert(defaultDocument);
        documentFileService.createEmptyPage(documentFileService.getDefaultPageFileBySpaceName(space.getName()));
        spaceUserService.add(space.getSpaceId(), currentUser.getUserId(), SPACE_MANAGER_PRIVILEGE);
        return JsonResponse.success("添加空间成功", null, "/system/space/list", 2000);
    }

    @Transactional
    /**
     * 更新空间基础信息与访问级别。
     */
    public JsonResponse<Void> updateSpace(CurrentUser currentUser, Space space) throws IOException {
        if (space == null || space.getSpaceId() == null) {
            return JsonResponse.error("空间不存在！", null, "", 2000);
        }
        Space existing = spaceMapper.findActiveById(space.getSpaceId());
        if (existing == null) {
            return JsonResponse.error("空间不存在！", null, "", 2000);
        }
        JsonResponse<Void> validation = validateSpace(space, space.getSpaceId());
        if (validation != null) {
            return validation;
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        space.setUpdateTime(now);
        spaceMapper.update(space);

        Document defaultDocument = documentMapper.findSpaceDefaultDocument(space.getSpaceId());
        if (defaultDocument != null) {
            String oldPageFile = documentFileService.getDefaultPageFileBySpaceName(existing.getName());
            documentFileService.renamePageOrDirectory(
                oldPageFile,
                space.getName(),
                DocumentFileService.DOCUMENT_TYPE_DIR,
                !Objects.equals(existing.getName(), space.getName())
            );
            defaultDocument.setName(space.getName());
            defaultDocument.setEditUserId(currentUser.getUserId());
            defaultDocument.setUpdateTime(now);
            documentMapper.updateDefaultDocumentName(defaultDocument);
        }
        return JsonResponse.success("修改空间成功", null, "/system/space/list", 2000);
    }

    @Transactional
    /**
     * 删除空间并清理对应的文档资源。
     */
    public JsonResponse<Void> deleteSpace(CurrentUser currentUser, Integer spaceId) throws IOException {
        Space space = spaceMapper.findActiveById(spaceId);
        if (space == null) {
            return JsonResponse.error("空间不存在！", null, "", 2000);
        }
        List<Document> documents = documentMapper.findActiveBySpaceId(spaceId);
        if (documents.size() > 1) {
            return JsonResponse.error("不能删除空间，请先删除该空间下文档。", null, "", 2000);
        }
        if (documents.size() == 1 && !Objects.equals(documents.get(0).getName(), space.getName())) {
            return JsonResponse.error("不能删除空间，请先删除该空间下文档。", null, "", 2000);
        }
        if (documents.size() == 1) {
            Document defaultDocument = documents.get(0);
            defaultDocument.setEditUserId(currentUser.getUserId());
            defaultDocument.setUpdateTime(Math.toIntExact(Instant.now().getEpochSecond()));
            documentMapper.markDeleted(defaultDocument);
            attachmentService.deleteByDocumentId(defaultDocument.getDocumentId());
            documentFileService.deletePageOrDirectory(documentFileService.getDefaultPageFileBySpaceName(space.getName()), DocumentFileService.DOCUMENT_TYPE_DIR);
        } else {
            documentFileService.deletePageOrDirectory(documentFileService.getDefaultPageFileBySpaceName(space.getName()), DocumentFileService.DOCUMENT_TYPE_DIR);
        }
        spaceUserService.deleteBySpaceId(spaceId);
        spaceMapper.markDeleted(spaceId);
        return JsonResponse.success("删除空间成功", null, "/system/space/list", 2000);
    }

    /**
     * 打包下载空间下的文档资源。
     */
    public SpaceDownload downloadSpace(Integer spaceId) throws IOException {
        Space space = requireSpace(spaceId);
        List<Attachment> attachments = attachmentService.findBySpaceId(spaceId);
        byte[] payload = zipSpace(space, attachments);
        return new SpaceDownload(space.getName() + ".zip", new ByteArrayResource(payload));
    }

    /**
     * 向私有空间添加成员并设置权限。
     */
    public void addMember(CurrentUser currentUser, Integer spaceId, Integer userId, Integer privilege) {
        Space space = requireSpace(spaceId);
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.manager()) {
            throw new IllegalStateException("您没有权限添加该空间成员。");
        }
        if (spaceUserService.findBySpaceIdAndUserId(spaceId, userId) != null) {
            throw new IllegalStateException("该用户已经是空间成员。");
        }
        spaceUserService.add(spaceId, userId, privilege);
    }

    /**
     * 从私有空间移除成员。
     */
    public void removeMember(CurrentUser currentUser, Integer spaceId, Integer userId, Integer spaceUserId) {
        Space space = requireSpace(spaceId);
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.manager()) {
            throw new IllegalStateException("您没有权限移除该空间成员。");
        }
        SpaceUser membership = spaceUserService.findBySpaceIdAndUserId(spaceId, userId);
        if (membership == null || !spaceUserId.equals(membership.getSpaceUserId())) {
            throw new IllegalStateException("空间成员不存在。");
        }
        spaceUserService.deleteById(spaceUserId);
    }

    /**
     * 更新空间成员权限。
     */
    public void updateMemberPrivilege(CurrentUser currentUser, Integer spaceId, Integer spaceUserId, Integer privilege) {
        Space space = requireSpace(spaceId);
        AccessService.Access access = accessService.access(currentUser, space);
        if (!access.manager()) {
            throw new IllegalStateException("您没有权限修改该空间成员。");
        }
        spaceUserService.updatePrivilege(spaceUserId, privilege);
    }

    private void markCollections(CurrentUser currentUser, List<Space> spaces) {
        List<CollectionEntry> collections = collectionService.findByUserIdAndType(currentUser.getUserId(), CollectionService.TYPE_SPACE);
        Map<Integer, Integer> collectionBySpace = collections.stream()
            .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getResourceId()), CollectionEntry::getCollectionId, (left, right) -> left));
        for (Space space : spaces) {
            Integer collectionId = collectionBySpace.get(space.getSpaceId());
            if (collectionId != null) {
                space.setCollection(true);
                space.setCollectionId(collectionId);
            }
        }
    }

    private void decorate(List<Space> spaces) {
        for (Space space : spaces) {
            if (space.getCreateTime() != null && space.getCreateTime() > 0) {
                space.setCreateDateText(DATE_ONLY.format(Instant.ofEpochSecond(space.getCreateTime())));
            } else {
                space.setCreateDateText("");
            }
        }
    }

    private JsonResponse<Void> validateSpace(Space space, Integer currentId) {
        if (space == null) {
            return JsonResponse.error("空间信息不能为空！", null, "", 2000);
        }
        space.setName(trim(space.getName()));
        space.setDescription(trim(space.getDescription()));
        space.setTags(trim(space.getTags()));
        space.setVisitLevel(trim(space.getVisitLevel()).toLowerCase());
        if (!StringUtils.hasText(space.getVisitLevel())) {
            space.setVisitLevel("public");
        }
        if (!"public".equals(space.getVisitLevel()) && !"private".equals(space.getVisitLevel())) {
            return JsonResponse.error("访问级别不正确！", null, "", 2000);
        }
        space.setIsShare(Objects.equals(space.getIsShare(), 1) ? 1 : 0);
        space.setIsExport(Objects.equals(space.getIsExport(), 1) ? 1 : 0);
        if (!StringUtils.hasText(space.getName())) {
            return JsonResponse.error("空间名称不能为空！", null, "", 2000);
        }
        if (INVALID_SPACE_NAME.matcher(space.getName()).find()) {
            return JsonResponse.error("空间名称格式不正确！", null, "", 2000);
        }
        long duplicate = currentId == null ? spaceMapper.countByName(space.getName()) : spaceMapper.countByNameAndNotId(currentId, space.getName());
        if (duplicate > 0) {
            return JsonResponse.error("空间名已经存在！", null, "", 2000);
        }
        return null;
    }

    private byte[] zipSpace(Space space, List<Attachment> attachments) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(output)) {
            Path markdownDir = documentFileService.resolvePagePath(space.getName());
            if (Files.exists(markdownDir)) {
                addPathToZip(zip, markdownDir, markdownDir.getParent());
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

    private void addPathToZip(ZipOutputStream zip, Path source, Path base) throws IOException {
        if (Files.isDirectory(source)) {
            try (var stream = Files.list(source)) {
                for (Path child : stream.toList()) {
                    addPathToZip(zip, child, base);
                }
            }
            return;
        }
        String entryName = base.relativize(source).toString().replace('\\', '/');
        zip.putNextEntry(new ZipEntry(entryName));
        Files.copy(source, zip);
        zip.closeEntry();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public record SpacePage(List<Space> spaces, long count, String keyword, Paginator paginator) {
    }

    public record SpaceCollectionPage(List<Space> spaces, int count) {
    }

    public record MemberView(User user, Integer privilege, Integer spaceUserId) {
    }

    public record MemberPage(List<MemberView> users, Paginator paginator, boolean manager, List<User> otherUsers) {
    }

    public record SpaceDownload(String fileName, ByteArrayResource resource) {
    }
}
