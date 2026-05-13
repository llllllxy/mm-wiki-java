package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.ConfigEntry;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.ConfigMapper;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class SystemConfigService {

    @Autowired
    private ConfigMapper configMapper;
    @Autowired
    private MmwikiProperties properties;
    @Autowired
    private EmailService emailService;
    @Autowired
    private LoginAuthService loginAuthService;

    public Map<String, String> loadConfigMap() {
        Map<String, String> values = new LinkedHashMap<>();
        for (ConfigEntry entry : configMapper.findAll()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            if (("auto_follow_doc_open".equals(entry.getKey())
                || "send_email_open".equals(entry.getKey())
                || "sso_open".equals(entry.getKey()))
                && !"1".equals(value)) {
                value = "0";
            }
            values.put(entry.getKey(), value);
        }
        values.putIfAbsent("system_version", properties.getVersion());
        return values;
    }

    public JsonResponse<Void> updateGlobal(
        String mainTitle,
        String mainDescription,
        String autoFollowDocOpen,
        String sendEmailOpen,
        String ssoOpen,
        String fulltextSearchOpen,
        String docSearchTimer,
        String systemName
    ) {
        if (!StringUtils.hasText(systemName)) {
            throw new SystemException("系统名称不能为空。");
        }
        if ("1".equals(normalizeSwitch(sendEmailOpen)) && emailService.findUsed() == null) {
            throw new SystemException("开启邮件通知前必须先启用一个邮件服务器配置。");
        }
        if ("1".equals(normalizeSwitch(ssoOpen)) && loginAuthService.findUsed() == null) {
            throw new SystemException("开启统一登录前必须先启用一个登录认证配置。");
        }

        updateIfChanged("main_title", safe(mainTitle));
        updateIfChanged("main_description", safe(mainDescription));
        updateIfChanged("auto_follow_doc_open", normalizeSwitch(autoFollowDocOpen));
        updateIfChanged("send_email_open", normalizeSwitch(sendEmailOpen));
        updateIfChanged("sso_open", normalizeSwitch(ssoOpen));
        updateIfChanged("fulltext_search_open", normalizeSwitch(fulltextSearchOpen));
        updateIfChanged("doc_search_timer", safe(docSearchTimer, "3600"));
        updateIfChanged("system_name", safe(systemName));
        return JsonResponse.success("修改全局配置成功", "/system/config/global");
    }

    private void updateIfChanged(String key, String newValue) {
        String oldValue = configMapper.findValueByKey(key);
        String normalizedOld = oldValue == null ? "" : oldValue;
        if (!normalizedOld.equals(newValue)) {
            configMapper.updateValueByKey(key, newValue);
        }
    }

    private String normalizeSwitch(String value) {
        return "1".equals(value) ? "1" : "0";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String defaultValue) {
        String trimmed = safe(value);
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }
}
