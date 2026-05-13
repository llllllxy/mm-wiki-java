package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.*;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.SpaceMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.vo.*;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class SpaceService {
    private static final Pattern INVALID_SPACE_NAME = Pattern.compile("[\\\\/:*?\"<>|]");

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
        space.setCreateDateText(TimeUtils.format(space.getCreateTime()));
        return space;
    }

    /**
     * 统计当前可用的空间标签。
     */
    public Set<String> listTags(CurrentUser currentUser) {
        Set<String> tags = new LinkedHashSet<>();
        for (String tagText : spaceMapper.findVisibleTags(currentUser.getUserId(), isRoot(currentUser))) {
            if (tagText == null || tagText.isBlank()) {
                continue;
            }
            for (String tag : tagText.split(",")) {
                String clean = tag.trim();
                if (!clean.isEmpty()) {
                    tags.add(clean);
                }
            }
        }
        return tags;
    }

    /**
     * 分页查询当前用户可访问的空间列表，并标记空间收藏状态。
     *
     * @param currentUser 当前登录用户
     * @param keyword     空间名称或描述关键字
     * @param pageNum     当前页码
     * @param pageSize    每页数量
     * @return 空间分页数据
     */
    public PageModel<Space> listSpacesPage(CurrentUser currentUser, String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        PageInfo<Space> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> {
                    spaceMapper.pageByKeywordAndUser(currentUser.getUserId(), isRoot(currentUser), search);
                });
        List<Space> spaces = pageInfo.getList();
        markCollections(currentUser, spaces);
        spaces.forEach(space -> space.setCreateDateText(TimeUtils.format(space.getCreateTime())));
        return PageModel.from(pageInfo);
    }

    /**
     * 加载当前用户收藏的空间列表。
     */
    public PageModel<Space> listCollectedSpaces(CurrentUser currentUser, int pageNum, int pageSize) {
        PageInfo<Space> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> spaceMapper.pageCollectedByUser(currentUser.getUserId(), isRoot(currentUser)));
        List<Space> spaces = pageInfo.getList();
        for (Space space : spaces) {
            space.setCollection(true);
        }
        spaces.forEach(space -> space.setCreateDateText(TimeUtils.format(space.getCreateTime())));
        return PageModel.from(pageInfo);
    }

    /**
     * 按标签筛选当前用户可访问的空间。
     */
    public PageModel<Space> searchByTag(CurrentUser currentUser, String tag, int pageNum, int pageSize) {
        String cleanTag = tag == null ? "" : tag.trim();
        PageInfo<Space> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> spaceMapper.pageByTagAndUser(currentUser.getUserId(), isRoot(currentUser), cleanTag));
        List<Space> spaces = pageInfo.getList();
        markCollections(currentUser, spaces);
        spaces.forEach(space -> space.setCreateDateText(TimeUtils.format(space.getCreateTime())));
        return PageModel.from(pageInfo);
    }


    /**
     * 分页加载空间成员与可添加用户（非空间成员用户）。
     */
    public MemberPage getMemberPageInfo(CurrentUser currentUser, Integer spaceId) {
        return getMemberPageInfo(currentUser, spaceId, "/space/member?space_id=" + spaceId);
    }

    /**
     * 加载空间成员页面基础数据，并根据当前用户权限决定是否返回可添加用户列表。
     *
     * @param currentUser 当前登录用户
     * @param spaceId     空间ID
     * @param basePath    成员页面基础路径
     * @return 成员页面基础数据
     */
    public MemberPage getMemberPageInfo(CurrentUser currentUser, Integer spaceId, String basePath) {
        Space space = requireSpace(spaceId);
        Access access = currentUser == null ? new Access(true, false, false) : null;
        if (currentUser != null) {
            access = accessService.access(currentUser, space);
            if (!access.isVisit()) {
                throw new IllegalStateException("您没有权限访问该空间成员列表。");
            }
        }

        boolean manager = access != null && access.isManager();
        List<User> otherUsers = Collections.emptyList();
        if (manager) {
            List<Integer> existsUserIds = spaceUserService.findBySpaceId(spaceId).stream().map(SpaceUser::getUserId).toList();
            otherUsers = userService.findActiveExcludingIds(existsUserIds);
        }
        return new MemberPage(manager, otherUsers);
    }

    /**
     * 分页查询空间成员列表。
     *
     * @param currentUser 当前登录用户
     * @param spaceId     空间ID
     * @param pageNum     当前页码
     * @param pageSize    每页数量
     * @return 空间成员分页数据
     */
    public PageModel<MemberView> listMembersPage(CurrentUser currentUser, Integer spaceId, int pageNum, int pageSize) {
        Space space = requireSpace(spaceId);
        Access access = accessService.access(currentUser, space);
        if (!access.isVisit()) {
            throw new IllegalStateException("您没有权限访问该空间成员列表。");
        }
        PageInfo<SpaceUser> pageInfo = PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> spaceUserService.pageBySpaceId(spaceId));
        List<Integer> userIds = pageInfo.getList().stream().map(SpaceUser::getUserId).toList();
        Map<Integer, User> users = userIds.isEmpty() ? new HashMap<>() : userService.findActiveByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, item -> item));
        List<MemberView> views = new ArrayList<>();
        for (SpaceUser member : pageInfo.getList()) {
            User user = users.get(member.getUserId());
            if (user != null) {
                views.add(new MemberView(user, member.getPrivilege(), member.getSpaceUserId()));
            }
        }
        return PageModel.build((long) pageInfo.getPageNum(), (long) pageInfo.getPageSize(), views, pageInfo.getTotal(), (long) pageInfo.getPages());
    }


    /**
     * 创建空间并初始化默认文档目录。
     */
    @Transactional
    public JsonResponse<Void> createSpace(CurrentUser currentUser, Space space) throws IOException {
        JsonResponse<Void> validation = validateSpace(space, null);
        if (validation != null) {
            return validation;
        }
        LocalDateTime now = LocalDateTime.now();
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
        spaceUserService.add(space.getSpaceId(), currentUser.getUserId(), AccessService.SPACE_MANAGER);
        return JsonResponse.success("添加空间成功", "/system/space/list");
    }


    /**
     * 更新空间基础信息与访问级别。
     */
    @Transactional
    public JsonResponse<Void> updateSpace(CurrentUser currentUser, Space space) throws IOException {
        if (space == null || space.getSpaceId() == null) {
            return JsonResponse.error("空间不存在！");
        }
        Space existing = spaceMapper.findActiveById(space.getSpaceId());
        if (existing == null) {
            return JsonResponse.error("空间不存在！");
        }
        Access access = accessService.access(currentUser, existing);
        if (!access.isManager()) {
            return JsonResponse.error("您没有权限修改该空间。");
        }
        JsonResponse<Void> validation = validateSpace(space, space.getSpaceId());
        if (validation != null) {
            return validation;
        }
        LocalDateTime now = LocalDateTime.now();
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
        return JsonResponse.success("修改空间成功", "/system/space/list");
    }


    /**
     * 删除空间并清理对应的文档资源。
     */
    @Transactional
    public JsonResponse<Void> deleteSpace(CurrentUser currentUser, Integer spaceId) throws IOException {
        Space space = spaceMapper.findActiveById(spaceId);
        if (space == null) {
            return JsonResponse.error("空间不存在！");
        }
        Access access = accessService.access(currentUser, space);
        if (!access.isManager()) {
            return JsonResponse.error("您没有权限删除该空间。");
        }
        List<Document> documents = documentMapper.findActiveBySpaceId(spaceId);
        if (documents.size() > 1) {
            return JsonResponse.error("不能删除空间，请先删除该空间下文档。");
        }
        if (documents.size() == 1 && !Objects.equals(documents.get(0).getName(), space.getName())) {
            return JsonResponse.error("不能删除空间，请先删除该空间下文档。");
        }
        if (documents.size() == 1) {
            Document defaultDocument = documents.get(0);
            defaultDocument.setEditUserId(currentUser.getUserId());
            defaultDocument.setUpdateTime(TimeUtils.now());
            documentMapper.markDeleted(defaultDocument);
            attachmentService.deleteByDocumentId(defaultDocument.getDocumentId());
            documentFileService.deletePageOrDirectory(documentFileService.getDefaultPageFileBySpaceName(space.getName()), DocumentFileService.DOCUMENT_TYPE_DIR);
        } else {
            documentFileService.deletePageOrDirectory(documentFileService.getDefaultPageFileBySpaceName(space.getName()), DocumentFileService.DOCUMENT_TYPE_DIR);
        }
        spaceUserService.deleteBySpaceId(spaceId);
        spaceMapper.markDeleted(spaceId);
        return JsonResponse.success("删除空间成功", "/system/space/list");
    }

    /**
     * 打包下载空间下的文档资源。
     */
    public SpaceDownload downloadSpace(CurrentUser currentUser, Integer spaceId) throws IOException {
        Space space = requireSpace(spaceId);
        Access access = accessService.access(currentUser, space);
        if (!access.isVisit()) {
            throw new IllegalStateException("您没有权限导出该空间。");
        }
        List<Attachment> attachments = attachmentService.findBySpaceId(spaceId);
        byte[] payload = zipSpace(space, attachments);
        return new SpaceDownload(space.getName() + ".zip", new ByteArrayResource(payload));
    }

    /**
     * 向私有空间添加成员并设置权限。
     */
    public void addMember(CurrentUser currentUser, Integer spaceId, Integer userId, Integer privilege) {
        Space space = requireSpace(spaceId);
        Access access = accessService.access(currentUser, space);
        if (!access.isManager()) {
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
        Access access = accessService.access(currentUser, space);
        if (!access.isManager()) {
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
        Access access = accessService.access(currentUser, space);
        if (!access.isManager()) {
            throw new IllegalStateException("您没有权限修改该空间成员。");
        }
        spaceUserService.updatePrivilege(spaceUserId, privilege);
    }

    /**
     * 给空间列表设置当前用户的收藏状态和收藏ID。
     *
     * @param currentUser 当前登录用户
     * @param spaces      待处理的空间列表
     */
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


    /**
     * 判断当前用户是否为 root 角色。超级管理员
     *
     * @param currentUser 当前登录用户
     * @return true 表示 root 用户，false 表示普通用户或未登录用户
     */
    private boolean isRoot(CurrentUser currentUser) {
        return currentUser != null
                && currentUser.getRoleId() != null
                && currentUser.getRoleId() == AccessService.ROLE_ROOT_ID;
    }

    /**
     * 校验空间基础信息，并规范化可空字段和开关字段。
     *
     * @param space     待校验的空间对象
     * @param currentId 当前空间ID，新增时为空，编辑时用于排除自身重名
     * @return 校验失败时返回错误响应，校验通过时返回 null
     */
    private JsonResponse<Void> validateSpace(Space space, Integer currentId) {
        if (space == null) {
            return JsonResponse.error("空间信息不能为空！");
        }
        space.setName(trim(space.getName()));
        space.setDescription(trim(space.getDescription()));
        space.setTags(trim(space.getTags()));
        space.setVisitLevel(trim(space.getVisitLevel()).toLowerCase());
        if (!StringUtils.hasText(space.getVisitLevel())) {
            space.setVisitLevel("public");
        }
        if (!"public".equals(space.getVisitLevel()) && !"private".equals(space.getVisitLevel())) {
            return JsonResponse.error("访问级别不正确！");
        }
        space.setIsShare(Objects.equals(space.getIsShare(), 1) ? 1 : 0);
        space.setIsExport(Objects.equals(space.getIsExport(), 1) ? 1 : 0);
        if (!StringUtils.hasText(space.getName())) {
            return JsonResponse.error("空间名称不能为空！");
        }
        if (INVALID_SPACE_NAME.matcher(space.getName()).find()) {
            return JsonResponse.error("空间名称格式不正确！");
        }
        long duplicate = currentId == null ? spaceMapper.countByName(space.getName()) : spaceMapper.countByNameAndNotId(currentId, space.getName());
        if (duplicate > 0) {
            return JsonResponse.error("空间名已经存在！");
        }
        return null;
    }

    /**
     * 将空间下的 Markdown 文档目录和附件打包成 ZIP 字节数组。
     *
     * @param space       待导出的空间
     * @param attachments 空间下的附件列表
     * @return ZIP 文件字节数组
     * @throws IOException 文件读取或 ZIP 写入失败时抛出
     */
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

    /**
     * 递归将文件或目录写入 ZIP 输出流。
     *
     * @param zip    ZIP 输出流
     * @param source 当前待写入的文件或目录
     * @param base   计算 ZIP 内相对路径的基准目录
     * @throws IOException 文件读取或 ZIP 写入失败时抛出
     */
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

    /**
     * 去除字符串首尾空白，空值统一转换为空字符串。
     *
     * @param value 原始字符串
     * @return 去除首尾空白后的字符串
     */
    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
