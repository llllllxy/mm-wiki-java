package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.MainDefaultView;
import org.tinycloud.mmwiki.vo.SearchView;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MainService mainService;

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
        MainDefaultView view = mainService.loadDefaultView(currentUser.getUserId(), page, number);
        model.addAttribute("panel_title", view.getPanelTitle());
        model.addAttribute("panel_description", view.getPanelDescription());
        model.addAttribute("logDocuments", view.getLogDocuments());
        model.addAttribute("links", view.getLinks());
        model.addAttribute("contacts", view.getContacts());
        model.addAttribute("paginator", view.getPaginator());
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
        SearchView view = mainService.searchDocuments(currentUser.getUserId(), keyword, searchType);
        model.addAttribute("search_type", view.getSearchType());
        model.addAttribute("keyword", view.getKeyword());
        model.addAttribute("documents", view.getDocuments());
        model.addAttribute("count", view.getCount());
        return "main/search";
    }
}
