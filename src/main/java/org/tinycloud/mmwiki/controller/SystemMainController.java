package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.tinycloud.mmwiki.service.SystemService;
import org.tinycloud.mmwiki.web.ControllerSupport;

@Controller
public class SystemMainController extends ControllerSupport {

    private final SystemService systemService;

    public SystemMainController(SystemService systemService) {
        this.systemService = systemService;
    }

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
