package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.Access;

import org.springframework.core.io.PathResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.AttachmentSourceEnum;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.service.AccessService;
import org.tinycloud.mmwiki.service.AttachmentService;
import org.tinycloud.mmwiki.service.DocumentFileService;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.util.FileUtils;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.EditorImageResponse;

/**
 * 图片控制器，负责编辑器图片上传和受权限控制的图片读取。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class ImageController extends ControllerSupport {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private SpaceService spaceService;
    @Autowired
    private AccessService accessService;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private DocumentFileService documentFileService;

    /**
     * 读取 Markdown 中引用的图片资源，保持 /images/{spaceId}/{documentId}/{fileName} 地址不变，
     * 同时校验图片归属、空间分享状态和当前登录用户的空间访问权限。
     *
     * @param spaceId    空间ID
     * @param documentId 文档ID
     * @param fileName   图片存储文件名
     * @return 图片文件响应
     */
    @GetMapping("/images/{spaceId}/{documentId}/{fileName:.+}")
    public ResponseEntity<PathResource> view(@PathVariable Integer spaceId,
                                             @PathVariable String documentId,
                                             @PathVariable String fileName) throws IOException {
        String relativePath = "images/" + spaceId + "/" + documentId + "/" + fileName;
        Document document = documentService.findActiveById(documentId);
        if (document == null || !Objects.equals(document.getSpaceId(), spaceId)) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "图片不存在。");
        }
        Attachment attachment = attachmentService.findByDocumentIdPathAndSource(documentId, relativePath, AttachmentSourceEnum.IMAGE);
        if (attachment == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "图片不存在。");
        }
        Space space = spaceService.requireSpace(spaceId);
        if (!canVisitImage(space)) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限访问该空间图片。");
        }
        Path path = documentFileService.resolveAttachmentPath(attachment.getPath());
        if (!Files.isRegularFile(path)) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "图片不存在。");
        }
        PathResource resource = new PathResource(path);
        MediaType mediaType = MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(path))
                .body(resource);
    }

    @PostMapping("/image/upload")
    @ResponseBody
    public EditorImageResponse upload(@RequestParam("document_id") String documentId,
                                      @RequestParam("editormd-image-file") MultipartFile file) throws Exception {
        Document document = documentService.findActiveById(documentId);
        if (document == null) {
            return EditorImageResponse.error("文档不存在。");
        }
        Space space = spaceService.requireSpace(document.getSpaceId());
        Access access = accessService.access(currentUser(), space);
        if (!access.isEditor()) {
            return EditorImageResponse.error("您没有权限操作该空间文档。");
        }
        if (file == null || file.isEmpty()) {
            return EditorImageResponse.error("上传图片错误。");
        }
        Path saveDir = documentFileService.ensureAttachmentDirectory("images", String.valueOf(document.getSpaceId()), documentId);
        String originalFileName = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + FileUtils.getExtension(originalFileName);
        Path saveFile = saveDir.resolve(storedFileName);
        if (Files.exists(saveFile)) {
            return EditorImageResponse.error("该图片已经上传过。");
        }
        file.transferTo(saveFile);
        try {
            String relativePath = "images/" + document.getSpaceId() + "/" + documentId + "/" + storedFileName;
            String markdownUrl = "/" + relativePath;
            attachmentService.save(currentUser().getUserId(), documentId, originalFileName, relativePath, AttachmentSourceEnum.IMAGE);
            return EditorImageResponse.success("上传成功", markdownUrl);
        } catch (Exception ex) {
            Files.deleteIfExists(saveFile);
            throw ex;
        }
    }

    /**
     * 判断当前请求是否可以访问空间图片，空间开启分享时允许匿名访问，
     * 否则要求当前登录用户具备该空间访问权限。
     *
     * @param space 图片所属空间
     * @return true 表示允许访问，false 表示拒绝访问
     */
    private boolean canVisitImage(Space space) {
        if (Objects.equals(space.getIsShare(), 1)) {
            return true;
        }
        CurrentUser currentUser = currentUser();
        if (currentUser == null) {
            return false;
        }
        return accessService.access(currentUser, space).isVisit();
    }

}
