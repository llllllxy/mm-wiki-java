package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.service.ConfigService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final MmwikiProperties properties;
    private final ConfigService configService;

    public GlobalModelAttributes(MmwikiProperties properties, ConfigService configService) {
        this.properties = properties;
        this.configService = configService;
    }

    @ModelAttribute
    public void populate(Model model, HttpServletRequest request) {
        model.addAttribute("version", properties.getVersion());
        model.addAttribute("copyright", properties.getCopyright());
        model.addAttribute(
            "system_name",
            configService.getValue("system_name", properties.getSystemNameFallback())
        );

        CurrentUser currentUser = (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTR);
        if (currentUser != null) {
            model.addAttribute("login_user_id", currentUser.getUserId());
            model.addAttribute("login_username", currentUser.getUsername());
            model.addAttribute("login_role_id", currentUser.getRoleId());
        }
    }
}
