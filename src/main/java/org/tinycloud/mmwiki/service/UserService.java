package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.util.HashUtils;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.util.TimeUtils;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class UserService {

    private static final Pattern ALPHA_NUMERIC = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleService roleService;

    /**
     * 按用户名查询未删除用户。
     */
    public User findActiveByUsername(String username) {
        return userMapper.findActiveByUsername(username);
    }

    /**
     * 按用户 ID 查询未删除用户。
     */
    public User findActiveById(Integer userId) {
        return userMapper.findActiveById(userId);
    }

    /**
     * 批量查询未删除用户。
     */
    public List<User> findActiveByIds(List<Integer> userIds) {
        return userIds == null || userIds.isEmpty() ? List.of() : userMapper.findActiveByIds(userIds);
    }

    /**
     * 查询全部未删除用户。
     */
    public List<User> findAllActive() {
        return userMapper.findAllActive();
    }

    /**
     * 查询排除指定 ID 后的未删除用户。
     */
    public List<User> findActiveExcludingIds(List<Integer> excludedIds) {
        return userMapper.findActiveExcludingIds(excludedIds);
    }

    /**
     * 分页查询未删除用户。
     */
    public List<User> pageAllActive() {
        return userMapper.pageAllActive();
    }

    /**
     * 按用户名关键字分页查询用户。
     */
    public List<User> pageByUsernameLike(String username) {
        return userMapper.pageByUsernameLike(username);
    }

    /**
     * 按筛选条件分页查询用户。
     */
    public List<User> pageByFilters(String username, Integer roleId) {
        return userMapper.pageByFilters(username == null ? "" : username.trim(), roleId);
    }

    /**
     * 记录用户最近一次登录成功信息。
     */
    public int updateLoginSuccess(Integer userId, String clientIp, Integer now) {
        return userMapper.updateLoginSuccess(userId, clientIp, now);
    }

    /**
     * 同步统一登录用户资料，不存在时按默认角色自动创建。
     */
    @Transactional(rollbackFor = Exception.class)
    public User saveOrUpdateAuthUser(User user) {
        User existing = findActiveByUsername(user.getUsername());
        if (existing == null) {
            user.setRoleId(RoleService.DEFAULT_ROLE_ID);
            userMapper.insertAuthUser(user);
        } else {
            userMapper.updateAuthUserByUsername(user);
        }
        return findActiveByUsername(user.getUsername());
    }

    /**
     * 更新用户个人资料字段。
     */
    public int updateProfile(
            Integer userId,
            String givenName,
            String email,
            String mobile,
            String phone,
            String department,
            String position,
            String location,
            String im
    ) {
        return userMapper.updateProfile(userId, givenName, email, mobile, phone, department, position, location, im);
    }

    /**
     * 更新用户登录密码。
     */
    public int updatePassword(Integer userId, String password) {
        return userMapper.updatePassword(userId, password);
    }

    /**
     * 更新用户禁用状态。
     */
    public int updateForbidden(Integer userId, Integer isForbidden) {
        return userMapper.updateForbidden(userId, isForbidden);
    }

    /**
     * 更新用户所属角色。
     */
    public int updateRoleId(Integer userId, Integer roleId) {
        return userMapper.updateRoleId(userId, roleId);
    }

    /**
     * 在系统管理中创建用户。
     */
    public JsonResponse<Void> saveSystemUser(User user, CurrentUser operator) {
        JsonResponse<Void> validation = validateSystemUser(user, true, operator);
        if (validation != null) {
            return validation;
        }
        if (userMapper.countByUsername(user.getUsername()) > 0) {
            throw new SystemException("用户名已经存在！");
        }
        LocalDateTime now = LocalDateTime.now();
        user.setPassword(encodePassword(user.getPassword()));
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        return JsonResponse.success("添加用户成功", "/system/user/list");
    }

    /**
     * 在系统管理中更新用户资料。
     */
    public JsonResponse<Void> updateSystemUser(User user, CurrentUser operator) {
        if (user == null || user.getUserId() == null) {
            throw new SystemException("用户不存在！");
        }
        User existing = findActiveById(user.getUserId());
        if (existing == null) {
            throw new SystemException("用户不存在！");
        }
        if (isRootRole(existing.getRoleId()) && !isRoot(operator)) {
            throw new SystemException("没有权限修改超级管理员！");
        }
        if (isRootRole(existing.getRoleId())) {
            user.setRoleId(RoleService.ROOT_ROLE_ID);
        }
        JsonResponse<Void> validation = validateSystemUser(user, false, operator);
        if (validation != null) {
            return validation;
        }
        user.setUpdateTime(TimeUtils.now());
        if (StringUtils.hasText(user.getPassword()) && isRoot(operator)) {
            user.setPassword(encodePassword(user.getPassword().trim()));
            userMapper.updateSystemUserWithPassword(user);
        } else {
            userMapper.updateSystemUser(user);
        }
        return JsonResponse.success("修改用户成功", "/system/user/list");
    }

    /**
     * 按照旧版 MM-Wiki 规则生成密码摘要。
     */
    public String encodePassword(String password) {
        return HashUtils.md5(password);
    }

    private JsonResponse<Void> validateSystemUser(User user, boolean requirePassword, CurrentUser operator) {
        if (user == null) {
            throw new SystemException("用户信息不能为空！");
        }
        user.setUsername(trim(user.getUsername()));
        user.setGivenName(trim(user.getGivenName()));
        user.setPassword(trim(user.getPassword()));
        user.setEmail(trim(user.getEmail()));
        user.setMobile(trim(user.getMobile()));
        user.setPhone(trim(user.getPhone()));
        user.setDepartment(trim(user.getDepartment()));
        user.setPosition(trim(user.getPosition()));
        user.setLocation(trim(user.getLocation()));
        user.setIm(trim(user.getIm()));

        if (requirePassword) {
            if (!StringUtils.hasText(user.getUsername())) {
                throw new SystemException("用户名不能为空！");
            }
            if (!ALPHA_NUMERIC.matcher(user.getUsername()).matches()) {
                throw new SystemException("用户名格式不正确！");
            }
            if (!StringUtils.hasText(user.getPassword())) {
                throw new SystemException("密码不能为空！");
            }
        }
        if (!StringUtils.hasText(user.getGivenName())) {
            throw new SystemException("姓名不能为空！");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new SystemException("邮箱不能为空！");
        }
        if (!EMAIL.matcher(user.getEmail()).matches()) {
            throw new SystemException("邮箱格式不正确！");
        }
        if (!StringUtils.hasText(user.getMobile())) {
            throw new SystemException("手机号不能为空！");
        }
        if (user.getRoleId() == null) {
            throw new SystemException("没有选择角色！");
        }
        if (roleService.findActiveById(user.getRoleId()) == null) {
            throw new SystemException("角色不存在！");
        }
        if (isRootRole(user.getRoleId()) && !isRoot(operator)) {
            throw new SystemException("没有权限分配超级管理员角色！");
        }
        return null;
    }

    private boolean isRoot(CurrentUser currentUser) {
        return currentUser != null && isRootRole(currentUser.getRoleId());
    }

    private boolean isRootRole(Integer roleId) {
        return roleId != null && roleId == RoleService.ROOT_ROLE_ID;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

