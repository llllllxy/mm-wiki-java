package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.Dashboard;
import org.tinycloud.mmwiki.vo.Monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.StaticService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemStaticController extends ControllerSupport {

    @Autowired
    private StaticService staticService;

    @GetMapping("/system/static/default")
    public String defaultPage(Model model) {
        Dashboard view = staticService.dashboard();
        model.addAttribute("normalUserCount", view.getNormalUserCount());
        model.addAttribute("forbiddenUserCount", view.getForbiddenUserCount());
        model.addAttribute("spaceCount", view.getSpaceCount());
        model.addAttribute("documentCount", view.getDocumentCount());
        model.addAttribute("todayLoginUserCount", view.getTodayLoginUserCount());
        model.addAttribute("createMaxUser", view.getCreateMaxUser());
        model.addAttribute("editMaxUser", view.getEditMaxUser());
        model.addAttribute("collectMaxUser", view.getCollectMaxUser());
        model.addAttribute("fansMaxUser", view.getFansMaxUser());
        return "system/static/default";
    }

    @PostMapping("/system/static/spaceDocsRank")
    @ResponseBody
    public JsonResponse<?> spaceDocsRank(@RequestParam(defaultValue = "15") int number) {
        return JsonResponse.success("ok", staticService.spaceDocsRank(number), "", 0);
    }

    @PostMapping("/system/static/collectDocRank")
    @ResponseBody
    public JsonResponse<?> collectDocRank(@RequestParam(defaultValue = "10") int number) {
        return JsonResponse.success("ok", staticService.collectDocRank(number), "", 0);
    }

    @PostMapping("/system/static/docCountByTime")
    @ResponseBody
    public JsonResponse<?> docCountByTime(@RequestParam(value = "limit_day", defaultValue = "10") int limitDay) {
        return JsonResponse.success("ok", staticService.docCountByTime(limitDay), "", 0);
    }

    @GetMapping("/system/static/monitor")
    public String monitor(Model model) {
        Monitor view = staticService.monitor();
        model.addAttribute("serverInfo", view.getServerInfo());
        model.addAttribute("errorLogCount", view.getErrorLogCount());
        model.addAttribute("errLogs", view.getErrLogs());
        return "system/static/monitor";
    }

    @PostMapping("/system/static/serverStatus")
    @ResponseBody
    public JsonResponse<?> serverStatus() {
        return JsonResponse.success("ok", staticService.serverStatus(), "", 0);
    }

    @PostMapping("/system/static/serverTime")
    @ResponseBody
    public JsonResponse<?> serverTime() {
        return JsonResponse.success("ok", staticService.serverTime(), "", 0);
    }
}
