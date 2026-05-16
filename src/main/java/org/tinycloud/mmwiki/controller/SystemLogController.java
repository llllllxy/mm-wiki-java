package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.service.LogService;
import org.tinycloud.mmwiki.service.UserService;
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
public class SystemLogController extends ControllerSupport {

    @Autowired
    private LogService logService;
    @Autowired
    private UserService userService;

    @GetMapping("/system/log/system")
    public String system(@RequestParam(value = "level", required = false) Integer level,
                         @RequestParam(defaultValue = "") String message,
                         @RequestParam(defaultValue = "") String username,
                         Model model) {
        model.addAttribute("level", level);
        model.addAttribute("message", message == null ? "" : message.trim());
        model.addAttribute("username", username == null ? "" : username.trim());
        return "system/log/system";
    }

    @RequestMapping(value = "/system/log/system/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public JsonResponse<PageModel<LogEntry>> systemList(@RequestParam(value = "level", required = false) Integer level,
                                                        @RequestParam(defaultValue = "") String message,
                                                        @RequestParam(defaultValue = "") String username,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        PageModel<LogEntry> pageModel = logService.systemLogPage(level, message, username, pageNum, pageSize);
        return JsonResponse.success("查询成功", pageModel);
    }

    @GetMapping("/system/log/info")
    public String info(@RequestParam("log_id") Long logId, Model model) {
        LogEntry log = logService.findLog(logId);
        if (log == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "日志不存在");
        }
        model.addAttribute("log", log);
        return "system/log/info";
    }

    @GetMapping("/system/log/document")
    public String document(@RequestParam(value = "user_id", required = false) Integer userId,
                           @RequestParam(defaultValue = "") String keyword,
                           Model model) {
        model.addAttribute("users", userService.findAllActive());
        model.addAttribute("userId", userId);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/log/document";
    }

    @RequestMapping(value = "/system/log/document", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse<PageModel<LogDocumentView>> documentData(@RequestParam(value = "user_id", required = false) Integer userId,
                                                                 @RequestParam(defaultValue = "") String keyword,
                                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "20") int pageSize) {
        return JsonResponse.success("查询成功", logService.documentLogPage(userId, keyword, pageNum, pageSize));
    }
}
