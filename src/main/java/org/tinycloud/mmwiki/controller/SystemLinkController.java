package org.tinycloud.mmwiki.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Link;
import org.tinycloud.mmwiki.service.LinkService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemLinkController extends ControllerSupport {

    private final LinkService linkService;

    public SystemLinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/system/link/list")
    public String list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        @RequestParam(defaultValue = "") String keyword,
        Model model
    ) {
        LinkService.LinkPage view = linkService.list(keyword, page, number);
        model.addAttribute("links", view.links());
        model.addAttribute("keyword", view.keyword());
        model.addAttribute("paginator", view.paginator());
        return "system/link/list";
    }

    @GetMapping("/system/link/add")
    public String add() {
        return "system/link/form";
    }

    @GetMapping("/system/link/edit")
    public String edit(@RequestParam("link_id") Integer linkId, Model model) {
        Link link = linkService.findById(linkId);
        if (link == null) {
            throw new IllegalStateException("链接不存在。");
        }
        model.addAttribute("link", link);
        return "system/link/form";
    }

    @PostMapping("/system/link/save")
    @ResponseBody
    public JsonResponse<Void> save(Link link) {
        return linkService.save(link);
    }

    @PostMapping("/system/link/modify")
    @ResponseBody
    public JsonResponse<Void> modify(Link link) {
        return linkService.update(link);
    }

    @PostMapping("/system/link/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("link_id") Integer linkId) {
        return linkService.delete(linkId);
    }
}
