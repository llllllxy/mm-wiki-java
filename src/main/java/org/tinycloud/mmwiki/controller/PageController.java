package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.DocumentEditData;
import org.tinycloud.mmwiki.domain.DocumentViewData;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class PageController extends ControllerSupport {

    private final DocumentService documentService;

    public PageController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/page/view")
    public String view(@RequestParam("document_id") String documentId, HttpServletRequest request, Model model) throws Exception {
        DocumentViewData view = documentService.loadDocumentView(documentId, currentUser(request));
        model.addAttribute("space", view.space());
        model.addAttribute("document", view.document());
        model.addAttribute("collection_id", view.collectionId());
        model.addAttribute("create_user", view.createUser());
        model.addAttribute("edit_user", view.editUser());
        model.addAttribute("page_content", view.pageContent());
        model.addAttribute("parent_documents", view.parentDocuments());
        model.addAttribute("is_editor", view.editor());
        model.addAttribute("document_create_time", TimeUtils.formatUnix(view.document().getCreateTime()));
        model.addAttribute("document_update_time", TimeUtils.formatUnix(view.document().getUpdateTime()));
        return "page/view";
    }

    @GetMapping("/page/edit")
    public String edit(@RequestParam("document_id") String documentId, HttpServletRequest request, Model model) throws Exception {
        DocumentEditData editData = documentService.loadEditData(documentId, currentUser(request));
        model.addAttribute("document", editData.document());
        model.addAttribute("page_content", editData.pageContent());
        model.addAttribute("sendEmail", editData.sendEmail());
        model.addAttribute("autoFollowDoc", editData.autoFollowDoc());
        return "page/edit";
    }

    @PostMapping("/page/modify")
    @ResponseBody
    public JsonResponse<Void> modify(
        @RequestParam("document_id") String documentId,
        @RequestParam("name") String name,
        @RequestParam(name = "document_page_editor-markdown-doc", required = false, defaultValue = "") String content,
        @RequestParam(name = "comment", required = false, defaultValue = "") String comment,
        @RequestParam(name = "is_follow_doc", required = false, defaultValue = "0") String isFollowDoc,
        HttpServletRequest request
    ) throws Exception {
        return documentService.modifyPage(currentUser(request), documentId, name, content, comment, "1".equals(isFollowDoc));
    }

    @GetMapping("/page/display")
    public String display(@RequestParam("document_id") String documentId, Model model) throws Exception {
        DocumentService.SharedPageView view = documentService.loadSharedView(documentId);
        model.addAttribute("document", view.document());
        model.addAttribute("parent_documents", view.parentDocuments());
        model.addAttribute("page_content", view.pageContent());
        model.addAttribute("create_user", view.createUser());
        model.addAttribute("edit_user", view.editUser());
        model.addAttribute("document_create_time", TimeUtils.formatUnix(view.document().getCreateTime()));
        model.addAttribute("document_update_time", TimeUtils.formatUnix(view.document().getUpdateTime()));
        return "page/display";
    }

    @GetMapping("/page/export")
    public ResponseEntity<ByteArrayResource> export(@RequestParam("document_id") String documentId, HttpServletRequest request) throws Exception {
        DocumentService.ExportPayload payload = documentService.exportDocument(documentId, currentUser(request));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.fileName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(payload.resource().contentLength())
            .body(payload.resource());
    }
}
