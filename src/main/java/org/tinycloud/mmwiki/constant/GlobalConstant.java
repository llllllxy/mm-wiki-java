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
     * 登录用户在 HTTP Session 中保存的属性名。
     */
    public static final String SESSION_AUTHOR = "author";

    /**
     * session 中账号状态的最短刷新间隔，避免高频请求每次都查询用户表。目前默认是30秒
     */
    public static final long USER_STATUS_REFRESH_INTERVAL_MILLIS = 30_000L;

    /**
     * root 角色ID（超级管理员），默认拥有所有功能权限和空间访问，编辑和管理权限。
     */
    public static final int ROOT_ROLE_ID = 1;

    /**
     * 普通管理员角色ID，管理员角色，默认拥有空间管理权限。
     */
    public static final int MANAGE_ROLE_ID = 2;

    /**
     * 默认角色ID（普通用户），系统默认创建的普通用户角色。
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
