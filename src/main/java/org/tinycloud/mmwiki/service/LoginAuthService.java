package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.util.TimeUtils;

import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.mapper.LoginAuthMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

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

    /**
     * 分页查询登录认证配置，支持按名称或地址关键字过滤。
     *
     * @param keyword  查询关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 登录认证配置分页数据
     */
    public PageModel<LoginAuth> pageModel(String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        Page<LoginAuth> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> {
                    if (search.isEmpty()) {
                        loginAuthMapper.pageAllActive();
                    } else {
                        loginAuthMapper.pageByKeyword(search);
                    }
                });
        return PageModel.from(pageInfo);
    }

    /**
     * 根据ID查询未删除的登录认证配置。
     *
     * @param loginAuthId 登录认证配置ID
     * @return 登录认证配置，不存在时返回 null
     */
    public LoginAuth findById(Integer loginAuthId) {
        return loginAuthId == null ? null : loginAuthMapper.findActiveById(loginAuthId);
    }

    /**
     * 查询当前启用的登录认证配置。
     *
     * @return 已启用的登录认证配置，不存在时返回 null
     */
    public LoginAuth findUsed() {
        return loginAuthMapper.findUsed();
    }

    /**
     * 新增登录认证配置，保存前会校验名称、用户名前缀、认证地址和唯一性。
     *
     * @param loginAuth 待新增的登录认证配置
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> save(LoginAuth loginAuth) {
        JsonResponse<Void> validation = validate(loginAuth, null);
        if (validation != null) {
            return validation;
        }
        LocalDateTime now = LocalDateTime.now();
        loginAuth.setCreateTime(now);
        loginAuth.setUpdateTime(now);
        loginAuth.setIsUsed(0);
        loginAuth.setIsDelete(0);
        loginAuthMapper.insert(loginAuth);
        return JsonResponse.success("添加登录认证成功", "/system/auth/list");
    }

    /**
     * 更新登录认证配置，要求目标配置存在并通过字段校验。
     *
     * @param loginAuth 待更新的登录认证配置
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> update(LoginAuth loginAuth) {
        if (loginAuth.getLoginAuthId() == null || findById(loginAuth.getLoginAuthId()) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "登录认证不存在。");
        }
        JsonResponse<Void> validation = validate(loginAuth, loginAuth.getLoginAuthId());
        if (validation != null) {
            return validation;
        }
        loginAuth.setUpdateTime(TimeUtils.now());
        loginAuthMapper.update(loginAuth);
        return JsonResponse.success("修改登录认证成功", "/system/auth/list");
    }

    /**
     * 启用指定登录认证配置，并清除其他配置的启用状态。
     *
     * @param loginAuthId 登录认证配置ID
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> markUsed(Integer loginAuthId) {
        LoginAuth auth = findById(loginAuthId);
        if (auth == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "登录认证不存在。");
        }
        loginAuthMapper.clearUsed();
        loginAuthMapper.markUsed(loginAuthId);
        return JsonResponse.success("启用登录认证成功", "/system/auth/list");
    }

    /**
     * 逻辑删除指定登录认证配置。
     *
     * @param loginAuthId 登录认证配置ID
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> delete(Integer loginAuthId) {
        LoginAuth auth = findById(loginAuthId);
        if (auth == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "登录认证不存在。");
        }
        loginAuthMapper.markDeleted(loginAuthId);
        return JsonResponse.success("删除登录认证成功", "/system/auth/list");
    }

    /**
     * 校验并规范化登录认证配置字段，包含 URL 协议和唯一性检查。
     *
     * @param loginAuth 待校验的登录认证配置
     * @param currentId 当前更新记录ID，新增时为 null
     * @return 校验通过时返回 null，校验失败时抛出业务异常
     */
    private JsonResponse<Void> validate(LoginAuth loginAuth, Integer currentId) {
        if (loginAuth == null) {
            throw new SystemException("登录认证参数错误。");
        }
        if (!StringUtils.hasText(loginAuth.getName())) {
            throw new SystemException("登录认证名称不能为空。");
        }
        if (!StringUtils.hasText(loginAuth.getUsernamePrefix())) {
            throw new SystemException("用户名前缀不能为空。");
        }
        if (!loginAuth.getUsernamePrefix().matches("^[A-Za-z0-9]+$")) {
            throw new SystemException("用户名前缀格式不正确。");
        }
        if (!StringUtils.hasText(loginAuth.getUrl())) {
            throw new SystemException("认证 URL 不能为空。");
        }
        try {
            URI uri = URI.create(loginAuth.getUrl().trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)
                && !"ldap".equalsIgnoreCase(scheme) && !"ldaps".equalsIgnoreCase(scheme))) {
                throw new SystemException("认证 URL 协议不支持。");
            }
        } catch (Exception ex) {
            throw new SystemException("认证 URL 解析错误。");
        }
        long duplicateName = currentId == null
            ? loginAuthMapper.countByName(loginAuth.getName().trim())
            : loginAuthMapper.countByNameAndNotId(currentId, loginAuth.getName().trim());
        if (duplicateName > 0) {
            throw new SystemException("登录认证名称已经存在。");
        }
        long duplicatePrefix = currentId == null
            ? loginAuthMapper.countByUsernamePrefix(loginAuth.getUsernamePrefix().trim())
            : loginAuthMapper.countByUsernamePrefixAndNotId(currentId, loginAuth.getUsernamePrefix().trim());
        if (duplicatePrefix > 0) {
            throw new SystemException("用户名前缀已经存在。");
        }
        loginAuth.setName(loginAuth.getName().trim());
        loginAuth.setUsernamePrefix(loginAuth.getUsernamePrefix().trim());
        loginAuth.setUrl(loginAuth.getUrl().trim());
        loginAuth.setExtData(loginAuth.getExtData() == null ? "" : loginAuth.getExtData().trim());
        return null;
    }
}
