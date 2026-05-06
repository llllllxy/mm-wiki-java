package org.tinycloud.mmwiki.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.SystemConfigService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

@Controller
public class SystemConfigController extends ControllerSupport {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/system/config/global")
    public String global(Model model) {
        model.addAttribute("configValue", systemConfigService.loadConfigMap());
        return "system/config/form";
    }

    @PostMapping("/system/config/modify")
    @ResponseBody
    public JsonResponse<Void> modify(
        @RequestParam(value = "main_title", defaultValue = "") String mainTitle,
        @RequestParam(value = "main_description", defaultValue = "") String mainDescription,
        @RequestParam(value = "auto_follow_doc_open", defaultValue = "0") String autoFollowDocOpen,
        @RequestParam(value = "send_email_open", defaultValue = "0") String sendEmailOpen,
        @RequestParam(value = "sso_open", defaultValue = "0") String ssoOpen,
        @RequestParam(value = "fulltext_search_open", defaultValue = "0") String fulltextSearchOpen,
        @RequestParam(value = "doc_search_timer", defaultValue = "3600") String docSearchTimer,
        @RequestParam(value = "system_name", defaultValue = "") String systemName
    ) {
        return systemConfigService.updateGlobal(
            mainTitle,
            mainDescription,
            autoFollowDocOpen,
            sendEmailOpen,
            ssoOpen,
            fulltextSearchOpen,
            docSearchTimer,
            systemName
        );
    }
}
