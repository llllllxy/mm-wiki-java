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

    public List<ConfigEntry> findAll() {
        return configMapper.findAll();
    }

    public int updateValueByKey(String key, String value) {
        return configMapper.updateValueByKey(key, value);
    }
}
