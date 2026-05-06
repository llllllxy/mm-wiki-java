package org.tinycloud.mmwiki.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.service.LogService;
import org.tinycloud.mmwiki.web.ControllerSupport;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemLogController extends ControllerSupport {

    private final LogService logService;

    public SystemLogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/system/log/system")
    public String system(
        @RequestParam(value = "level", required = false) Integer level,
        @RequestParam(defaultValue = "") String message,
        @RequestParam(defaultValue = "") String username,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        Model model
    ) {
        LogService.SystemLogPage view = logService.systemLogs(level, message, username, page, number);
        model.addAttribute("logs", view.logs());
        model.addAttribute("level", view.level());
        model.addAttribute("message", view.message());
        model.addAttribute("username", view.username());
        model.addAttribute("paginator", view.paginator());
        return "system/log/system";
    }

    @GetMapping("/system/log/info")
    public String info(@RequestParam("log_id") Long logId, Model model) {
        LogEntry log = logService.findLog(logId);
        if (log == null) {
            throw new IllegalStateException("日志不存在");
        }
        model.addAttribute("log", log);
        return "system/log/info";
    }

    @GetMapping("/system/log/document")
    public String document(
        @RequestParam(value = "user_id", required = false) Integer userId,
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        Model model
    ) {
        LogService.DocumentLogPage view = logService.documentLogs(userId, keyword, page, number);
        model.addAttribute("logDocuments", view.logDocuments());
        model.addAttribute("users", view.users());
        model.addAttribute("userId", view.userId());
        model.addAttribute("keyword", view.keyword());
        model.addAttribute("paginator", view.paginator());
        return "system/log/document";
    }
}
