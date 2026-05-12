package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.PluginService;
import org.tinycloud.mmwiki.vo.PluginEntry;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemPluginController extends ControllerSupport {

    @Autowired
    private PluginService pluginService;

    @GetMapping("/system/plugin/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/plugin/list";
    }

    @PostMapping("/system/plugin/list")
    @ResponseBody
    public JsonResponse<PageModel<PluginEntry>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "20") int pageSize,
                                                         @RequestParam(defaultValue = "") String keyword) {
        return JsonResponse.success("查询成功", pluginService.pageModel(keyword, pageNum, pageSize));
    }

    @GetMapping("/system/plugin/config")
    public String config(@RequestParam("plugin_id") Integer pluginId, Model model) {
        model.addAttribute("pluginId", pluginId);
        return "system/plugin/config";
    }

    @PostMapping("/system/plugin/configModify")
    @ResponseBody
    public JsonResponse<Void> configModify(
        @RequestParam("plugin_id") Integer pluginId,
        @RequestParam(defaultValue = "") String confValue
    ) {
        return pluginService.updateConfig(pluginId, confValue);
    }
}
