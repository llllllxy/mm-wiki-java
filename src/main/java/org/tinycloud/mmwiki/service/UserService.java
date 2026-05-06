package org.tinycloud.mmwiki.service;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.util.HashUtils;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;

@Service
public class UserService {

    private static final Pattern ALPHA_NUMERIC = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserMapper userMapper;
    private final RoleService roleService;

    public UserService(UserMapper userMapper, RoleService roleService) {
        this.userMapper = userMapper;
        this.roleService = roleService;
    }

    public User findActiveByUsername(String username) {
        return userMapper.findActiveByUsername(username);
    }

    public User findActiveById(Integer userId) {
        return userMapper.findActiveById(userId);
    }

    public List<User> findActiveByIds(List<Integer> userIds) {
        return userIds == null || userIds.isEmpty() ? List.of() : userMapper.findActiveByIds(userIds);
    }

    public List<User> findAllActive() {
        return userMapper.findAllActive();
    }

    public List<User> findActiveExcludingIds(List<Integer> excludedIds) {
        return userMapper.findActiveExcludingIds(excludedIds);
    }

    public long countAllActive() {
        return userMapper.countAllActive();
    }

    public long countByUsernameLike(String username) {
        return userMapper.countByUsernameLike(username);
    }

    public long countByFilters(String username, Integer roleId) {
        return userMapper.countByFilters(username == null ? "" : username.trim(), roleId);
    }

    public List<User> findAllActivePaged(int offset, int size) {
        return userMapper.findAllActivePaged(offset, size);
    }

    public List<User> findByUsernameLikePaged(String username, int offset, int size) {
        return userMapper.findByUsernameLikePaged(username, offset, size);
    }

    public List<User> findByFilters(String username, Integer roleId, int offset, int size) {
        return userMapper.findByFiltersPaged(username == null ? "" : username.trim(), roleId, offset, size);
    }

    public int updateLoginSuccess(Integer userId, String clientIp, Integer now) {
        return userMapper.updateLoginSuccess(userId, clientIp, now);
    }

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

    public int updatePassword(Integer userId, String password) {
        return userMapper.updatePassword(userId, password);
    }

    public int updateForbidden(Integer userId, Integer isForbidden) {
        return userMapper.updateForbidden(userId, isForbidden);
    }

    public int updateRoleId(Integer userId, Integer roleId) {
        return userMapper.updateRoleId(userId, roleId);
    }

    public JsonResponse<Void> saveSystemUser(User user, CurrentUser operator) {
        JsonResponse<Void> validation = validateSystemUser(user, true, operator);
        if (validation != null) {
            return validation;
        }
        if (userMapper.countByUsername(user.getUsername()) > 0) {
            return JsonResponse.error("用户名已经存在！", null, "", 2000);
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        user.setPassword(encodePassword(user.getPassword()));
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        return JsonResponse.success("添加用户成功", null, "/system/user/list", 2000);
    }

    public JsonResponse<Void> updateSystemUser(User user, CurrentUser operator) {
        if (user == null || user.getUserId() == null) {
            return JsonResponse.error("用户不存在！", null, "", 2000);
        }
        User existing = findActiveById(user.getUserId());
        if (existing == null) {
            return JsonResponse.error("用户不存在！", null, "", 2000);
        }
        if (isRootRole(existing.getRoleId()) && !isRoot(operator)) {
            return JsonResponse.error("没有权限修改超级管理员！", null, "", 2000);
        }
        if (isRootRole(existing.getRoleId())) {
            user.setRoleId(RoleService.ROOT_ROLE_ID);
        }
        JsonResponse<Void> validation = validateSystemUser(user, false, operator);
        if (validation != null) {
            return validation;
        }
        user.setUpdateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        if (StringUtils.hasText(user.getPassword()) && isRoot(operator)) {
            user.setPassword(encodePassword(user.getPassword().trim()));
            userMapper.updateSystemUserWithPassword(user);
        } else {
            userMapper.updateSystemUser(user);
        }
        return JsonResponse.success("修改用户成功", null, "/system/user/list", 2000);
    }

    public String encodePassword(String password) {
        return HashUtils.md5(password);
    }

    private JsonResponse<Void> validateSystemUser(User user, boolean requirePassword, CurrentUser operator) {
        if (user == null) {
            return JsonResponse.error("用户信息不能为空！", null, "", 2000);
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
                return JsonResponse.error("用户名不能为空！", null, "", 2000);
            }
            if (!ALPHA_NUMERIC.matcher(user.getUsername()).matches()) {
                return JsonResponse.error("用户名格式不正确！", null, "", 2000);
            }
            if (!StringUtils.hasText(user.getPassword())) {
                return JsonResponse.error("密码不能为空！", null, "", 2000);
            }
        }
        if (!StringUtils.hasText(user.getGivenName())) {
            return JsonResponse.error("姓名不能为空！", null, "", 2000);
        }
        if (!StringUtils.hasText(user.getEmail())) {
            return JsonResponse.error("邮箱不能为空！", null, "", 2000);
        }
        if (!EMAIL.matcher(user.getEmail()).matches()) {
            return JsonResponse.error("邮箱格式不正确！", null, "", 2000);
        }
        if (!StringUtils.hasText(user.getMobile())) {
            return JsonResponse.error("手机号不能为空！", null, "", 2000);
        }
        if (user.getRoleId() == null) {
            return JsonResponse.error("没有选择角色！", null, "", 2000);
        }
        if (roleService.findActiveById(user.getRoleId()) == null) {
            return JsonResponse.error("角色不存在！", null, "", 2000);
        }
        if (isRootRole(user.getRoleId()) && !isRoot(operator)) {
            return JsonResponse.error("没有权限分配超级管理员角色！", null, "", 2000);
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
