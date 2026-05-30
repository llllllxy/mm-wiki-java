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

    /**
     * 加载全局系统配置并补齐运行时默认值。
     *
     * @return 以配置键为键的配置映射
     */
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

    /**
     * 更新全局系统配置，开启邮件或统一登录前会校验对应配置可用。
     *
     * @param mainTitle          首页标题
     * @param mainDescription    首页描述
     * @param autoFollowDocOpen  自动关注文档开关
     * @param sendEmailOpen      邮件通知开关
     * @param ssoOpen            统一登录开关
     * @param fulltextSearchOpen 全文搜索开关
     * @param docSearchTimer     文档索引定时周期
     * @param systemName         系统名称
     * @return 前端统一 JSON 响应
     */
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

    /**
     * 当配置值发生变化时才写入数据库，避免无意义更新。
     *
     * @param key      配置键
     * @param newValue 新配置值
     */
    private void updateIfChanged(String key, String newValue) {
        String oldValue = configMapper.findValueByKey(key);
        String normalizedOld = oldValue == null ? "" : oldValue;
        if (!normalizedOld.equals(newValue)) {
            configMapper.updateValueByKey(key, newValue);
        }
    }

    /**
     * 规范化开关型配置值，只有字符串 1 视为开启。
     *
     * @param value 原始开关值
     * @return 规范化后的 1 或 0
     */
    private String normalizeSwitch(String value) {
        return "1".equals(value) ? "1" : "0";
    }

    /**
     * 安全裁剪字符串，null 会转换为空字符串。
     *
     * @param value 原始字符串
     * @return 裁剪后的字符串
     */
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 安全裁剪字符串，裁剪后为空时返回默认值。
     *
     * @param value        原始字符串
     * @param defaultValue 默认值
     * @return 裁剪后的字符串或默认值
     */
    private String safe(String value, String defaultValue) {
        String trimmed = safe(value);
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }
}
