package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.DocumentViewData;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.DocumentHistoryView;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

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
    public String index(@RequestParam("document_id") String documentId, Model model) throws Exception {
        DocumentViewData view = documentService.loadDocumentView(documentId, currentUser());
        model.addAttribute("documents", view.getDocuments());
        model.addAttribute("documentsJson", documentService.toTreeJson(view.getDocuments()));
        model.addAttribute("default_document_id", view.getDocument().getDocumentId());
        model.addAttribute("space", view.getSpace());
        model.addAttribute("space_document", view.getSpaceDocument());
        model.addAttribute("is_editor", view.isEditor());
        model.addAttribute("is_delete", view.isManager());
        return "document/index";
    }

    @GetMapping("/document/add")
    public String add(
        @RequestParam("space_id") Integer spaceId,
        @RequestParam("parent_id") String parentId,
        Model model
    ) throws Exception {
        DocumentViewData parentView = documentService.loadDocumentView(parentId, currentUser());
        model.addAttribute("parent_documents", documentService.getParentDocuments(parentView.getDocument()));
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
        @RequestParam("name") String name
    ) throws Exception {
        return documentService.createDocument(currentUser(), spaceId, parentId, type, name);
    }

    @GetMapping("/document/history")
    public String history(
        @RequestParam("document_id") String documentId,
        Model model
    ) {
        model.addAttribute("documentId", documentId);
        return "document/history";
    }

    @PostMapping("/document/history")
    @ResponseBody
    public JsonResponse<PageModel<DocumentHistoryView>> historyData(
            @RequestParam("document_id") String documentId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return JsonResponse.success("查询成功", documentService.historyPage(documentId, currentUser(), pageNum, pageSize));
    }

    @PostMapping("/document/move")
    @ResponseBody
    public JsonResponse<Void> move(
        @RequestParam("document_id") String documentId,
        @RequestParam("target_id") String targetId,
        @RequestParam(
                name = "move_type",
                required = false,
                defaultValue = ""
        ) String moveType
    ) throws Exception {
        return documentService.moveDocument(currentUser(), documentId, targetId, moveType);
    }

    @PostMapping("/document/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("document_id") String documentId) throws Exception {
        return documentService.deleteDocument(currentUser(), documentId);
    }
}
