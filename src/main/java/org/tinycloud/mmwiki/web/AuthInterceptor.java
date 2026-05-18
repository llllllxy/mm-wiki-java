package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.GlobalConstant;
import org.tinycloud.mmwiki.domain.Privilege;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.PrivilegeMapper;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.service.LogService;
import org.tinycloud.mmwiki.service.UserService;
import org.tinycloud.mmwiki.util.JsonUtils;
import org.tinycloud.mmwiki.util.RequestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_AUTHOR = "author";
    /**
     * session 中账号状态的最短刷新间隔，避免高频请求每次都查询用户表。目前默认是30秒
     */
    private static final long USER_STATUS_REFRESH_INTERVAL_MILLIS = 30_000L;

    @Autowired
    private UserService userService;
    @Autowired
    private PrivilegeMapper privilegeMapper;
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
            return handleUnauthenticated(request, response);
        }

        Object sessionValue = session.getAttribute(SESSION_AUTHOR);
        if (!(sessionValue instanceof CurrentUser)) {
            return handleUnauthenticated(request, response);
        }
        CurrentUser currentUser = (CurrentUser) sessionValue;

        if (shouldRefreshUserStatus(currentUser)) {
            User refreshedUser = userService.findActiveById(currentUser.getUserId());
            if (refreshedUser == null || refreshedUser.getIsForbidden() == 1) {
                session.invalidate();
                return handleUnauthenticated(request, response);
            }
            currentUser = CurrentUser.from(refreshedUser);
            session.setAttribute(SESSION_AUTHOR, currentUser);
        }

        return checkSystemAccess(request, response, currentUser);
    }

    /**
     * 判断是否需要重新读取账号状态，避免每个请求都查询用户表。
     *
     * @param currentUser 当前 session 中的登录用户
     * @return true 表示需要刷新账号状态
     */
    private boolean shouldRefreshUserStatus(CurrentUser currentUser) {
        return System.currentTimeMillis() - currentUser.getStatusRefreshTime() >= USER_STATUS_REFRESH_INTERVAL_MILLIS;
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
        CurrentUser currentUser = session == null ? null : (CurrentUser) session.getAttribute(SESSION_AUTHOR);
        if (currentUser == null || !shouldRecordOperation(request)) {
            return;
        }
        logService.recordSystemOperationAsync(request, currentUser, ex);
    }

    /**
     * 处理未登录或登录失效请求。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @return false 表示请求已处理完毕，不再进入 Controller
     */
    private boolean handleUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (RequestUtils.expectsJsonResponse(request)) {
            return writeJsonError(response, JsonResponse.error(ErrorCodeEnum.UNAUTHORIZED, "未登录或登录已失效！", "/author/index"));
        }
        response.sendRedirect("/author/index");
        return false;
    }

    /**
     * 校验后台系统菜单权限。
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
        if (currentUser.getRoleId() != null && currentUser.getRoleId() == GlobalConstant.ROOT_ROLE_ID) {
            return true;
        }
        Privilege privilege = privilegeMapper.findControllerPrivilege(controller, action);
        if (privilege == null) {
            return true;
        }
        Set<Integer> allowed = new HashSet<>(rolePrivilegeMapper.findPrivilegeIdsByRoleId(currentUser.getRoleId()));
        if (allowed.contains(privilege.getPrivilegeId())) {
            return true;
        }
        return handleForbidden(request, response);
    }

    /**
     * 处理无权限访问后台功能的请求。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @return false 表示请求已处理完毕，不再进入 Controller
     */
    private boolean handleForbidden(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (RequestUtils.expectsJsonResponse(request)) {
            return writeJsonError(response, JsonResponse.error(ErrorCodeEnum.FORBIDDEN, "抱歉，您没有权限操作！", "/error/403"));
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

    /**
     * 响应 JSON 错误。
     *
     * @param response 当前响应
     * @param body     错误信息
     * @return false
     * @throws IOException 响应 IO 异常
     */
    private boolean writeJsonError(HttpServletResponse response, JsonResponse<?> body) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonUtils.writeValueAsString(body));
        return false;
    }
}
