package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.tinycloud.mmwiki.service.SystemService;
import org.tinycloud.mmwiki.web.ControllerSupport;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemMainController extends ControllerSupport {

    @Autowired
    private SystemService systemService;

    @GetMapping("/system/main/index")
    public String index(HttpServletRequest request, Model model) {
        nav(model, "system");
        model.addAttribute("menuGroups", systemService.loadMenuGroups(currentUser(request)));
        return "system/main/index";
    }

    @GetMapping("/system/main/default")
    public String defaultPage() {
        return "redirect:/system/profile/info";
    }
}
