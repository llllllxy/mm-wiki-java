package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.service.ConfigService;
import org.tinycloud.mmwiki.service.InstallService;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@ControllerAdvice
public class GlobalModelAttributes {
    private static final Logger log = LoggerFactory.getLogger(GlobalModelAttributes.class);

    private final MmwikiProperties properties;
    private final ConfigService configService;
    private final InstallService installService;

    public GlobalModelAttributes(MmwikiProperties properties, ConfigService configService, InstallService installService) {
        this.properties = properties;
        this.configService = configService;
        this.installService = installService;
    }

    /**
     * 全局异常捕获
     *
     * @param e BindException
     */
    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    public JsonResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("handleBindException: ", e);
        return JsonResponse.error(e.getMessage());
    }


    @ModelAttribute
    public void populate(Model model, HttpServletRequest request) {
        model.addAttribute("version", properties.getVersion());
        model.addAttribute("copyright", properties.getCopyright());
        if (!installService.installed() || (request.getRequestURI() != null && request.getRequestURI().startsWith("/install"))) {
            model.addAttribute("system_name", properties.getSystemNameFallback());
            return;
        }
        model.addAttribute("system_name", configService.getValue("system_name", properties.getSystemNameFallback()));

        HttpSession session = request.getSession(false);
        CurrentUser currentUser = session == null ? null : (CurrentUser) session.getAttribute(AuthInterceptor.SESSION_AUTHOR);
        if (currentUser != null) {
            model.addAttribute("login_user_id", currentUser.getUserId());
            model.addAttribute("login_username", currentUser.getUsername());
            model.addAttribute("login_role_id", currentUser.getRoleId());
        }
    }
}
