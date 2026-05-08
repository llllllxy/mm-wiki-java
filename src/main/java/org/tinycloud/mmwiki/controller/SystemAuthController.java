package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.service.LoginAuthService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemAuthController extends ControllerSupport {

    @Autowired
    private LoginAuthService loginAuthService;

    @GetMapping("/system/auth/list")
    public String list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        @RequestParam(defaultValue = "") String keyword,
        Model model
    ) {
        LoginAuthService.AuthPage view = loginAuthService.list(keyword, page, number);
        model.addAttribute("auths", view.auths());
        model.addAttribute("keyword", view.keyword());
        model.addAttribute("paginator", view.paginator());
        return "system/auth/list";
    }

    @GetMapping("/system/auth/add")
    public String add() {
        return "system/auth/form";
    }

    @GetMapping("/system/auth/edit")
    public String edit(@RequestParam("login_auth_id") Integer loginAuthId, Model model) {
        LoginAuth auth = loginAuthService.findById(loginAuthId);
        if (auth == null) {
            throw new IllegalStateException("登录认证不存在。");
        }
        model.addAttribute("auth", auth);
        return "system/auth/form";
    }

    @PostMapping("/system/auth/save")
    @ResponseBody
    public JsonResponse<Void> save(LoginAuth loginAuth) {
        return loginAuthService.save(loginAuth);
    }

    @PostMapping("/system/auth/modify")
    @ResponseBody
    public JsonResponse<Void> modify(LoginAuth loginAuth) {
        return loginAuthService.update(loginAuth);
    }

    @PostMapping("/system/auth/used")
    @ResponseBody
    public JsonResponse<Void> used(@RequestParam("login_auth_id") Integer loginAuthId) {
        return loginAuthService.markUsed(loginAuthId);
    }

    @PostMapping("/system/auth/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("login_auth_id") Integer loginAuthId) {
        return loginAuthService.delete(loginAuthId);
    }

    @GetMapping("/system/auth/doc")
    public String doc() {
        return "system/auth/doc";
    }
}
