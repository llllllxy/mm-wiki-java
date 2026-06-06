package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.service.LoginAuthService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * 后台登录认证控制器，负责统一认证配置的列表、表单、启用和删除。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/auth")
public class SystemAuthController extends ControllerSupport {

    @Autowired
    private LoginAuthService loginAuthService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/auth/list";
    }

    @PostMapping("/list")
    @ResponseBody
    public JsonResponse<PageModel<LoginAuth>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                       @RequestParam(defaultValue = "") String keyword) {
        return JsonResponse.success("查询成功", loginAuthService.pageModel(keyword, pageNum, pageSize));
    }

    @GetMapping("/add")
    public String add() {
        return "system/auth/form";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("login_auth_id") Integer loginAuthId, Model model) {
        LoginAuth auth = loginAuthService.findById(loginAuthId);
        if (auth == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "登录认证不存在。");
        }
        model.addAttribute("auth", auth);
        return "system/auth/form";
    }

    @PostMapping("/save")
    @ResponseBody
    public JsonResponse<Void> save(LoginAuth loginAuth) {
        return loginAuthService.save(loginAuth);
    }

    @PostMapping("/modify")
    @ResponseBody
    public JsonResponse<Void> modify(LoginAuth loginAuth) {
        return loginAuthService.update(loginAuth);
    }

    @PostMapping("/used")
    @ResponseBody
    public JsonResponse<Void> used(@RequestParam("login_auth_id") Integer loginAuthId) {
        return loginAuthService.markUsed(loginAuthId);
    }

    @PostMapping("/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("login_auth_id") Integer loginAuthId) {
        return loginAuthService.delete(loginAuthId);
    }

    @GetMapping("/doc")
    public String doc() {
        return "system/auth/doc";
    }
}
