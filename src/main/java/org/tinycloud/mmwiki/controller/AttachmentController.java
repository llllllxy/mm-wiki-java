package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.tinycloud.mmwiki.domain.Attachment;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.service.AccessService;
import org.tinycloud.mmwiki.service.AttachmentService;
import org.tinycloud.mmwiki.service.DocumentFileService;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

@Controller
public class AttachmentController extends ControllerSupport {

    private final DocumentService documentService;
    private final SpaceService spaceService;
    private final AccessService accessService;
    private final AttachmentService attachmentService;
    private final DocumentFileService documentFileService;

    public AttachmentController(
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

    @GetMapping("/attachment/page")
    public String page(@RequestParam("document_id") String documentId, HttpServletRequest request, Model model) {
        Document document = requireDocument(documentId);
        AccessService.Access access = requireAccess(request, document);
        model.addAttribute("attachments", attachmentService.findByDocumentIdAndSource(documentId, AttachmentService.SOURCE_ATTACHMENT));
        model.addAttribute("document_id", documentId);
        model.addAttribute("is_upload", access.editor());
        model.addAttribute("is_delete", access.manager());
        return "attachment/page";
    }

    @GetMapping("/attachment/image")
    public String image(@RequestParam("document_id") String documentId, HttpServletRequest request, Model model) {
        Document document = requireDocument(documentId);
        AccessService.Access access = requireAccess(request, document);
        model.addAttribute("attachments", attachmentService.findByDocumentIdAndSource(documentId, AttachmentService.SOURCE_IMAGE));
        model.addAttribute("document_id", documentId);
        model.addAttribute("is_delete", access.manager());
        return "attachment/image";
    }

    @PostMapping("/attachment/upload")
    @ResponseBody
    public JsonResponse<Void> upload(
        @RequestParam("document_id") String documentId,
        @RequestParam("attachment") MultipartFile file,
        HttpServletRequest request
    ) throws Exception {
        Document document = requireDocument(documentId);
        AccessService.Access access = requireAccess(request, document);
        if (!access.editor()) {
            return JsonResponse.error("您没有权限操作该空间文档。", null, "", 2000);
        }
        if (file == null || file.isEmpty()) {
            return JsonResponse.error("上传附件错误。", null, "", 2000);
        }
        Path saveDir = documentFileService.ensureAttachmentDirectory("attachment", String.valueOf(document.getSpaceId()), documentId);
        Path saveFile = saveDir.resolve(file.getOriginalFilename());
        if (Files.exists(saveFile)) {
            return JsonResponse.error("该附件已经存在。", null, "", 2000);
        }
        file.transferTo(saveFile);
        try {
            attachmentService.save(
                currentUser(request).getUserId(),
                documentId,
                file.getOriginalFilename(),
                "attachment/" + document.getSpaceId() + "/" + documentId + "/" + file.getOriginalFilename(),
                AttachmentService.SOURCE_ATTACHMENT
            );
            return JsonResponse.success("附件上传成功", null, "/attachment/page?document_id=" + documentId, 2000);
        } catch (Exception ex) {
            Files.deleteIfExists(saveFile);
            throw ex;
        }
    }

    @PostMapping("/attachment/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("attachment_id") Integer attachmentId, HttpServletRequest request) throws Exception {
        Attachment attachment = attachmentService.findById(attachmentId);
        if (attachment == null) {
            return JsonResponse.error("附件不存在。", null, "", 2000);
        }
        Document document = requireDocument(attachment.getDocumentId());
        AccessService.Access access = requireAccess(request, document);
        if (!access.manager()) {
            return JsonResponse.error("您没有权限删除该空间文档附件。", null, "", 2000);
        }
        attachmentService.deleteById(attachmentId);
        String redirect = attachment.getSource() != null && attachment.getSource() == AttachmentService.SOURCE_IMAGE
            ? "/attachment/image?document_id=" + document.getDocumentId()
            : "/attachment/page?document_id=" + document.getDocumentId();
        return JsonResponse.success("删除成功", null, redirect, 2000);
    }

    @GetMapping("/attachment/download")
    public ResponseEntity<PathResource> download(@RequestParam("attachment_id") Integer attachmentId, HttpServletRequest request) throws Exception {
        Attachment attachment = attachmentService.findById(attachmentId);
        if (attachment == null) {
            throw new IllegalStateException("附件不存在。");
        }
        Document document = requireDocument(attachment.getDocumentId());
        AccessService.Access access = requireAccess(request, document);
        if (!access.visit()) {
            throw new IllegalStateException("您没有权限下载该空间附件。");
        }
        Path path = documentFileService.resolveAttachmentPath(attachment.getPath());
        PathResource resource = new PathResource(path);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(resource.contentLength())
            .body(resource);
    }

    private Document requireDocument(String documentId) {
        Document document = documentService.findActiveById(documentId);
        if (document == null) {
            throw new IllegalStateException("文档不存在。");
        }
        return document;
    }

    private AccessService.Access requireAccess(HttpServletRequest request, Document document) {
        Space space = spaceService.requireSpace(document.getSpaceId());
        AccessService.Access access = accessService.access(currentUser(request), space);
        if (!access.visit()) {
            throw new IllegalStateException("您没有权限访问该空间文档。");
        }
        return access;
    }
}
