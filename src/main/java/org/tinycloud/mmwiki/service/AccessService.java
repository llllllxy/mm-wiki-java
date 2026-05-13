package org.tinycloud.mmwiki.service;

import org.tinycloud.mmwiki.vo.Access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.SpaceUser;
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
     * root 角色ID，root 用户默认拥有所有空间的访问、编辑和管理权限。
     */
    public static final int ROLE_ROOT_ID = 1;

    /**
     * 空间编辑者权限值，可访问并编辑空间文档，但不能管理空间成员。
     */
    public static final int SPACE_EDITOR = 1;

    /**
     * 空间管理员权限值，可访问、编辑空间文档，并管理空间成员。
     */
    public static final int SPACE_MANAGER = 2;

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
        if (currentUser.getRoleId() != null && currentUser.getRoleId() == ROLE_ROOT_ID) {
            return new Access(true, true, true);
        }

        SpaceUser membership = this.spaceUserService.findBySpaceIdAndUserId(space.getSpaceId(), currentUser.getUserId());
        if (membership == null) {
            if ("private".equalsIgnoreCase(space.getVisitLevel())) {
                return new Access(false, false, false);
            }
            return new Access(true, false, false);
        }

        if (membership.getPrivilege() != null && membership.getPrivilege() == SPACE_MANAGER) {
            return new Access(true, true, true);
        }
        if (membership.getPrivilege() != null && membership.getPrivilege() == SPACE_EDITOR) {
            return new Access(true, true, false);
        }
        return new Access(true, false, false);
    }
}
