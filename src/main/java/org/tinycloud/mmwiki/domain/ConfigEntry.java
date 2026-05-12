package org.tinycloud.mmwiki.domain;

import java.time.LocalDateTime;

/**
 * 系统配置实体。
 *
 * <p>对应数据库表：mw_config。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class ConfigEntry {

    /**
     * 配置ID
     */
    private Integer configId;
    /**
     * 配置名称
     */
    private String name;
    /**
     * 配置键
     */
    private String key;
    /**
     * 配置值
     */
    private String value;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Integer getConfigId() {
        return configId;
    }

    public void setConfigId(Integer configId) {
        this.configId = configId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

