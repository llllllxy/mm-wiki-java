package org.tinycloud.mmwiki.constant;

/**
 * 收藏类型枚举，对应 mw_collection.type 字段。
 *
 * @author liuxingyu01
 * @since 2026-05-30
 */
public enum CollectionTypeEnum {

    /**
     * 收藏文档。
     */
    DOCUMENT(1),

    /**
     * 收藏空间。
     */
    SPACE(2);

    private final int code;

    /**
     * 构造收藏类型枚举，并保存数据库中的收藏类型编码。
     *
     * @param code 数据库 mw_collection.type 字段值
     */
    CollectionTypeEnum(int code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的收藏类型编码。
     *
     * @return 收藏类型编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 判断给定收藏类型编码是否等于当前枚举值。
     *
     * @param type 收藏类型编码
     * @return true 表示编码匹配当前枚举，false 表示不匹配
     */
    public boolean is(Integer type) {
        return type != null && type == code;
    }
}
