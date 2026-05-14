package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.config.GlobalConstant;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.SpaceUser;
import org.tinycloud.mmwiki.vo.Access;
import org.tinycloud.mmwiki.web.CurrentUser;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class AccessService {


    /**
     * 空间成员服务，用于查询用户在指定空间下的成员身份和权限等级。
     */
    @Autowired
    private SpaceUserService spaceUserService;

    /**
     * 计算当前用户对指定空间的访问权限。
     *
     * @param currentUser 当前登录用户
     * @param space       待判断权限的空间
     * @return 当前用户在该空间下的访问、编辑、管理权限
     */
    public Access access(CurrentUser currentUser, Space space) {
        if (currentUser == null || space == null) {
            return new Access(false, false, false);
        }
        if (currentUser.getRoleId() != null && currentUser.getRoleId() == GlobalConstant.ROLE_ROOT_ID) {
            return new Access(true, true, true);
        }

        SpaceUser membership = this.spaceUserService.findBySpaceIdAndUserId(space.getSpaceId(), currentUser.getUserId());
        if (membership == null) {
            if ("private".equalsIgnoreCase(space.getVisitLevel())) {
                return new Access(false, false, false);
            }
            return new Access(true, false, false);
        }

        if (membership.getPrivilege() != null && membership.getPrivilege() == GlobalConstant.SPACE_MANAGER) {
            return new Access(true, true, true);
        }
        if (membership.getPrivilege() != null && membership.getPrivilege() == GlobalConstant.SPACE_EDITOR) {
            return new Access(true, true, false);
        }
        return new Access(true, false, false);
    }


    /**
     * 判断当前用户是否为 root超级管理员用户
     *
     * @param currentUser 当前登录用户
     * @return true 表示 root 用户，false 表示普通用户或未登录用户
     */
    public static boolean isRoot(CurrentUser currentUser) {
        return currentUser != null && isRootRole(currentUser.getRoleId());
    }

    /**
     * 判断指定角色是否为 root 超级管理员角色
     *
     * @param roleId 角色ID
     * @return true 角色为 root 角色，false 角色非 root 角色
     */
    public static boolean isRootRole(Integer roleId) {
        return roleId != null && roleId == GlobalConstant.ROLE_ROOT_ID;
    }
}
