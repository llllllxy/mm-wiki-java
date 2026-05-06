package org.tinycloud.mmwiki.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Role;
import org.tinycloud.mmwiki.mapper.RoleMapper;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class RoleService {

    public static final int ROOT_ROLE_ID = 1;
    public static final int DEFAULT_ROLE_ID = 3;
    public static final int SYSTEM_ROLE_TYPE = 1;
    public static final int CUSTOM_ROLE_TYPE = 0;
    public static final List<Integer> DEFAULT_PRIVILEGE_IDS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

    private final RoleMapper roleMapper;
    private final RolePrivilegeMapper rolePrivilegeMapper;
    private final UserMapper userMapper;

    public RoleService(RoleMapper roleMapper, RolePrivilegeMapper rolePrivilegeMapper, UserMapper userMapper) {
        this.roleMapper = roleMapper;
        this.rolePrivilegeMapper = rolePrivilegeMapper;
        this.userMapper = userMapper;
    }

    public List<Role> findAllActive() {
        return roleMapper.findAllActive();
    }

    public Role findActiveById(Integer roleId) {
        return roleId == null ? null : roleMapper.findActiveById(roleId);
    }

    public Map<Integer, String> roleNameIndex() {
        Map<Integer, String> index = new LinkedHashMap<>();
        for (Role role : findAllActive()) {
            index.put(role.getRoleId(), role.getName());
        }
        return index;
    }

    public String roleName(Integer roleId) {
        Role role = findActiveById(roleId);
        if (role != null && role.getName() != null && !role.getName().isBlank()) {
            return role.getName();
        }
        return switch (roleId == null ? 0 : roleId) {
            case 1 -> "超级管理员";
            case 2 -> "管理员";
            case 3 -> "普通用户";
            default -> "";
        };
    }

    public RolePage list(String keyword, int page, int number) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String search = keyword == null ? "" : keyword.trim();
        long count = search.isEmpty() ? roleMapper.countAll() : roleMapper.countByKeyword(search);
        List<Role> roles = search.isEmpty()
            ? roleMapper.findPaged(offset, safeNumber)
            : roleMapper.findByKeywordPaged(search, offset, safeNumber);
        roles.forEach(role -> role.setUpdateTimeText(org.tinycloud.mmwiki.util.TimeUtils.formatUnix(role.getUpdateTime())));
        return new RolePage(roles, search, Paginator.of(safePage, safeNumber, count, "/system/role/list?keyword=" + search));
    }

    @Transactional
    public JsonResponse<Void> save(Role role) {
        JsonResponse<Void> validation = validate(role, null);
        if (validation != null) {
            return validation;
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        role.setType(CUSTOM_ROLE_TYPE);
        role.setCreateTime(now);
        role.setUpdateTime(now);
        roleMapper.insert(role);
        rolePrivilegeMapper.insertBatch(role.getRoleId(), DEFAULT_PRIVILEGE_IDS, now);
        return JsonResponse.success("添加角色成功", null, "/system/role/list", 2000);
    }

    public JsonResponse<Void> update(Role role) {
        if (role == null || role.getRoleId() == null) {
            return JsonResponse.error("角色不存在", null, "", 2000);
        }
        if (role.getRoleId() == ROOT_ROLE_ID) {
            return JsonResponse.error("超级管理员不能修改", null, "", 2000);
        }
        Role existing = findActiveById(role.getRoleId());
        if (existing == null) {
            return JsonResponse.error("角色不存在", null, "", 2000);
        }
        JsonResponse<Void> validation = validate(role, role.getRoleId());
        if (validation != null) {
            return validation;
        }
        role.setUpdateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        roleMapper.update(role);
        return JsonResponse.success("修改角色成功", null, "/system/role/list", 2000);
    }

    @Transactional
    public JsonResponse<Void> delete(Integer roleId) {
        Role role = findActiveById(roleId);
        if (role == null) {
            return JsonResponse.error("角色不存在", null, "", 2000);
        }
        if (roleId == ROOT_ROLE_ID) {
            return JsonResponse.error("超级管理员不能删除", null, "", 2000);
        }
        if ((role.getType() == null ? CUSTOM_ROLE_TYPE : role.getType()) == SYSTEM_ROLE_TYPE) {
            return JsonResponse.error("系统角色不能删除", null, "", 2000);
        }
        if (userMapper.countByRoleId(roleId) > 0) {
            return JsonResponse.error("不能删除角色，请先移除该角色下用户", null, "", 2000);
        }
        rolePrivilegeMapper.deleteByRoleId(roleId);
        roleMapper.markDeleted(roleId);
        return JsonResponse.success("删除角色成功", null, "/system/role/list", 2000);
    }

    public List<Integer> rolePrivilegeIds(Integer roleId) {
        if (roleId == null) {
            return List.of();
        }
        if (roleId == ROOT_ROLE_ID) {
            return List.of();
        }
        return rolePrivilegeMapper.findPrivilegeIdsByRoleId(roleId);
    }

    @Transactional
    public JsonResponse<Void> grantPrivileges(Integer roleId, List<Integer> privilegeIds) {
        Role role = findActiveById(roleId);
        if (role == null) {
            return JsonResponse.error("角色不存在", null, "", 2000);
        }
        if (roleId == ROOT_ROLE_ID) {
            return JsonResponse.error("超级管理员不需要授权", null, "", 2000);
        }
        Set<Integer> merged = new LinkedHashSet<>(DEFAULT_PRIVILEGE_IDS);
        if (privilegeIds != null) {
            merged.addAll(privilegeIds);
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        rolePrivilegeMapper.deleteByRoleId(roleId);
        rolePrivilegeMapper.insertBatch(roleId, new ArrayList<>(merged), now);
        return JsonResponse.success("角色授权成功", null, "/system/role/list", 2000);
    }

    public JsonResponse<Void> resetUserRole(Integer userId) {
        if (userId == null) {
            return JsonResponse.error("用户不存在", null, "", 2000);
        }
        var user = userMapper.findActiveById(userId);
        if (user == null) {
            return JsonResponse.error("用户不存在", null, "", 2000);
        }
        if (userId == ROOT_ROLE_ID) {
            return JsonResponse.error("root 用户不能重置角色", null, "", 2000);
        }
        userMapper.updateRoleId(userId, DEFAULT_ROLE_ID);
        return JsonResponse.success("重置用户角色成功", null, "/system/role/list", 2000);
    }

    private JsonResponse<Void> validate(Role role, Integer currentId) {
        if (role == null || !StringUtils.hasText(role.getName())) {
            return JsonResponse.error("角色名称不能为空", null, "", 2000);
        }
        String name = role.getName().trim();
        long duplicate = currentId == null ? roleMapper.countByName(name) : roleMapper.countByNameAndNotId(currentId, name);
        if (duplicate > 0) {
            return JsonResponse.error("角色名已经存在", null, "", 2000);
        }
        role.setName(name);
        return null;
    }

    public record RolePage(List<Role> roles, String keyword, Paginator paginator) {
    }
}
