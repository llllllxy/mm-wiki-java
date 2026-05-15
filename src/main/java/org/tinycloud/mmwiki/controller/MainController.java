package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.service.MainService;
import org.tinycloud.mmwiki.vo.MainDefaultView;
import org.tinycloud.mmwiki.vo.SearchView;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

import java.util.List;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class MainController extends ControllerSupport {

    @Autowired
    private MainService mainService;

    @GetMapping({"/", "/main/index"})
    public String index(Model model) {
        nav(model, "main");
        CurrentUser currentUser = currentUser();
        List<Document> documents = mainService.loadCollectedDocuments(currentUser);
        model.addAttribute("documents", documents);
        model.addAttribute("count", documents.size());
        return "main/index";
    }

    @GetMapping("/main/default")
    public String defaultPage(Model model) {
        nav(model, "main");
        MainDefaultView view = mainService.loadDefaultView();
        model.addAttribute("panel_title", view.getPanelTitle());
        model.addAttribute("panel_description", view.getPanelDescription());
        model.addAttribute("links", view.getLinks());
        model.addAttribute("contacts", view.getContacts());
        return "main/default";
    }

    @PostMapping("/main/default")
    @ResponseBody
    public JsonResponse<PageModel<LogDocumentView>> defaultData(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return JsonResponse.success("查询成功", mainService.recentDocumentPage(currentUser(), pageNum, pageSize));
    }

    @GetMapping("/main/about")
    public String about(Model model) {
        nav(model, "main");
        return "main/about";
    }

    @GetMapping("/main/search")
    public String search(@RequestParam(defaultValue = "") String keyword,
                         @RequestParam(name = "search_type", defaultValue = "title") String searchType,
                         Model model) {
        CurrentUser currentUser = currentUser();
        SearchView view = mainService.searchDocuments(currentUser, keyword, searchType);
        model.addAttribute("search_type", view.getSearchType());
        model.addAttribute("keyword", view.getKeyword());
        model.addAttribute("documents", view.getDocuments());
        model.addAttribute("count", view.getCount());
        return "main/search";
    }

    @GetMapping("/error/403")
    public String error403(Model model) {
        nav(model, "main");
        return "error/403";
    }
}
