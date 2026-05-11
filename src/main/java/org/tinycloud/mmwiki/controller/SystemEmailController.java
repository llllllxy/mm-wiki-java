package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.tinycloud.mmwiki.web.PageModel;

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
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/email/list";
    }

    /**
     * 邮件服务器列表数据。
     */
    @PostMapping("/system/email/list")
    @ResponseBody
    public JsonResponse<PageModel<EmailServer>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "20") int pageSize,
                                                         @RequestParam(defaultValue = "") String keyword) {
        return JsonResponse.success("查询成功", emailService.pageModel(keyword, pageNum, pageSize));
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
    public String edit(@RequestParam("emailId") Integer emailId, Model model) {
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
    public JsonResponse<Void> save(EmailServer emailServer) {
        return emailService.save(emailServer);
    }

    /**
     * 保存邮件服务器修改。
     */
    @PostMapping("/system/email/modify")
    @ResponseBody
    public JsonResponse<Void> modify(EmailServer emailServer) {
        return emailService.update(emailServer);
    }

    /**
     * 启用邮件服务器。
     */
    @PostMapping("/system/email/used")
    @ResponseBody
    public JsonResponse<Void> used(@RequestParam("emailId") Integer emailId) {
        return emailService.markUsed(emailId);
    }

    /**
     * 删除邮件服务器。
     */
    @PostMapping("/system/email/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("emailId") Integer emailId) {
        return emailService.delete(emailId);
    }

    /**
     * 使用表单配置发送测试邮件。
     */
    @PostMapping("/system/email/test")
    @ResponseBody
    public JsonResponse<Void> test(EmailServer emailServer, @RequestParam(defaultValue = "") String emails) {
        return emailService.testSend(emailServer, emails);
    }
}
