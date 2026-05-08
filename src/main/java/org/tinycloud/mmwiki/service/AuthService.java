package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.util.IpUtils;
import org.tinycloud.mmwiki.web.AuthInterceptor;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class AuthService {

    @Autowired
    private UserService userService;
    @Autowired
    private ConfigService configService;

    /**
     * 判断系统是否开启统一登录。
     */
    public boolean isSsoOpen() {
        return "1".equals(configService.getValue("sso_open", "0"));
    }

    /**
     * 校验本地账号密码并写入登录会话。
     */
    public JsonResponse<Void> login(String username, String password,
                                    HttpServletRequest request, HttpServletResponse response) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanPassword = password == null ? "" : password.trim();

        if (!StringUtils.hasText(cleanUsername)) {
            return JsonResponse.error("系统用户名不能为空！");
        }
        if (cleanUsername.contains("_")) {
            return JsonResponse.error("系统用户名不合法！");
        }
        if (!StringUtils.hasText(cleanPassword)) {
            return JsonResponse.error("密码不能为空！");
        }

        User user = userService.findActiveByUsername(cleanUsername);
        if (user == null || user.getIsForbidden() == 1) {
            return JsonResponse.error("用户名或密码错误!");
        }

        String encodedPassword = userService.encodePassword(cleanPassword);
        if (!encodedPassword.equals(user.getPassword())) {
            return JsonResponse.error("用户名或密码错误!");
        }

        int now = Math.toIntExact(Instant.now().getEpochSecond());
        userService.updateLoginSuccess(user.getUserId(), IpUtils.getClientIp(request), now);

        User refreshed = userService.findActiveById(user.getUserId());
        CurrentUser currentUser = CurrentUser.from(refreshed);
        request.getSession().setAttribute(AuthInterceptor.SESSION_AUTHOR, currentUser);
        return JsonResponse.success("登录成功！", "/main/index");
    }

    /**
     * 返回统一登录暂未实现的兼容提示。
     */
    public JsonResponse<Void> authLoginStub() {
        return JsonResponse.error("统一登录功能尚未迁移完成。");
    }

    /**
     * 清理服务端会话，完成本地退出登录。
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
