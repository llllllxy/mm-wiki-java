package org.tinycloud.mmwiki.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.ConfigEntry;
import org.tinycloud.mmwiki.mapper.ConfigMapper;

@Service
public class ConfigService {

    private final ConfigMapper configMapper;

    public ConfigService(ConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

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
