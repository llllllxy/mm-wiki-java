package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tinycloud.mmwiki.service.SystemService;
import org.tinycloud.mmwiki.web.ControllerSupport;

/**
 * 后台首页控制器，负责系统后台入口和个人中心默认跳转。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/main")
public class SystemMainController extends ControllerSupport {

    @Autowired
    private SystemService systemService;

    @GetMapping("/index")
    public String index(Model model) {
        nav(model, "system");
        model.addAttribute("menuGroups", systemService.loadMenuGroups(currentUser()));
        return "system/main/index";
    }

    @GetMapping("/default")
    public String defaultPage() {
        return "redirect:/system/profile/info";
    }
}
