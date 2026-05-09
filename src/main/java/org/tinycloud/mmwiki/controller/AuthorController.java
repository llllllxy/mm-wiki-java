package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.tinycloud.mmwiki.service.AuthService;
import org.tinycloud.mmwiki.service.InstallService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/author")
public class AuthorController extends ControllerSupport {

    @Autowired
    private AuthService authService;
    @Autowired
    private InstallService installService;

    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        if (!installService.installed()) {
            return "redirect:/install/index";
        }
        model.addAttribute("sso_open", authService.isSsoOpen() ? "1" : "0");
        return "author/index";
    }

    /**
     * 普通登录
     */
    @PostMapping("/login")
    @ResponseBody
    public JsonResponse<Void> login(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        return authService.login(username, password, request, response);
    }

    /**
     * 统一登录
     */
    @PostMapping("/authLogin")
    @ResponseBody
    public JsonResponse<Void> authLogin(@RequestParam String username,
                                        @RequestParam String password,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        return authService.authLogin(username, password, request, response);
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        authService.logout(request, response);
        return "redirect:/author/index";
    }
}
