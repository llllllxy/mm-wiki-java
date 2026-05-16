package org.tinycloud.mmwiki.constant;

import java.util.List;

/**
 * <p>
 * 通用全局常量
 * </p>
 *
 * @author liuxingyu01
 * @since 2026/5/14 21:59
 */
public final class GlobalConstant {

    /**
     * root 角色ID，root 用户默认拥有所有空间的访问、编辑和管理权限。
     */
    public static final int ROLE_ROOT_ID = 1;

    /**
     * 空间访问者权限值，可访问空间文档，但不能编辑和添加空间成员。
     */
    public static final int SPACE_VISITOR = 0;

    /**
     * 空间编辑者权限值，可访问并编辑空间文档，但不能管理空间成员。
     */
    public static final int SPACE_EDITOR = 1;

    /**
     * 空间管理员权限值，可访问、编辑空间文档，并管理空间成员。
     */
    public static final int SPACE_MANAGER = 2;

    /**
     * root 角色ID，root 超级管理员，默认拥有所有空间访问，编辑和管理权限。
     */
    public static final int ROOT_ROLE_ID = 1;

    /**
     * 角色ID，普通管理员
     */
    public static final int MANAGE_ROLE_ID = 1;

    /**
     * 默认角色ID，系统默认创建的普通用户角色。
     */
    public static final int DEFAULT_ROLE_ID = 3;

    /**
     * 系统角色类型，系统创建的角色。
     */
    public static final int SYSTEM_ROLE_TYPE = 1;

    /**
     * 自定义角色类型，用户自定义的角色。
     */
    public static final int CUSTOM_ROLE_TYPE = 0;

    /**
     * 系统默认的权限ID列表。
     */
    public static final List<Integer> DEFAULT_PRIVILEGE_IDS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
}
