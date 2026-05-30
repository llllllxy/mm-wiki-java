package org.tinycloud.mmwiki.constant;

/**
 * 附件来源类型枚举，对应 mw_attachment.source 字段。
 *
 * @author liuxingyu01
 * @since 2026-05-30
 */
public enum AttachmentSourceEnum {

    /**
     * 普通附件上传。
     */
    ATTACHMENT(0),

    /**
     * Markdown 编辑器图片上传。
     */
    IMAGE(1);

    private final int code;

    /**
     * 构造附件来源枚举，并保存数据库中的来源编码。
     *
     * @param code 数据库 mw_attachment.source 字段值
     */
    AttachmentSourceEnum(int code) {
        this.code = code;
    }

    /**
     * 获取数据库中保存的附件来源编码。
     *
     * @return 附件来源编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 判断给定来源编码是否等于当前枚举值。
     *
     * @param source 附件来源编码
     * @return true 表示来源编码匹配当前枚举，false 表示不匹配
     */
    public boolean is(Integer source) {
        return source != null && source == code;
    }
}
