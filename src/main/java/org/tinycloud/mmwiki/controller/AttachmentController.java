package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.vo.Access;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class AttachmentController extends ControllerSupport {

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

    @GetMapping("/attachment/page")
    public String page(@RequestParam("document_id") String documentId, Model model) {
        Document document = requireDocument(documentId);
        Access access = requireAccess(document);
        model.addAttribute("attachments", attachmentService.findByDocumentIdAndSource(documentId, AttachmentService.SOURCE_ATTACHMENT));
        model.addAttribute("document_id", documentId);
        model.addAttribute("is_upload", access.isEditor());
        model.addAttribute("is_delete", access.isManager());
        return "attachment/page";
    }

    @GetMapping("/attachment/image")
    public String image(@RequestParam("document_id") String documentId, Model model) {
        Document document = requireDocument(documentId);
        Access access = requireAccess(document);
        model.addAttribute("attachments", attachmentService.findByDocumentIdAndSource(documentId, AttachmentService.SOURCE_IMAGE));
        model.addAttribute("document_id", documentId);
        model.addAttribute("is_delete", access.isManager());
        return "attachment/image";
    }

    @PostMapping("/attachment/upload")
    @ResponseBody
    public JsonResponse<Void> upload(@RequestParam("document_id") String documentId,
                                     @RequestParam("attachment") MultipartFile file) throws Exception {
        Document document = requireDocument(documentId);
        Access access = requireAccess(document);
        if (!access.isEditor()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限操作该空间文档。");
        }
        if (file == null || file.isEmpty()) {
            throw new SystemException("上传附件错误。");
        }
        Path saveDir = documentFileService.ensureAttachmentDirectory("attachment", String.valueOf(document.getSpaceId()), documentId);
        Path saveFile = saveDir.resolve(file.getOriginalFilename());
        if (Files.exists(saveFile)) {
            throw new SystemException("该附件已经存在。");
        }
        file.transferTo(saveFile);
        try {
            attachmentService.save(
                    currentUser().getUserId(),
                    documentId,
                    file.getOriginalFilename(),
                    "attachment/" + document.getSpaceId() + "/" + documentId + "/" + file.getOriginalFilename(),
                    AttachmentService.SOURCE_ATTACHMENT
            );
            return JsonResponse.success("附件上传成功", "/attachment/page?document_id=" + documentId);
        } catch (Exception ex) {
            Files.deleteIfExists(saveFile);
            throw ex;
        }
    }

    @PostMapping("/attachment/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("attachment_id") Integer attachmentId) throws Exception {
        Attachment attachment = attachmentService.findById(attachmentId);
        if (attachment == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "附件不存在。");
        }
        Document document = requireDocument(attachment.getDocumentId());
        Access access = requireAccess(document);
        if (!access.isManager()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限删除该空间文档附件。");
        }
        attachmentService.deleteById(attachmentId);
        String redirect = attachment.getSource() != null && attachment.getSource() == AttachmentService.SOURCE_IMAGE
                ? "/attachment/image?document_id=" + document.getDocumentId()
                : "/attachment/page?document_id=" + document.getDocumentId();
        return JsonResponse.success("删除成功", redirect);
    }

    @GetMapping("/attachment/download")
    public ResponseEntity<PathResource> download(@RequestParam("attachment_id") Integer attachmentId) throws Exception {
        Attachment attachment = attachmentService.findById(attachmentId);
        if (attachment == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "附件不存在。");
        }
        Document document = requireDocument(attachment.getDocumentId());
        Access access = requireAccess(document);
        if (!access.isVisit()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限下载该空间附件。");
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
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "文档不存在。");
        }
        return document;
    }

    private Access requireAccess(Document document) {
        Space space = spaceService.requireSpace(document.getSpaceId());
        Access access = accessService.access(currentUser(), space);
        if (!access.isVisit()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限访问该空间文档。");
        }
        return access;
    }
}
