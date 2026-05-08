package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.EmailServer;
import org.tinycloud.mmwiki.service.EmailService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * 系统邮件服务器配置控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemEmailController extends ControllerSupport {

    @Autowired
    private EmailService emailService;

    /**
     * 邮件服务器列表页。
     */
    @GetMapping("/system/email/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("emails", emailService.list(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/email/list";
    }

    /**
     * 新增邮件服务器表单页。
     */
    @GetMapping("/system/email/add")
    public String add() {
        return "system/email/form";
    }

    /**
     * 修改邮件服务器表单页。
     */
    @GetMapping("/system/email/edit")
    public String edit(@RequestParam("email_id") Integer emailId, Model model) {
        EmailServer email = emailService.findById(emailId);
        if (email == null) {
            throw new IllegalStateException("邮件服务器不存在。");
        }
        model.addAttribute("email", email);
        return "system/email/form";
    }

    /**
     * 保存新增邮件服务器。
     */
    @PostMapping("/system/email/save")
    @ResponseBody
    public JsonResponse<Void> save(HttpServletRequest request) {
        return emailService.save(buildEmailServer(null, request));
    }

    /**
     * 保存邮件服务器修改。
     */
    @PostMapping("/system/email/modify")
    @ResponseBody
    public JsonResponse<Void> modify(HttpServletRequest request) {
        return emailService.update(buildEmailServer(integerParam(request, "email_id", "emailId"), request));
    }

    /**
     * 启用邮件服务器。
     */
    @PostMapping("/system/email/used")
    @ResponseBody
    public JsonResponse<Void> used(@RequestParam("email_id") Integer emailId) {
        return emailService.markUsed(emailId);
    }

    /**
     * 删除邮件服务器。
     */
    @PostMapping("/system/email/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("email_id") Integer emailId) {
        return emailService.delete(emailId);
    }

    /**
     * 使用表单配置发送测试邮件。
     */
    @PostMapping("/system/email/test")
    @ResponseBody
    public JsonResponse<Void> test(HttpServletRequest request) {
        return emailService.testSend(buildEmailServer(integerParam(request, "email_id", "emailId"), request), param(request, "emails"));
    }

    private EmailServer buildEmailServer(Integer emailId, HttpServletRequest request) {
        EmailServer emailServer = new EmailServer();
        emailServer.setEmailId(emailId);
        emailServer.setName(param(request, "name"));
        emailServer.setSenderAddress(param(request, "sender_address", "senderAddress"));
        emailServer.setSenderName(param(request, "sender_name", "senderName"));
        emailServer.setSenderTitlePrefix(param(request, "sender_title_prefix", "senderTitlePrefix"));
        emailServer.setHost(param(request, "host"));
        emailServer.setPort(integerParam(request, "port"));
        emailServer.setUsername(param(request, "username"));
        emailServer.setPassword(param(request, "password", "mailPassword"));
        emailServer.setIsSsl(integerParam(request, "is_ssl", "isSsl"));
        return emailServer;
    }

    private String param(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getParameter(name);
            if (value != null) {
                return value;
            }
        }
        return "";
    }

    private Integer integerParam(HttpServletRequest request, String... names) {
        String value = param(request, names);
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
