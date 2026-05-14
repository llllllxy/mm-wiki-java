package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.Access;

import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.service.AccessService;
import org.tinycloud.mmwiki.service.AttachmentService;
import org.tinycloud.mmwiki.service.DocumentFileService;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.EditorImageResponse;

/**
 * MM-Wiki 页面与接口控制器。
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
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + getExtension(originalFileName);
        Path saveFile = saveDir.resolve(storedFileName);
        if (Files.exists(saveFile)) {
            return EditorImageResponse.error("该图片已经上传过。");
        }
        file.transferTo(saveFile);
        try {
            String relativePath = "images/" + document.getSpaceId() + "/" + documentId + "/" + storedFileName;
            String markdownUrl = "/" + relativePath;
            attachmentService.save(currentUser().getUserId(), documentId, originalFileName, relativePath, AttachmentService.SOURCE_IMAGE);
            return EditorImageResponse.success("上传成功", markdownUrl);
        } catch (Exception ex) {
            Files.deleteIfExists(saveFile);
            throw ex;
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ROOT);
    }
}
