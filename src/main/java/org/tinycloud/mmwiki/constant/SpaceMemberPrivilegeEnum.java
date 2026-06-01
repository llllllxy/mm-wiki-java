package org.tinycloud.mmwiki.constant;

/**
 * 空间成员权限枚举，对应 mw_space_user.privilege 字段。
 *
 * @author liuxingyu01
 * @since 2026-06-01
 */
public enum SpaceMemberPrivilegeEnum {

    /**
     * 浏览者，可访问空间文档，但不能编辑文档和管理空间成员。
     */
    VISITOR(0),

    /**
     * 编辑者，可访问并编辑空间文档，但不能管理空间成员。
     */
    EDITOR(1),

    /**
     * 管理员，可访问、编辑空间文档，并管理空间成员。
     */
    MANAGER(2);

    private final int code;

    /**
     * 构造空间成员权限枚举，并保存数据库中的权限编码。
     *
     * @param code 数据库 mw_space_user.privilege 字段值
     */
    SpaceMemberPrivilegeEnum(int code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的空间成员权限编码。
     *
     * @return 空间成员权限编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 判断给定空间成员权限编码是否等于当前枚举值。
     *
     * @param privilege 空间成员权限编码
     * @return true 表示权限编码匹配当前枚举，false 表示不匹配
     */
    public boolean is(Integer privilege) {
        return privilege != null && privilege == code;
    }
}
