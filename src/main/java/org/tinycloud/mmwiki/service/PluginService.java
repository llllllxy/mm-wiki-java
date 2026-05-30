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

    /**
     * 查询插件分页数据；当前数据库脚本未启用插件表，因此返回空分页。
     *
     * @param keyword  查询关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 空插件分页数据
     */
    public PageModel<PluginEntry> pageModel(String keyword, int pageNum, int pageSize) {
        return PageModel.build((long) pageNum, (long) pageSize, List.of(), 0L, 0L);
    }

    /**
     * 根据插件ID查询插件；当前插件表未启用，因此始终返回 null。
     *
     * @param pluginId 插件ID
     * @return 当前实现固定返回 null
     */
    public PluginEntry findById(Integer pluginId) {
        return null;
    }

    /**
     * 更新插件配置；当前插件表未启用，因此返回不可用提示。
     *
     * @param pluginId  插件ID
     * @param confValue 插件配置内容
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> updateConfig(Integer pluginId, String confValue) {
        return JsonResponse.error("当前数据库脚本未包含插件表，插件配置暂不可用", "/system/plugin/list");
    }
}
