package org.tinycloud.mmwiki.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.time.Instant;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.constant.GlobalConstant;
import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.util.BCrypt;
import org.tinycloud.mmwiki.util.IpUtils;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.service.UnifiedAuthService.AuthLoginProfile;

/**
 * 本地账号认证服务，负责登录校验、密码验证和当前用户会话信息构建。
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
    @Autowired
    private PasswordCryptoService passwordCryptoService;

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
            throw new SystemException("系统用户名不能为空！");
        }
        if (cleanUsername.contains("_")) {
            throw new SystemException("系统用户名不合法！");
        }
        if (!StringUtils.hasText(cleanPassword)) {
            throw new SystemException("密码不能为空！");
        }

        User user = userService.findActiveByUsername(cleanUsername);
        if (user == null || user.getIsForbidden() == 1) {
            throw new SystemException("用户名或密码错误!");
        }

        String plainPassword = passwordCryptoService.decryptPassword(cleanPassword);
        boolean isMatch = BCrypt.checkpw(plainPassword, user.getPassword());
        if (!isMatch) {
            throw new SystemException("用户名或密码错误!");
        }

        int now = Math.toIntExact(Instant.now().getEpochSecond());
        userService.updateLoginSuccess(user.getUserId(), IpUtils.getClientIp(request), now);

        User refreshed = userService.findActiveById(user.getUserId());
        CurrentUser currentUser = CurrentUser.from(refreshed);
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(GlobalConstant.SESSION_AUTHOR, currentUser);
        // 写入 Spring Session 标准 principalName 索引字段，映射到 spring_session.PRINCIPAL_NAME。
        // 便于后续按用户名查询会话、统计在线用户、实现单点登录及强制下线等功能
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, currentUser.getUsername());
        return JsonResponse.success("登录成功！", "/main/index");
    }

    /**
     * 校验统一登录账号密码，同步本地用户并写入登录会话。
     */
    public JsonResponse<Void> authLogin(String username, String password,
                                        HttpServletRequest request, HttpServletResponse response) {
        if (!isSsoOpen()) {
            throw new SystemException("系统未开启统一登录功能！");
        }
        LoginAuth loginAuth = loginAuthService.findUsed();
        if (loginAuth == null) {
            throw new SystemException("统一登录认证配置不可用！");
        }

        String cleanUsername = username == null ? "" : username.trim();
        String cleanPassword = password == null ? "" : password.trim();
        if (!StringUtils.hasText(cleanUsername)) {
            throw new SystemException("统一登录用户名不能为空！");
        }
        if (!StringUtils.hasText(cleanPassword)) {
            throw new SystemException("统一登录密码不能为空！");
        }

        AuthLoginProfile profile;
        String plainPassword = passwordCryptoService.decryptPassword(cleanPassword);
        try {
            profile = unifiedAuthService.authenticate(loginAuth, cleanUsername, plainPassword);
        } catch (Exception ex) {
            log.error("统一登录失败：{}", ex.getMessage(), ex);
            throw new SystemException("统一登录失败！");
        }
        if (profile == null) {
            throw new SystemException("统一登录失败！");
        }

        int loginTime = Math.toIntExact(Instant.now().getEpochSecond());
        LocalDateTime now = LocalDateTime.now();
        String realUsername = loginAuth.getUsernamePrefix() + "_" + cleanUsername;
        User user = new User();
        user.setUsername(realUsername);
        user.setGivenName(value(profile.getGivenName()));
        user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
        user.setEmail(value(profile.getEmail()));
        user.setMobile(value(profile.getMobile()));
        user.setPhone(value(profile.getPhone()));
        user.setDepartment(value(profile.getDepartment()));
        user.setPosition(value(profile.getPosition()));
        user.setLocation(value(profile.getLocation()));
        user.setIm(value(profile.getIm()));
        user.setLastIp(IpUtils.getClientIp(request));
        user.setLastTime(loginTime);
        user.setCreateTime(now);
        user.setUpdateTime(now);

        User refreshed = userService.saveOrUpdateAuthUser(user);
        if (refreshed == null) {
            throw new SystemException("登录失败!");
        }
        CurrentUser currentUser = CurrentUser.from(refreshed);
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(GlobalConstant.SESSION_AUTHOR, currentUser);
        return JsonResponse.success("登录成功！", "/main/index");
    }

    /**
     * 清理服务端会话，完成本地退出登录。
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * 将统一认证返回的可空字段转换为空字符串，避免同步本地用户时写入 null。
     *
     * @param value 原始字段值
     * @return 非 null 字符串
     */
    private String value(String value) {
        return value == null ? "" : value;
    }
}
