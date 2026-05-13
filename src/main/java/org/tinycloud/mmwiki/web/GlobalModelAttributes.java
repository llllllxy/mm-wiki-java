package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.service.ConfigService;
import org.tinycloud.mmwiki.service.InstallService;
import org.tinycloud.mmwiki.util.RequestUtils;

/**
 * MM-Wiki Web 全局模型属性与异常处理组件。
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
     * 全局业务异常处理。
     * <p>
     * Ajax、表单、表格等异步请求继续返回 JSON；普通页面请求返回错误页，避免视图页面直接显示 JSON。
     *
     * @param e       运行时异常
     * @param request 当前请求
     * @return JSON 响应或错误视图
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = resolveStatus(e);
        String message = resolveMessage(e, status);
        if (status.is5xxServerError()) {
            log.error("handleRuntimeException: ", e);
        } else {
            log.warn("handleRuntimeException: {}", message, e);
        }

        if (RequestUtils.expectsJsonResponse(request)) {
            if (e instanceof SystemException) {
                return JsonResponse.error(message, ((SystemException) e).getUrl());
            } else {
                return JsonResponse.error(message);
            }
        }

        ModelAndView view = new ModelAndView("error/default");
        view.setStatus(status);
        view.addObject("status", status.value());
        view.addObject("message", message);
        view.addObject("homeUrl", "/");
        return view;
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

    /**
     * 根据业务异常文案推断 HTTP 状态码。
     * <p>
     * 后续如果引入 ForbiddenException、NotFoundException 等自定义异常，这里可以直接按异常类型判断。
     *
     * @param e 运行时异常
     * @return HTTP 状态码
     */
    private HttpStatus resolveStatus(RuntimeException e) {
        if (e instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        String message = e.getMessage();
        if (message == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (message.contains("没有权限") || message.contains("不允许")) {
            return HttpStatus.FORBIDDEN;
        }
        if (message.contains("不存在") || message.contains("未找到")) {
            return HttpStatus.NOT_FOUND;
        }
        if (e instanceof IllegalStateException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 获取对用户展示的异常文案。
     *
     * @param e      运行时异常
     * @param status HTTP 状态码
     * @return 用户可读的错误文案
     */
    private String resolveMessage(RuntimeException e, HttpStatus status) {
        String message = e.getMessage();
        if (message != null && !message.isBlank() && !status.is5xxServerError()) {
            return message;
        }
        if (status == HttpStatus.FORBIDDEN) {
            return "您没有权限访问该页面。";
        }
        if (status == HttpStatus.NOT_FOUND) {
            return "页面不存在或已经被移动。";
        }
        if (status == HttpStatus.BAD_REQUEST) {
            return "请求参数错误。";
        }
        return "服务器处理请求时出现异常，请稍后重试。";
    }

}
