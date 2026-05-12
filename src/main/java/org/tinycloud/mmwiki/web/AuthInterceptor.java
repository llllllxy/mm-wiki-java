package org.tinycloud.mmwiki.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.domain.Privilege;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.LogMapper;
import org.tinycloud.mmwiki.mapper.PrivilegeMapper;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.service.RoleService;
import org.tinycloud.mmwiki.service.UserService;
import org.tinycloud.mmwiki.util.IpUtils;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_AUTHOR = "author";

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final PrivilegeMapper privilegeMapper;
    private final RolePrivilegeMapper rolePrivilegeMapper;
    private final LogMapper logMapper;

    public AuthInterceptor(
            UserService userService,
            ObjectMapper objectMapper,
            PrivilegeMapper privilegeMapper,
            RolePrivilegeMapper rolePrivilegeMapper,
            LogMapper logMapper
    ) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.privilegeMapper = privilegeMapper;
        this.rolePrivilegeMapper = rolePrivilegeMapper;
        this.logMapper = logMapper;
    }

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

        User refreshedUser = userService.findActiveById(currentUser.getUserId());
        if (refreshedUser == null || refreshedUser.getIsForbidden() == 1) {
            session.invalidate();
            return handleUnauthenticated(request, response);
        }

        CurrentUser refreshed = CurrentUser.from(refreshedUser);
        session.setAttribute(SESSION_AUTHOR, refreshed);
        return checkSystemAccess(request, response, refreshed);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        HttpSession session = request.getSession(false);
        CurrentUser currentUser = session == null ? null : (CurrentUser) session.getAttribute(SESSION_AUTHOR);
        if (currentUser == null || !shouldRecordOperation(request)) {
            return;
        }
        try {
            LogEntry logEntry = new LogEntry();
            logEntry.setLevel(ex == null ? 6 : 3);
            logEntry.setPath(left(request.getRequestURI(), 100));
            logEntry.setGet(left(request.getQueryString() == null ? "" : request.getQueryString(), 4096));
            logEntry.setPost(left(postParameters(request), 4096));
            logEntry.setMessage(left((ex == null ? "系统操作" : "系统操作异常") + ": " + request.getMethod() + " " + request.getRequestURI(), 255));
            logEntry.setIp(left(IpUtils.getClientIp(request), 100));
            logEntry.setUserAgent(left(header(request, "User-Agent"), 255));
            logEntry.setReferer(left(header(request, "Referer"), 100));
            logEntry.setUserId(currentUser.getUserId());
            logEntry.setUsername(currentUser.getUsername());
            logEntry.setCreateTime(LocalDateTime.now());
            logMapper.insert(logEntry);
        } catch (RuntimeException ignored) {
            // Logging must never break the user-facing request path.
        }
    }

    private boolean handleUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isAjax(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(
                    response.getWriter(),
                    JsonResponse.error("未登录或登录已失效！", "/author/index")
            );
            return false;
        }

        response.sendRedirect("/author/index");
        return false;
    }

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
        if (currentUser.getRoleId() != null && currentUser.getRoleId() == RoleService.ROOT_ROLE_ID) {
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

    private boolean handleForbidden(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isAjax(request) || "POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(
                    response.getWriter(),
                    JsonResponse.error("抱歉，您没有权限操作！", "/system/main/index")
            );
            return false;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有权限访问该页面！");
        return false;
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }

    private boolean shouldRecordOperation(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "POST".equalsIgnoreCase(request.getMethod()) && path != null && path.startsWith("/system/");
    }

    private String postParameters(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(entry.getKey()).append('=');
            if (entry.getKey().toLowerCase(Locale.ROOT).contains("password")
                    || entry.getKey().toLowerCase(Locale.ROOT).contains("pwd")) {
                builder.append("***");
            } else {
                builder.append(String.join(",", entry.getValue()));
            }
        }
        return builder.toString();
    }

    private String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? "" : value;
    }

    private String left(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
