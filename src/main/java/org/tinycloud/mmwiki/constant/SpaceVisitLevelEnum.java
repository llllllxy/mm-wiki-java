package org.tinycloud.mmwiki.constant;

/**
 * 空间访问级别枚举，对应 mw_space.visit_level 字段。
 *
 * @author liuxingyu01
 * @since 2026-05-30
 */
public enum SpaceVisitLevelEnum {

    /**
     * 公开空间，登录用户可访问。
     */
    PUBLIC("public"),

    /**
     * 私有空间，仅空间成员或 root 可访问。
     */
    PRIVATE("private");

    private final String code;

    /**
     * 构造空间访问级别枚举，并保存数据库中的访问级别值。
     *
     * @param code 数据库 mw_space.visit_level 字段值
     */
    SpaceVisitLevelEnum(String code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的空间访问级别值。
     *
     * @return 空间访问级别值
     */
    public String getCode() {
        return code;
    }

    /**
     * 判断给定访问级别是否等于当前枚举值，忽略大小写。
     *
     * @param visitLevel 空间访问级别值
     * @return true 表示访问级别匹配当前枚举，false 表示不匹配
     */
    public boolean is(String visitLevel) {
        return code.equalsIgnoreCase(visitLevel);
    }

    /**
     * 根据访问级别字符串查找枚举，并兼容前后空格和大小写差异。
     *
     * @param visitLevel 空间访问级别值
     * @return 匹配的访问级别枚举，不匹配时返回 null
     */
    public static SpaceVisitLevelEnum fromCode(String visitLevel) {
        if (visitLevel == null) {
            return null;
        }
        String normalized = visitLevel.trim();
        for (SpaceVisitLevelEnum item : values()) {
            if (item.is(normalized)) {
                return item;
            }
        }
        return null;
    }
}
