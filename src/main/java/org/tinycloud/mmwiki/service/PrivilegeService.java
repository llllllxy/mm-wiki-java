package org.tinycloud.mmwiki.service;

import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.vo.PrivilegeGroups;
import org.tinycloud.mmwiki.util.TimeUtils;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Privilege;
import org.tinycloud.mmwiki.mapper.PrivilegeMapper;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.util.TimeUtils;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class PrivilegeService {

    @Autowired
    private PrivilegeMapper privilegeMapper;

    @Autowired
    private RolePrivilegeMapper rolePrivilegeMapper;

    @Value("${spring.profiles.active:dev}")
    private String mode;

    /**
     * 查询全部权限并按菜单权限和控制器权限分组。
     *
     * @return 权限分组视图
     */
    public PrivilegeGroups groups() {
        List<Privilege> all = privilegeMapper.findAllOrderBySequence();
        return new PrivilegeGroups(
            all.stream().filter(item -> "menu".equalsIgnoreCase(item.getType())).toList(),
            all.stream().filter(item -> "controller".equalsIgnoreCase(item.getType())).toList()
        );
    }

    /**
     * 根据权限ID查询权限配置。
     *
     * @param privilegeId 权限ID
     * @return 权限记录，不存在时返回 null
     */
    public Privilege findById(Integer privilegeId) {
        return privilegeId == null ? null : privilegeMapper.findById(privilegeId);
    }

    /**
     * 获取当前运行模式，未配置时默认按开发模式处理。
     *
     * @return 当前运行模式
     */
    public String mode() {
        return mode == null || mode.isBlank() ? "dev" : mode;
    }

    /**
     * 判断当前是否为开发模式，权限配置增删改仅允许开发模式执行。
     *
     * @return true 表示开发模式
     */
    public boolean devMode() {
        return mode().toLowerCase().contains("dev");
    }

    /**
     * 新增权限配置，仅允许在开发模式下执行。
     *
     * @param privilege 待新增的权限配置
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> save(Privilege privilege) {
        if (!devMode()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "只允许在开发模式下添加权限");
        }
        JsonResponse<Void> validation = validate(privilege);
        if (validation != null) {
            return validation;
        }
        LocalDateTime now = LocalDateTime.now();
        privilege.setCreateTime(now);
        privilege.setUpdateTime(now);
        privilegeMapper.insert(privilege);
        return JsonResponse.success("添加权限成功", "/system/privilege/list");
    }

    /**
     * 更新权限配置，仅允许在开发模式下执行。
     *
     * @param privilege 待更新的权限配置
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> update(Privilege privilege) {
        if (!devMode()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "只允许在开发模式下修改权限");
        }
        if (privilege == null || privilege.getPrivilegeId() == null || findById(privilege.getPrivilegeId()) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "权限不存在");
        }
        JsonResponse<Void> validation = validate(privilege);
        if (validation != null) {
            return validation;
        }
        privilege.setUpdateTime(TimeUtils.now());
        privilegeMapper.update(privilege);
        return JsonResponse.success("修改权限成功", "/system/privilege/list");
    }

    /**
     * 删除权限配置，仅允许在开发模式下执行，并会清理角色权限关系。
     *
     * @param privilegeId 权限ID
     * @return 前端统一 JSON 响应
     */
    @Transactional
    public JsonResponse<Void> delete(Integer privilegeId) {
        if (!devMode()) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "只允许在开发模式下删除权限");
        }
        Privilege privilege = findById(privilegeId);
        if (privilege == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "权限不存在");
        }
        if (privilegeMapper.countChildren(privilegeId) > 0) {
            throw new SystemException("请先删除该菜单下的权限");
        }
        rolePrivilegeMapper.deleteByPrivilegeId(privilegeId);
        privilegeMapper.deleteById(privilegeId);
        return JsonResponse.success("删除权限成功", "/system/privilege/list");
    }

    /**
     * 校验并规范化权限配置字段，控制器权限会额外校验父级菜单和路由信息。
     *
     * @param privilege 待校验的权限配置
     * @return 校验通过时返回 null，校验失败时抛出业务异常
     */
    private JsonResponse<Void> validate(Privilege privilege) {
        if (privilege == null || !StringUtils.hasText(privilege.getName())) {
            throw new SystemException("权限名称不能为空");
        }
        if (!StringUtils.hasText(privilege.getType())) {
            throw new SystemException("没有选择权限类型");
        }
        String type = privilege.getType().trim();
        if (!"menu".equals(type) && !"controller".equals(type)) {
            throw new SystemException("权限类型错误");
        }
        privilege.setName(privilege.getName().trim());
        privilege.setType(type);
        privilege.setIcon(StringUtils.hasText(privilege.getIcon()) ? privilege.getIcon().trim() : "glyphicon-list");
        privilege.setTarget(StringUtils.hasText(privilege.getTarget()) ? privilege.getTarget().trim() : "");
        privilege.setSequence(privilege.getSequence() == null ? 0 : privilege.getSequence());
        privilege.setIsDisplay(privilege.getIsDisplay() == null ? 0 : privilege.getIsDisplay());
        if ("controller".equals(type)) {
            if (privilege.getParentId() == null || privilege.getParentId() <= 0) {
                throw new SystemException("控制器必须选择上级菜单");
            }
            if (!StringUtils.hasText(privilege.getController())) {
                throw new SystemException("控制器名称不能为空");
            }
            if (!StringUtils.hasText(privilege.getAction())) {
                throw new SystemException("方法名称不能为空");
            }
            privilege.setController(privilege.getController().trim());
            privilege.setAction(privilege.getAction().trim());
        } else {
            privilege.setParentId(0);
            privilege.setController("");
            privilege.setAction("");
            privilege.setTarget("");
        }
        return null;
    }
}

