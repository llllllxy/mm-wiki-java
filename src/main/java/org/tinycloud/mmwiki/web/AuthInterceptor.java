package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.GlobalConstant;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.service.LogService;
import org.tinycloud.mmwiki.service.UserService;
import org.tinycloud.mmwiki.util.WebUtils;

import java.util.Locale;
import java.util.Objects;


/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Autowired
    private RolePrivilegeMapper rolePrivilegeMapper;
    @Autowired
    private LogService logService;


    /**
     * 请求进入 Controller 前进行登录校验、账号状态刷新和后台权限校验。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器对象
     * @return true 表示继续执行 Controller，false 表示请求已被拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return this.handleUnauthenticated(request, response);
        }

        Object sessionValue = session.getAttribute(GlobalConstant.SESSION_AUTHOR);
        if (!(sessionValue instanceof CurrentUser)) {
            return this.handleUnauthenticated(request, response);
        }
        CurrentUser currentUser = (CurrentUser) sessionValue;
        // 判断是否需要重新读取账号状态，避免每个请求都查询用户表
        if (System.currentTimeMillis() - currentUser.getStatusRefreshTime() >= GlobalConstant.USER_STATUS_REFRESH_INTERVAL_MILLIS) {
            User refreshedUser = this.userService.findActiveById(currentUser.getUserId());
            if (refreshedUser == null || refreshedUser.getIsForbidden() == 1) {
                session.invalidate();
                return this.handleUnauthenticated(request, response);
            }
            currentUser = CurrentUser.from(refreshedUser);
            session.setAttribute(GlobalConstant.SESSION_AUTHOR, currentUser);
        }
        // 验证权限
        return this.checkSystemAccess(request, response, currentUser);
    }

    /**
     * 请求完成后异步记录后台系统操作日志。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器对象
     * @param ex       Controller 执行异常，正常完成时为空
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        HttpSession session = request.getSession(false);
        CurrentUser currentUser = session == null ? null : (CurrentUser) session.getAttribute(GlobalConstant.SESSION_AUTHOR);
        if (currentUser == null || !this.shouldRecordOperation(request)) {
            return;
        }
        this.logService.recordSystemOperationAsync(request, currentUser, ex);
    }

    /**
     * 处理未登录或登录失效请求。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @return false 表示请求已处理完毕，不再进入 Controller
     */
    private boolean handleUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (WebUtils.expectsJsonResponse(request)) {
            WebUtils.writeJson(response, JsonResponse.error(ErrorCodeEnum.UNAUTHORIZED, "未登录或登录已失效！", "/author/index"));
            return false;
        }
        response.sendRedirect("/author/index");
        return false;
    }

    /**
     * 校验后台管理系统菜单权限。
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param currentUser 当前登录用户
     * @return true 表示有权限继续执行，false 表示请求已被拦截
     */
    private boolean checkSystemAccess(HttpServletRequest request, HttpServletResponse response, CurrentUser currentUser) throws Exception {
        String path = request.getRequestURI();
        String[] parts = path == null ? new String[0] : path.replaceAll("^/+", "").split("/");
        if (parts.length < 3 || !"system".equalsIgnoreCase(parts[0])) {
            return true;
        }
        String controller = parts[1].toLowerCase(Locale.ROOT);
        String action = parts[2].toLowerCase(Locale.ROOT);
        if ("main".equals(controller) && ("index".equals(action) || "default".equals(action))) {
            return true;
        }
        if (currentUser.getRoleId() != null && Objects.equals(currentUser.getRoleId(), GlobalConstant.ROOT_ROLE_ID)) {
            return true;
        }
        if (this.rolePrivilegeMapper.countAuthorized(controller, action, currentUser.getRoleId()) > 0) {
            return true;
        }
        return this.handleForbidden(request, response);
    }

    /**
     * 处理无权限访问后台功能的请求。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @return false 表示请求已处理完毕，不再进入 Controller
     */
    private boolean handleForbidden(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (WebUtils.expectsJsonResponse(request)) {
            WebUtils.writeJson(response, JsonResponse.error(ErrorCodeEnum.FORBIDDEN, "抱歉，您没有权限操作！", "/error/403"));
            return false;
        }
        response.sendRedirect("/error/403");
        return false;
    }

    /**
     * 判断当前请求是否需要记录系统操作日志。
     *
     * @param request 当前请求
     * @return true 表示需要记录操作日志
     */
    private boolean shouldRecordOperation(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "POST".equalsIgnoreCase(request.getMethod()) && path != null && path.startsWith("/system/");
    }
}
