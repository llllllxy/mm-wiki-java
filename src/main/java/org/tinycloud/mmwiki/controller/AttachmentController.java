package org.tinycloud.mmwiki.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.tinycloud.mmwiki.constant.AttachmentSourceEnum;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
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
import org.tinycloud.mmwiki.vo.Access;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * 附件控制器，负责文档附件页面、附件上传、下载和删除接口。
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
        Document document = this.requireDocument(documentId);
        Access access = this.requireAccess(document);
        model.addAttribute("document_id", documentId);
        model.addAttribute("is_upload", access.isEditor());
        model.addAttribute("is_delete", access.isManager());
        return "attachment/page";
    }

    /**
     * 异步分页加载普通附件列表，加载前校验当前用户是否有文档访问权限。
     *
     * @param documentId 文档ID
     * @param pageNum    当前页码
     * @param pageSize   每页数量
     * @return 普通附件分页数据
     */
    @PostMapping("/attachment/page")
    @ResponseBody
    public JsonResponse<PageModel<Attachment>> pageData(@RequestParam("document_id") String documentId,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        Document document = this.requireDocument(documentId);
        this.requireAccess(document);
        return JsonResponse.success("查询成功", this.attachmentService.pageByDocumentIdAndSource(documentId, AttachmentSourceEnum.ATTACHMENT, pageNum, pageSize));
    }

    @GetMapping("/attachment/image")
    public String image(@RequestParam("document_id") String documentId, Model model) {
        Document document = this.requireDocument(documentId);
        Access access = this.requireAccess(document);
        model.addAttribute("document_id", documentId);
        model.addAttribute("is_delete", access.isManager());
        return "attachment/image";
    }

    /**
     * 异步分页加载图片附件列表，加载前校验当前用户是否有文档访问权限。
     *
     * @param documentId 文档ID
     * @param pageNum    当前页码
     * @param pageSize   每页数量
     * @return 图片附件分页数据
     */
    @PostMapping("/attachment/image")
    @ResponseBody
    public JsonResponse<PageModel<Attachment>> imageData(@RequestParam("document_id") String documentId,
                                                         @RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        Document document = this.requireDocument(documentId);
        this.requireAccess(document);
        return JsonResponse.success("查询成功", this.attachmentService.pageByDocumentIdAndSource(documentId, AttachmentSourceEnum.IMAGE, pageNum, pageSize));
    }

    @PostMapping("/attachment/upload")
    @ResponseBody
    public JsonResponse<Void> upload(@RequestParam("document_id") String documentId,
                                     @RequestParam("attachment") MultipartFile file) throws Exception {
        Document document = this.requireDocument(documentId);
        Access access = this.requireAccess(document);
        if (!access.isEditor()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限操作该空间文档。");
        }
        if (file == null || file.isEmpty()) {
            throw new SystemException("上传附件错误。");
        }
        Path saveDir = this.documentFileService.ensureAttachmentDirectory("attachment", String.valueOf(document.getSpaceId()), documentId);
        String originalFileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "attachment";
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + FileUtils.getExtension(originalFileName);
        Path saveFile = saveDir.resolve(storedFileName);
        if (Files.exists(saveFile)) {
            throw new SystemException("该附件已经存在。");
        }
        file.transferTo(saveFile);
        try {
            String relativePath = "attachment/" + document.getSpaceId() + "/" + documentId + "/" + storedFileName;
            this.attachmentService.save(
                    this.currentUser().getUserId(),
                    documentId,
                    originalFileName,
                    relativePath,
                    AttachmentSourceEnum.ATTACHMENT
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
        Attachment attachment = this.attachmentService.findById(attachmentId);
        if (attachment == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "附件不存在。");
        }
        Document document = this.requireDocument(attachment.getDocumentId());
        Access access = this.requireAccess(document);
        if (!access.isManager()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限删除该空间文档附件。");
        }
        this.attachmentService.deleteById(attachmentId);
        String redirect = AttachmentSourceEnum.IMAGE.is(attachment.getSource())
                ? "/attachment/image?document_id=" + document.getDocumentId()
                : "/attachment/page?document_id=" + document.getDocumentId();
        return JsonResponse.success("删除成功", redirect);
    }

    @GetMapping("/attachment/download")
    public ResponseEntity<PathResource> download(@RequestParam("attachment_id") Integer attachmentId) throws Exception {
        Attachment attachment = this.attachmentService.findById(attachmentId);
        if (attachment == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "附件不存在。");
        }
        Document document = this.requireDocument(attachment.getDocumentId());
        Access access = this.requireAccess(document);
        if (!access.isVisit()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限下载该空间附件。");
        }
        Path path = this.documentFileService.resolveAttachmentPath(attachment.getPath());
        PathResource resource = new PathResource(path);
        String downloadFileName = StringUtils.hasText(attachment.getName()) ? attachment.getName() : "attachment";
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(downloadFileName, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    private Document requireDocument(String documentId) {
        Document document = this.documentService.findActiveById(documentId);
        if (document == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "文档不存在。");
        }
        return document;
    }

    private Access requireAccess(Document document) {
        Space space = this.spaceService.requireSpace(document.getSpaceId());
        Access access = this.accessService.access(this.currentUser(), space);
        if (!access.isVisit()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限访问该空间文档。");
        }
        return access;
    }
}
