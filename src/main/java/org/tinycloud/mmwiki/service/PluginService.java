package org.tinycloud.mmwiki.service;

import org.tinycloud.mmwiki.vo.PluginEntry;

import java.util.List;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class PluginService {

    public PageModel<PluginEntry> pageModel(String keyword, int pageNum, int pageSize) {
        return PageModel.build((long) pageNum, (long) pageSize, List.of(), 0L, 0L);
    }

    public PluginEntry findById(Integer pluginId) {
        return null;
    }

    public JsonResponse<Void> updateConfig(Integer pluginId, String confValue) {
        return JsonResponse.error("当前数据库脚本未包含插件表，插件配置暂不可用", "/system/plugin/list");
    }
}
