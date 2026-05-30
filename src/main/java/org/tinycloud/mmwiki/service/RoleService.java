package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.Role;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.RoleMapper;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

import java.time.LocalDateTime;
import java.util.*;

import static org.tinycloud.mmwiki.constant.GlobalConstant.*;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class RoleService {


    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RolePrivilegeMapper rolePrivilegeMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 查询全部未删除角色。
     *
     * @return 有效角色列表
     */
    public List<Role> findAllActive() {
        return roleMapper.findAllActive();
    }

    /**
     * 根据角色ID查询未删除角色。
     *
     * @param roleId 角色ID
     * @return 角色记录，不存在时返回 null
     */
    public Role findActiveById(Integer roleId) {
        return roleId == null ? null : roleMapper.findActiveById(roleId);
    }

    /**
     * 查询角色名称索引，用于页面展示角色名称。
     *
     * @return 以角色ID为键、角色名称为值的映射
     */
    public Map<Integer, String> roleNameIndex() {
        Map<Integer, String> index = new LinkedHashMap<>();
        for (Role role : findAllActive()) {
            index.put(role.getRoleId(), role.getName());
        }
        return index;
    }

    /**
     * 获取角色名称，数据库不存在时回退到系统内置角色名称。
     *
     * @param roleId 角色ID
     * @return 角色名称
     */
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

    /**
     * 分页查询角色，支持按关键字过滤并格式化更新时间。
     *
     * @param keyword  查询关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 角色分页数据
     */
    public PageModel<Role> pageModel(String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        Page<Role> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> {
                    if (search.isEmpty()) {
                        roleMapper.pageAll();
                    } else {
                        roleMapper.pageByKeyword(search);
                    }
                });
        pageInfo.getRecords().forEach(role -> role.setUpdateTimeText(TimeUtils.format(role.getUpdateTime())));
        return PageModel.from(pageInfo);
    }

    /**
     * 新增自定义角色，并自动授予默认权限。
     *
     * @param role 待新增的角色
     * @return 前端统一 JSON 响应
     */
    @Transactional
    public JsonResponse<Void> save(Role role) {
        JsonResponse<Void> validation = validate(role, null);
        if (validation != null) {
            return validation;
        }
        LocalDateTime now = LocalDateTime.now();
        role.setType(CUSTOM_ROLE_TYPE);
        role.setCreateTime(now);
        role.setUpdateTime(now);
        roleMapper.insert(role);
        rolePrivilegeMapper.insertBatch(role.getRoleId(), DEFAULT_PRIVILEGE_IDS, now);
        return JsonResponse.success("添加角色成功", "/system/role/list");
    }

    /**
     * 更新角色基础信息，超级管理员角色禁止修改。
     *
     * @param role 待更新的角色
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> update(Role role) {
        if (role == null || role.getRoleId() == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在");
        }
        if (role.getRoleId() == ROOT_ROLE_ID) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "超级管理员不能修改");
        }
        Role existing = findActiveById(role.getRoleId());
        if (existing == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在");
        }
        JsonResponse<Void> validation = validate(role, role.getRoleId());
        if (validation != null) {
            return validation;
        }
        role.setUpdateTime(TimeUtils.now());
        roleMapper.update(role);
        return JsonResponse.success("修改角色成功", "/system/role/list");
    }

    /**
     * 删除自定义角色，删除前会校验系统角色、超级管理员角色和用户占用情况。
     *
     * @param roleId 角色ID
     * @return 前端统一 JSON 响应
     */
    @Transactional
    public JsonResponse<Void> delete(Integer roleId) {
        Role role = findActiveById(roleId);
        if (role == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在");
        }
        if (roleId == ROOT_ROLE_ID) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "超级管理员不能删除");
        }
        if ((role.getType() == null ? CUSTOM_ROLE_TYPE : role.getType()) == SYSTEM_ROLE_TYPE) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "系统角色不能删除");
        }
        if (userMapper.countByRoleId(roleId) > 0) {
            throw new SystemException("不能删除角色，请先移除该角色下用户");
        }
        rolePrivilegeMapper.deleteByRoleId(roleId);
        roleMapper.markDeleted(roleId);
        return JsonResponse.success("删除角色成功", "/system/role/list");
    }

    /**
     * 查询指定角色已授予的权限ID列表，超级管理员不需要显式权限列表。
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    public List<Integer> rolePrivilegeIds(Integer roleId) {
        if (roleId == null) {
            return List.of();
        }
        if (roleId == ROOT_ROLE_ID) {
            return List.of();
        }
        return rolePrivilegeMapper.findPrivilegeIdsByRoleId(roleId);
    }

    /**
     * 重置角色权限，始终合并系统默认权限后重新写入角色权限关系。
     *
     * @param roleId       角色ID
     * @param privilegeIds 前端提交的权限ID列表
     * @return 前端统一 JSON 响应
     */
    @Transactional
    public JsonResponse<Void> grantPrivileges(Integer roleId, List<Integer> privilegeIds) {
        Role role = findActiveById(roleId);
        if (role == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在");
        }
        if (roleId == ROOT_ROLE_ID) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "超级管理员不需要授权");
        }
        Set<Integer> merged = new LinkedHashSet<>(DEFAULT_PRIVILEGE_IDS);
        if (privilegeIds != null) {
            merged.addAll(privilegeIds);
        }
        LocalDateTime now = LocalDateTime.now();
        rolePrivilegeMapper.deleteByRoleId(roleId);
        rolePrivilegeMapper.insertBatch(roleId, new ArrayList<>(merged), now);
        return JsonResponse.success("角色授权成功", "/system/role/list");
    }

    /**
     * 将指定用户角色重置为默认普通用户角色。
     *
     * @param userId 用户ID
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> resetUserRole(Integer userId) {
        if (userId == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在");
        }
        var user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在");
        }
        if (userId == ROOT_ROLE_ID) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "root 用户不能重置角色");
        }
        userMapper.updateRoleId(userId, DEFAULT_ROLE_ID);
        return JsonResponse.success("重置用户角色成功", "/system/role/user?role_id=" + DEFAULT_ROLE_ID);
    }

    /**
     * 校验并规范化角色名称，包含必填和名称唯一性检查。
     *
     * @param role      待校验的角色
     * @param currentId 当前更新角色ID，新增时为 null
     * @return 校验通过时返回 null，校验失败时抛出业务异常
     */
    private JsonResponse<Void> validate(Role role, Integer currentId) {
        if (role == null || !StringUtils.hasText(role.getName())) {
            throw new SystemException("角色名称不能为空");
        }
        String name = role.getName().trim();
        long duplicate = currentId == null ? roleMapper.countByName(name) : roleMapper.countByNameAndNotId(currentId, name);
        if (duplicate > 0) {
            throw new SystemException("角色名已经存在");
        }
        role.setName(name);
        return null;
    }
}
