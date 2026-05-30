package org.tinycloud.mmwiki.constant;

/**
 * 关注类型枚举，对应 mw_follow.type 字段。
 *
 * @author liuxingyu01
 * @since 2026-05-30
 */
public enum FollowTypeEnum {

    /**
     * 关注文档。
     */
    DOCUMENT(1),

    /**
     * 关注用户。
     */
    USER(2);

    private final int code;

    /**
     * 构造关注类型枚举，并保存数据库中的关注类型编码。
     *
     * @param code 数据库 mw_follow.type 字段值
     */
    FollowTypeEnum(int code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的关注类型编码。
     *
     * @return 关注类型编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 判断给定关注类型编码是否等于当前枚举值。
     *
     * @param type 关注类型编码
     * @return true 表示编码匹配当前枚举，false 表示不匹配
     */
    public boolean is(Integer type) {
        return type != null && type == code;
    }
}
