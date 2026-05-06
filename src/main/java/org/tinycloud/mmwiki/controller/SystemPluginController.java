package org.tinycloud.mmwiki.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.PluginService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemPluginController extends ControllerSupport {

    private final PluginService pluginService;

    public SystemPluginController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @GetMapping("/system/plugin/list")
    public String list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        @RequestParam(defaultValue = "") String keyword,
        Model model
    ) {
        PluginService.PluginPage view = pluginService.list(keyword, page, number);
        model.addAttribute("plugins", view.plugins());
        model.addAttribute("keyword", view.keyword());
        model.addAttribute("paginator", view.paginator());
        return "system/plugin/list";
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
