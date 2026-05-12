package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.DocumentEditData;
import org.tinycloud.mmwiki.vo.DocumentViewData;
import org.tinycloud.mmwiki.vo.ExportPayload;
import org.tinycloud.mmwiki.vo.SharedPageView;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class PageController extends ControllerSupport {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/page/view")
    public String view(@RequestParam("document_id") String documentId, Model model) throws Exception {
        DocumentViewData view = documentService.loadDocumentView(documentId, currentUser());
        model.addAttribute("space", view.getSpace());
        model.addAttribute("document", view.getDocument());
        model.addAttribute("collection_id", view.getCollectionId());
        model.addAttribute("create_user", view.getCreateUser());
        model.addAttribute("edit_user", view.getEditUser());
        model.addAttribute("page_content", view.getPageContent());
        model.addAttribute("parent_documents", view.getParentDocuments());
        model.addAttribute("is_editor", view.isEditor());
        model.addAttribute("document_create_time", TimeUtils.format(view.getDocument().getCreateTime()));
        model.addAttribute("document_update_time", TimeUtils.format(view.getDocument().getUpdateTime()));
        return "page/view";
    }

    @GetMapping("/page/edit")
    public String edit(@RequestParam("document_id") String documentId, Model model) throws Exception {
        DocumentEditData editData = documentService.loadEditData(documentId, currentUser());
        model.addAttribute("document", editData.getDocument());
        model.addAttribute("page_content", editData.getPageContent());
        model.addAttribute("sendEmail", editData.getSendEmail());
        model.addAttribute("autoFollowDoc", editData.getAutoFollowDoc());
        return "page/edit";
    }

    @PostMapping("/page/modify")
    @ResponseBody
    public JsonResponse<Void> modify(
            @RequestParam("document_id") String documentId,
            @RequestParam("name") String name,
            @RequestParam(name = "document_page_editor-markdown-doc", required = false, defaultValue = "") String content,
            @RequestParam(name = "comment", required = false, defaultValue = "") String comment,
            @RequestParam(name = "is_notice_user", required = false, defaultValue = "0") String isNoticeUser,
            @RequestParam(name = "is_follow_doc", required = false, defaultValue = "0") String isFollowDoc,
            HttpServletRequest request
    ) throws Exception {
        return documentService.modifyPage(
                currentUser(),
                documentId,
                name,
                content,
                comment,
                "1".equals(isFollowDoc),
                "1".equals(isNoticeUser),
                documentUrl(request, documentId)
        );
    }

    @GetMapping("/page/display")
    public String display(@RequestParam("document_id") String documentId, Model model) throws Exception {
        SharedPageView view = documentService.loadSharedView(documentId);
        model.addAttribute("document", view.getDocument());
        model.addAttribute("parent_documents", view.getParentDocuments());
        model.addAttribute("page_content", view.getPageContent());
        model.addAttribute("create_user", view.getCreateUser());
        model.addAttribute("edit_user", view.getEditUser());
        model.addAttribute("document_create_time", TimeUtils.format(view.getDocument().getCreateTime()));
        model.addAttribute("document_update_time", TimeUtils.format(view.getDocument().getUpdateTime()));
        return "page/display";
    }

    @GetMapping("/page/export")
    public ResponseEntity<ByteArrayResource> export(
            @RequestParam("document_id") String documentId,
            @RequestParam(
                    name = "output",
                    required = false,
                    defaultValue = "markdown"
            ) String output
    ) throws Exception {
        ExportPayload payload = documentService.exportDocument(documentId, currentUser(), output);
        String encoded = URLEncoder.encode(payload.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(payload.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM : payload.getContentType())
                .contentLength(payload.getResource().contentLength())
                .body(payload.getResource());
    }

    private String documentUrl(HttpServletRequest request, String documentId) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port) + "/document/index?document_id=" + documentId;
    }
}
