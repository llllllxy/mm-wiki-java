package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.tinycloud.mmwiki.vo.AuthPage;

import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.mapper.LoginAuthMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class LoginAuthService {

    @Autowired
    private LoginAuthMapper loginAuthMapper;

    public AuthPage list(String keyword, int page, int number) {
        String search = keyword == null ? "" : keyword.trim();
        PageInfo<LoginAuth> pageInfo = PageHelper.startPage(page, number)
                .doSelectPageInfo(() -> {
                    if (search.isEmpty()) {
                        loginAuthMapper.pageAllActive();
                    } else {
                        loginAuthMapper.pageByKeyword(search);
                    }
                });
        return new AuthPage(pageInfo.getList(), search, Paginator.of(page, number, pageInfo.getTotal(), "/system/auth/list?keyword=" + search));
    }

    public LoginAuth findById(Integer loginAuthId) {
        return loginAuthId == null ? null : loginAuthMapper.findActiveById(loginAuthId);
    }

    public LoginAuth findUsed() {
        return loginAuthMapper.findUsed();
    }

    public JsonResponse<Void> save(LoginAuth loginAuth) {
        JsonResponse<Void> validation = validate(loginAuth, null);
        if (validation != null) {
            return validation;
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        loginAuth.setCreateTime(now);
        loginAuth.setUpdateTime(now);
        loginAuth.setIsUsed(0);
        loginAuth.setIsDelete(0);
        loginAuthMapper.insert(loginAuth);
        return JsonResponse.success("添加登录认证成功", "/system/auth/list");
    }

    public JsonResponse<Void> update(LoginAuth loginAuth) {
        if (loginAuth.getLoginAuthId() == null || findById(loginAuth.getLoginAuthId()) == null) {
            return JsonResponse.error("登录认证不存在。");
        }
        JsonResponse<Void> validation = validate(loginAuth, loginAuth.getLoginAuthId());
        if (validation != null) {
            return validation;
        }
        loginAuth.setUpdateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        loginAuthMapper.update(loginAuth);
        return JsonResponse.success("修改登录认证成功", "/system/auth/list");
    }

    public JsonResponse<Void> markUsed(Integer loginAuthId) {
        LoginAuth auth = findById(loginAuthId);
        if (auth == null) {
            return JsonResponse.error("登录认证不存在。");
        }
        loginAuthMapper.clearUsed();
        loginAuthMapper.markUsed(loginAuthId);
        return JsonResponse.success("启用登录认证成功", "/system/auth/list");
    }

    public JsonResponse<Void> delete(Integer loginAuthId) {
        LoginAuth auth = findById(loginAuthId);
        if (auth == null) {
            return JsonResponse.error("登录认证不存在。");
        }
        loginAuthMapper.markDeleted(loginAuthId);
        return JsonResponse.success("删除登录认证成功", "/system/auth/list");
    }

    private JsonResponse<Void> validate(LoginAuth loginAuth, Integer currentId) {
        if (loginAuth == null) {
            return JsonResponse.error("登录认证参数错误。");
        }
        if (!StringUtils.hasText(loginAuth.getName())) {
            return JsonResponse.error("登录认证名称不能为空。");
        }
        if (!StringUtils.hasText(loginAuth.getUsernamePrefix())) {
            return JsonResponse.error("用户名前缀不能为空。");
        }
        if (!loginAuth.getUsernamePrefix().matches("^[A-Za-z0-9]+$")) {
            return JsonResponse.error("用户名前缀格式不正确。");
        }
        if (!StringUtils.hasText(loginAuth.getUrl())) {
            return JsonResponse.error("认证 URL 不能为空。");
        }
        try {
            URI uri = URI.create(loginAuth.getUrl().trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)
                && !"ldap".equalsIgnoreCase(scheme) && !"ldaps".equalsIgnoreCase(scheme))) {
                return JsonResponse.error("认证 URL 协议不支持。");
            }
        } catch (Exception ex) {
            return JsonResponse.error("认证 URL 解析错误。");
        }
        long duplicateName = currentId == null
            ? loginAuthMapper.countByName(loginAuth.getName().trim())
            : loginAuthMapper.countByNameAndNotId(currentId, loginAuth.getName().trim());
        if (duplicateName > 0) {
            return JsonResponse.error("登录认证名称已经存在。");
        }
        long duplicatePrefix = currentId == null
            ? loginAuthMapper.countByUsernamePrefix(loginAuth.getUsernamePrefix().trim())
            : loginAuthMapper.countByUsernamePrefixAndNotId(currentId, loginAuth.getUsernamePrefix().trim());
        if (duplicatePrefix > 0) {
            return JsonResponse.error("用户名前缀已经存在。");
        }
        loginAuth.setName(loginAuth.getName().trim());
        loginAuth.setUsernamePrefix(loginAuth.getUsernamePrefix().trim());
        loginAuth.setUrl(loginAuth.getUrl().trim());
        loginAuth.setExtData(loginAuth.getExtData() == null ? "" : loginAuth.getExtData().trim());
        return null;
    }
}
