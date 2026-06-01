package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.util.BCrypt;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static org.tinycloud.mmwiki.constant.GlobalConstant.DEFAULT_ROLE_ID;
import static org.tinycloud.mmwiki.constant.GlobalConstant.ROOT_ROLE_ID;

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
    @Autowired
    private PasswordCryptoService passwordCryptoService;

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
            user.setRoleId(DEFAULT_ROLE_ID);
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
        this.validateSystemUser(user, true, operator);
        if (this.userMapper.countByUsername(user.getUsername()) > 0) {
            throw new SystemException("用户名已经存在！");
        }
        LocalDateTime now = LocalDateTime.now();
        user.setPassword(BCrypt.hashpw(this.passwordCryptoService.decryptPassword(user.getPassword()), BCrypt.gensalt()));
        user.setCreateTime(now);
        user.setUpdateTime(now);
        this.userMapper.insert(user);
        return JsonResponse.success("添加用户成功", "/system/user/list");
    }

    /**
     * 在系统管理中更新用户资料。
     */
    public JsonResponse<Void> updateSystemUser(User user, CurrentUser operator) {
        if (user == null || user.getUserId() == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在！");
        }
        User existing = findActiveById(user.getUserId());
        if (existing == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在！");
        }
        if (AccessService.isRootRole(existing.getRoleId()) && !AccessService.isRoot(operator)) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "没有权限修改超级管理员！");
        }
        if (AccessService.isRootRole(existing.getRoleId())) {
            user.setRoleId(ROOT_ROLE_ID);
        }
        this.validateSystemUser(user, false, operator);
        user.setUpdateTime(LocalDateTime.now());
        if (StringUtils.hasText(user.getPassword()) && AccessService.isRoot(operator)) {
            user.setPassword(BCrypt.hashpw(passwordCryptoService.decryptPassword(user.getPassword()), BCrypt.gensalt()));
            userMapper.updateSystemUserWithPassword(user);
        } else {
            userMapper.updateSystemUser(user);
        }
        return JsonResponse.success("修改用户成功", "/system/user/list");
    }

    /**
     * 校验并规范化后台用户字段，新增时会额外校验用户名和密码。
     *
     * @param user            待校验的用户信息
     * @param requirePassword 是否要求密码必填
     * @param operator        当前操作人
     */
    private void validateSystemUser(User user, boolean requirePassword, CurrentUser operator) {
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
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在！");
        }
        if (AccessService.isRootRole(user.getRoleId()) && !AccessService.isRoot(operator)) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "没有权限分配超级管理员角色！");
        }
    }


    /**
     * 安全裁剪字符串，null 会转换为空字符串。
     *
     * @param value 原始字符串
     * @return 裁剪后的字符串
     */
    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

