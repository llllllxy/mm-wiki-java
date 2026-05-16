package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
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

    @Autowired
    private MmwikiProperties properties;
    @Autowired
    private ConfigService configService;
    @Autowired
    private InstallService installService;


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
        String message = e.getMessage();
        int code = e instanceof SystemException
                ? ((SystemException) e).getErrorCode().getCode()
                : ErrorCodeEnum.INTERNAL_ERROR.getCode();
        String redirectUrl = e instanceof SystemException
                ? ((SystemException) e).getUrl()
                : null;
        log.warn("handleRuntimeException code: {}, message: {}", code, message, e);

        if (RequestUtils.expectsJsonResponse(request)) {
            return ResponseEntity.ok(JsonResponse.error(code, message, redirectUrl));
        }

        ModelAndView view = new ModelAndView("error/default");
        view.setStatus(HttpStatus.OK);
        view.addObject("status", code);
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

}
