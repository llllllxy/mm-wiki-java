package org.tinycloud.mmwiki.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.util.HashUtils;
import org.tinycloud.mmwiki.util.IpUtils;
import org.tinycloud.mmwiki.web.AuthInterceptor;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;

@Service
public class AuthService {

    private final UserService userService;
    private final ConfigService configService;
    private final MmwikiProperties properties;

    public AuthService(UserService userService, ConfigService configService, MmwikiProperties properties) {
        this.userService = userService;
        this.configService = configService;
        this.properties = properties;
    }

    public boolean isSsoOpen() {
        return "1".equals(configService.getValue("sso_open", "0"));
    }

    public JsonResponse<Void> login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanPassword = password == null ? "" : password.trim();

        if (!StringUtils.hasText(cleanUsername)) {
            return JsonResponse.error("系统用户名不能为空！", null, "", 2000);
        }
        if (cleanUsername.contains("_")) {
            return JsonResponse.error("系统用户名不合法！", null, "", 2000);
        }
        if (!StringUtils.hasText(cleanPassword)) {
            return JsonResponse.error("密码不能为空！", null, "", 2000);
        }

        User user = userService.findActiveByUsername(cleanUsername);
        if (user == null || user.getIsForbidden() == 1) {
            return JsonResponse.error("用户名或密码错误!", null, "", 2000);
        }

        String encodedPassword = userService.encodePassword(cleanPassword);
        if (!encodedPassword.equals(user.getPassword())) {
            return JsonResponse.error("用户名或密码错误!", null, "", 2000);
        }

        int now = Math.toIntExact(Instant.now().getEpochSecond());
        userService.updateLoginSuccess(user.getUserId(), IpUtils.getClientIp(request), now);

        User refreshed = userService.findActiveById(user.getUserId());
        CurrentUser currentUser = CurrentUser.from(refreshed);
        request.getSession(true).setAttribute(AuthInterceptor.SESSION_AUTHOR, currentUser);
        addPassportCookie(currentUser, request, response);
        return JsonResponse.success("登录成功！", null, "/main/index", 2000);
    }

    public JsonResponse<Void> authLoginStub() {
        return JsonResponse.error("统一登录功能尚未迁移完成。", null, "", 2000);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Cookie cookie = new Cookie(properties.getAuthor().getPassport(), "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void addPassportCookie(CurrentUser currentUser, HttpServletRequest request, HttpServletResponse response) {
        String identify = HashUtils.md5(
            request.getHeader("User-Agent") + IpUtils.getClientIp(request) + currentUser.getPasswordHash()
        );
        String value = Base64.getEncoder()
            .encodeToString((currentUser.getUsername() + "@" + identify).getBytes(StandardCharsets.UTF_8));
        Cookie cookie = new Cookie(properties.getAuthor().getPassport(), value);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(properties.getAuthor().getCookieExpired());
        response.addCookie(cookie);
    }
}
