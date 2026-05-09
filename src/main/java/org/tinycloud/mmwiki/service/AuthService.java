package org.tinycloud.mmwiki.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.util.IpUtils;
import org.tinycloud.mmwiki.web.AuthInterceptor;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.service.UnifiedAuthService.AuthLoginProfile;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private LoginAuthService loginAuthService;
    @Autowired
    private UnifiedAuthService unifiedAuthService;

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
     * 校验统一登录账号密码，同步本地用户并写入登录会话。
     */
    public JsonResponse<Void> authLogin(String username, String password,
                                        HttpServletRequest request, HttpServletResponse response) {
        if (!isSsoOpen()) {
            return JsonResponse.error("系统未开启统一登录功能！");
        }
        LoginAuth loginAuth = loginAuthService.findUsed();
        if (loginAuth == null) {
            return JsonResponse.error("统一登录认证配置不可用！");
        }

        String cleanUsername = username == null ? "" : username.trim();
        String cleanPassword = password == null ? "" : password.trim();
        if (!StringUtils.hasText(cleanUsername)) {
            return JsonResponse.error("统一登录用户名不能为空！");
        }
        if (!StringUtils.hasText(cleanPassword)) {
            return JsonResponse.error("统一登录密码不能为空！");
        }

        AuthLoginProfile profile;
        try {
            profile = unifiedAuthService.authenticate(loginAuth, cleanUsername, cleanPassword);
        } catch (Exception ex) {
            log.error("统一登录失败：{}", ex.getMessage(), ex);
            return JsonResponse.error("统一登录失败！");
        }
        if (profile == null) {
            return JsonResponse.error("统一登录失败！");
        }

        int now = Math.toIntExact(Instant.now().getEpochSecond());
        String realUsername = loginAuth.getUsernamePrefix() + "_" + cleanUsername;
        User user = new User();
        user.setUsername(realUsername);
        user.setGivenName(value(profile.getGivenName()));
        user.setPassword(userService.encodePassword(cleanPassword));
        user.setEmail(value(profile.getEmail()));
        user.setMobile(value(profile.getMobile()));
        user.setPhone(value(profile.getPhone()));
        user.setDepartment(value(profile.getDepartment()));
        user.setPosition(value(profile.getPosition()));
        user.setLocation(value(profile.getLocation()));
        user.setIm(value(profile.getIm()));
        user.setLastIp(IpUtils.getClientIp(request));
        user.setLastTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);

        User refreshed = userService.saveOrUpdateAuthUser(user);
        if (refreshed == null) {
            return JsonResponse.error("登录失败!");
        }
        CurrentUser currentUser = CurrentUser.from(refreshed);
        request.getSession().setAttribute(AuthInterceptor.SESSION_AUTHOR, currentUser);
        return JsonResponse.success("登录成功！", "/main/index");
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

    private String value(String value) {
        return value == null ? "" : value;
    }
}
