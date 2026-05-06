package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.service.MainService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class MainController extends ControllerSupport {

    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @GetMapping({"/", "/main/index"})
    public String index(HttpServletRequest request, Model model) {
        nav(model, "main");
        CurrentUser currentUser = currentUser(request);
        List<Document> documents = mainService.loadCollectedDocuments(currentUser.getUserId());
        model.addAttribute("documents", documents);
        model.addAttribute("count", documents.size());
        return "main/index";
    }

    @GetMapping("/main/default")
    public String defaultPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int number,
        HttpServletRequest request,
        Model model
    ) {
        nav(model, "main");
        CurrentUser currentUser = currentUser(request);
        MainService.MainDefaultView view = mainService.loadDefaultView(currentUser.getUserId(), page, number);
        model.addAttribute("panel_title", view.panelTitle());
        model.addAttribute("panel_description", view.panelDescription());
        model.addAttribute("logDocuments", view.logDocuments());
        model.addAttribute("links", view.links());
        model.addAttribute("contacts", view.contacts());
        model.addAttribute("paginator", view.paginator());
        return "main/default";
    }

    @GetMapping("/main/about")
    public String about(Model model) {
        nav(model, "main");
        return "main/about";
    }

    @GetMapping("/main/search")
    public String search(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(name = "search_type", defaultValue = "title") String searchType,
        HttpServletRequest request,
        Model model
    ) {
        CurrentUser currentUser = currentUser(request);
        MainService.SearchView view = mainService.searchDocuments(currentUser.getUserId(), keyword, searchType);
        model.addAttribute("search_type", view.searchType());
        model.addAttribute("keyword", view.keyword());
        model.addAttribute("documents", view.documents());
        model.addAttribute("count", view.count());
        return "main/search";
    }
}
