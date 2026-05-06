package org.tinycloud.mmwiki.controller;

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

@Controller
public class SystemEmailController extends ControllerSupport {

    private final EmailService emailService;

    public SystemEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/system/email/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("emails", emailService.list(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/email/list";
    }

    @GetMapping("/system/email/add")
    public String add() {
        return "system/email/form";
    }

    @GetMapping("/system/email/edit")
    public String edit(@RequestParam("email_id") Integer emailId, Model model) {
        EmailServer email = emailService.findById(emailId);
        if (email == null) {
            throw new IllegalStateException("邮件服务器不存在。");
        }
        model.addAttribute("email", email);
        return "system/email/form";
    }

    @PostMapping("/system/email/save")
    @ResponseBody
    public JsonResponse<Void> save(
        @RequestParam("name") String name,
        @RequestParam("senderAddress") String senderAddress,
        @RequestParam(value = "senderName", defaultValue = "") String senderName,
        @RequestParam(value = "senderTitlePrefix", defaultValue = "") String senderTitlePrefix,
        @RequestParam("host") String host,
        @RequestParam("port") Integer port,
        @RequestParam("username") String username,
        @RequestParam("mailPassword") String mailPassword,
        @RequestParam(value = "isSsl", defaultValue = "0") Integer isSsl
    ) {
        return emailService.save(buildEmailServer(null, name, senderAddress, senderName, senderTitlePrefix, host, port, username, mailPassword, isSsl));
    }

    @PostMapping("/system/email/modify")
    @ResponseBody
    public JsonResponse<Void> modify(
        @RequestParam("emailId") Integer emailId,
        @RequestParam("name") String name,
        @RequestParam("senderAddress") String senderAddress,
        @RequestParam(value = "senderName", defaultValue = "") String senderName,
        @RequestParam(value = "senderTitlePrefix", defaultValue = "") String senderTitlePrefix,
        @RequestParam("host") String host,
        @RequestParam("port") Integer port,
        @RequestParam("username") String username,
        @RequestParam("mailPassword") String mailPassword,
        @RequestParam(value = "isSsl", defaultValue = "0") Integer isSsl
    ) {
        return emailService.update(buildEmailServer(emailId, name, senderAddress, senderName, senderTitlePrefix, host, port, username, mailPassword, isSsl));
    }

    @PostMapping("/system/email/used")
    @ResponseBody
    public JsonResponse<Void> used(@RequestParam("email_id") Integer emailId) {
        return emailService.markUsed(emailId);
    }

    @PostMapping("/system/email/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("email_id") Integer emailId) {
        return emailService.delete(emailId);
    }

    @PostMapping("/system/email/test")
    @ResponseBody
    public JsonResponse<Void> test(
        @RequestParam("name") String name,
        @RequestParam("senderAddress") String senderAddress,
        @RequestParam(value = "senderName", defaultValue = "") String senderName,
        @RequestParam(value = "senderTitlePrefix", defaultValue = "") String senderTitlePrefix,
        @RequestParam("host") String host,
        @RequestParam("port") Integer port,
        @RequestParam("username") String username,
        @RequestParam("mailPassword") String mailPassword,
        @RequestParam(value = "isSsl", defaultValue = "0") Integer isSsl,
        @RequestParam(value = "emails", defaultValue = "") String emails
    ) {
        return emailService.testSend(buildEmailServer(null, name, senderAddress, senderName, senderTitlePrefix, host, port, username, mailPassword, isSsl), emails);
    }

    private EmailServer buildEmailServer(
        Integer emailId,
        String name,
        String senderAddress,
        String senderName,
        String senderTitlePrefix,
        String host,
        Integer port,
        String username,
        String mailPassword,
        Integer isSsl
    ) {
        EmailServer emailServer = new EmailServer();
        emailServer.setEmailId(emailId);
        emailServer.setName(name);
        emailServer.setSenderAddress(senderAddress);
        emailServer.setSenderName(senderName);
        emailServer.setSenderTitlePrefix(senderTitlePrefix);
        emailServer.setHost(host);
        emailServer.setPort(port);
        emailServer.setUsername(username);
        emailServer.setPassword(mailPassword);
        emailServer.setIsSsl(isSsl);
        return emailServer;
    }
}
