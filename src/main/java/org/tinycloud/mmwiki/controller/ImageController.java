package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
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

@Controller
public class ImageController extends ControllerSupport {

    private final DocumentService documentService;
    private final SpaceService spaceService;
    private final AccessService accessService;
    private final AttachmentService attachmentService;
    private final DocumentFileService documentFileService;

    public ImageController(
        DocumentService documentService,
        SpaceService spaceService,
        AccessService accessService,
        AttachmentService attachmentService,
        DocumentFileService documentFileService
    ) {
        this.documentService = documentService;
        this.spaceService = spaceService;
        this.accessService = accessService;
        this.attachmentService = attachmentService;
        this.documentFileService = documentFileService;
    }

    @PostMapping("/image/upload")
    @ResponseBody
    public EditorImageResponse upload(
        @RequestParam("document_id") String documentId,
        @RequestParam("editormd-image-file") MultipartFile file,
        HttpServletRequest request
    ) throws Exception {
        Document document = documentService.findActiveById(documentId);
        if (document == null) {
            return EditorImageResponse.error("文档不存在。");
        }
        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser(request), space);
        if (!access.editor()) {
            return EditorImageResponse.error("您没有权限操作该空间文档。");
        }
        if (file == null || file.isEmpty()) {
            return EditorImageResponse.error("上传图片错误。");
        }
        Path saveDir = documentFileService.ensureAttachmentDirectory("images", String.valueOf(document.getSpaceId()), documentId);
        Path saveFile = saveDir.resolve(file.getOriginalFilename());
        if (Files.exists(saveFile)) {
            return EditorImageResponse.error("该图片已经上传过。");
        }
        file.transferTo(saveFile);
        try {
            String relativePath = "images/" + document.getSpaceId() + "/" + documentId + "/" + file.getOriginalFilename();
            attachmentService.save(currentUser(request).getUserId(), documentId, file.getOriginalFilename(), relativePath, AttachmentService.SOURCE_IMAGE);
            return EditorImageResponse.success("上传成功", "/" + relativePath);
        } catch (Exception ex) {
            Files.deleteIfExists(saveFile);
            throw ex;
        }
    }
}
