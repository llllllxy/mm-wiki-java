package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.DocumentViewData;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class DocumentController extends ControllerSupport {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/document/index")
    public String index(@RequestParam("document_id") String documentId, HttpServletRequest request, Model model) throws Exception {
        DocumentViewData view = documentService.loadDocumentView(documentId, currentUser(request));
        model.addAttribute("documents", view.documents());
        model.addAttribute("documentsJson", documentService.toTreeJson(view.documents()));
        model.addAttribute("default_document_id", view.document().getDocumentId());
        model.addAttribute("space", view.space());
        model.addAttribute("space_document", view.spaceDocument());
        model.addAttribute("is_editor", view.editor());
        model.addAttribute("is_delete", view.manager());
        return "document/index";
    }

    @GetMapping("/document/add")
    public String add(
        @RequestParam("space_id") Integer spaceId,
        @RequestParam("parent_id") String parentId,
        HttpServletRequest request,
        Model model
    ) throws Exception {
        DocumentViewData parentView = documentService.loadDocumentView(parentId, currentUser(request));
        model.addAttribute("parent_documents", documentService.getParentDocuments(parentView.document()));
        model.addAttribute("parent_id", parentId);
        model.addAttribute("space_id", spaceId);
        return "document/form";
    }

    @PostMapping("/document/save")
    @ResponseBody
    public JsonResponse<Void> save(
        @RequestParam("space_id") Integer spaceId,
        @RequestParam("parent_id") String parentId,
        @RequestParam("type") Integer type,
        @RequestParam("name") String name,
        HttpServletRequest request
    ) throws Exception {
        return documentService.createDocument(currentUser(request), spaceId, parentId, type, name);
    }

    @GetMapping("/document/history")
    public String history(
        @RequestParam("document_id") String documentId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int number,
        HttpServletRequest request,
        Model model
    ) {
        DocumentService.HistoryPage history = documentService.loadHistory(documentId, currentUser(request), page, number);
        model.addAttribute("logDocuments", history.items());
        model.addAttribute("paginator", history.paginator());
        return "document/history";
    }

    @PostMapping("/document/move")
    @ResponseBody
    public JsonResponse<Void> move(
        @RequestParam("document_id") String documentId,
        @RequestParam("target_id") String targetId,
        @RequestParam(name = "move_type", required = false, defaultValue = "") String moveType,
        HttpServletRequest request
    ) throws Exception {
        return documentService.moveDocument(currentUser(request), documentId, targetId, moveType);
    }

    @PostMapping("/document/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("document_id") String documentId, HttpServletRequest request) throws Exception {
        return documentService.deleteDocument(currentUser(request), documentId);
    }
}
