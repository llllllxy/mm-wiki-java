package org.tinycloud.mmwiki.service;

import org.tinycloud.mmwiki.vo.PluginEntry;
import org.tinycloud.mmwiki.vo.PluginPage;

import java.util.List;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class PluginService {

    public PluginPage list(String keyword, int page, int number) {
        String search = keyword == null ? "" : keyword.trim();
        return new PluginPage(List.of(), search, Paginator.of(page, number, 0, "/system/plugin/list?keyword=" + search));
    }

    public PluginEntry findById(Integer pluginId) {
        return null;
    }

    public JsonResponse<Void> updateConfig(Integer pluginId, String confValue) {
        return JsonResponse.error("当前数据库脚本未包含插件表，插件配置暂不可用", "/system/plugin/list");
    }
}
