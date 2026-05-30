package org.tinycloud.mmwiki.constant;

/**
 * 文档类型枚举，对应 mw_document.type 字段。
 *
 * @author liuxingyu01
 * @since 2026-05-30
 */
public enum DocumentTypeEnum {

    /**
     * 普通页面文档。
     */
    PAGE(1),

    /**
     * 目录文档。
     */
    DIRECTORY(2);

    private final int code;

    /**
     * 构造文档类型枚举，并保存数据库中的文档类型编码。
     *
     * @param code 数据库 mw_document.type 字段值
     */
    DocumentTypeEnum(int code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的文档类型编码。
     *
     * @return 文档类型编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 判断给定文档类型编码是否等于当前枚举值。
     *
     * @param type 文档类型编码
     * @return true 表示编码匹配当前枚举，false 表示不匹配
     */
    public boolean is(Integer type) {
        return type != null && type == code;
    }
}
