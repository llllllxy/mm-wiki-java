package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.ConfigEntry;
import org.tinycloud.mmwiki.mapper.ConfigMapper;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class ConfigService {

    @Autowired
    private ConfigMapper configMapper;

    /**
     * 获取配置项的值。
     *
     * @param key          配置项的key
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public String getValue(String key, String defaultValue) {
        String value = configMapper.findValueByKey(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 查询系统配置表中的全部配置项。
     *
     * @return 系统配置列表
     */
    public List<ConfigEntry> findAll() {
        return configMapper.findAll();
    }

    /**
     * 根据配置键更新配置值。
     *
     * @param key   配置键
     * @param value 新配置值
     * @return 受影响的记录数
     */
    public int updateValueByKey(String key, String value) {
        return configMapper.updateValueByKey(key, value);
    }
}
