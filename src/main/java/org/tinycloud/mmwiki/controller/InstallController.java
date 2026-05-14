package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.vo.EnvView;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.InstallData;
import org.tinycloud.mmwiki.service.InstallService;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * 安装向导页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class InstallController {

    @Autowired
    private InstallService installService;

    /**
     * 安装欢迎页。
     */
    @GetMapping({"/install", "/install/", "/install/index"})
    public String index(Model model) {
        if (installed(model)) {
            return "install/error";
        }
        return "install/index";
    }

    /**
     * 许可协议页。
     */
    @GetMapping("/install/license")
    public String license(Model model) {
        if (installed(model)) {
            return "install/error";
        }
        model.addAttribute("license", installService.licenseText());
        model.addAttribute("license_agree", installService.data().getLicense());
        return "install/license";
    }

    /**
     * 保存许可协议确认结果。
     */
    @PostMapping("/install/license")
    @ResponseBody
    public JsonResponse<Void> acceptLicense(@RequestParam(value = "license_agree", defaultValue = "0") String agree) {
        if (installService.installed()) {
            throw new SystemException("系统已经安装完成，不能重复安装", "/author/index");
        }
        if (!"1".equals(agree)) {
            throw new SystemException("请先同意协议后再继续");
        }
        installService.data().setLicense(InstallData.LICENSE_AGREE);
        return JsonResponse.success("", "/install/env", 300);
    }

    /**
     * 环境检测页。
     */
    @GetMapping("/install/env")
    public String env(Model model) {
        if (installed(model)) {
            return "install/error";
        }
        EnvView view = installService.envView();
        model.addAttribute("server", view.getServer());
        model.addAttribute("envData", view.getEnvData());
        model.addAttribute("dirData", view.getDirData());
        return "install/env";
    }

    /**
     * 保存环境检测确认。
     */
    @PostMapping("/install/env")
    @ResponseBody
    public JsonResponse<Void> acceptEnv() {
        if (installService.data().getEnv() == InstallData.ENV_NOT_ACCESS) {
            throw new SystemException("环境检测未通过");
        }
        installService.data().setEnv(InstallData.ENV_ACCESS);
        return JsonResponse.success("", "/install/config", 300);
    }

    /**
     * 系统配置页。
     */
    @GetMapping("/install/config")
    public String config(Model model) {
        if (installed(model)) {
            return "install/error";
        }
        model.addAttribute("sysConf", installService.data().getSystemConf());
        return "install/config";
    }

    /**
     * 保存系统配置。
     */
    @PostMapping("/install/config")
    @ResponseBody
    public JsonResponse<Void> acceptConfig(
            @RequestParam("addr") String addr,
            @RequestParam("port") String port,
            @RequestParam("document_dir") String documentDir
    ) {
        String error = installService.saveSystemConfig(addr, port, documentDir);
        if (!error.isBlank()) {
            throw new SystemException(error);
        }
        return JsonResponse.success("", "/install/database", 300);
    }

    /**
     * 数据库配置页。
     */
    @GetMapping("/install/database")
    public String database(Model model) {
        if (installed(model)) {
            return "install/error";
        }
        model.addAttribute("databaseConf", installService.data().getDatabaseConf());
        return "install/database";
    }

    /**
     * 保存数据库配置。
     */
    @PostMapping("/install/database")
    @ResponseBody
    public JsonResponse<Void> acceptDatabase(@RequestParam("host") String host,
                                             @RequestParam("port") String port,
                                             @RequestParam("name") String name,
                                             @RequestParam("user") String user,
                                             @RequestParam("pass") String pass,
                                             @RequestParam("conn_max_idle") String connMaxIdle,
                                             @RequestParam("conn_max_connection") String connMaxConnection,
                                             @RequestParam("admin_name") String adminName,
                                             @RequestParam("admin_pass") String adminPass) {
        Map<String, String> conf = new LinkedHashMap<>();
        conf.put("host", host);
        conf.put("port", port);
        conf.put("name", name);
        conf.put("user", user);
        conf.put("pass", pass);
        conf.put("conn_max_idle", connMaxIdle);
        conf.put("conn_max_connection", connMaxConnection);
        conf.put("admin_name", adminName);
        conf.put("admin_pass", adminPass);
        String error = installService.saveDatabaseConfig(conf);
        if (!error.isBlank()) {
            throw new SystemException(error);
        }
        return JsonResponse.success("", "/install/ready", 300);
    }

    /**
     * 安装准备页。
     */
    @GetMapping("/install/ready")
    public String ready(Model model) {
        if (installed(model)) {
            return "install/error";
        }
        InstallData data = installService.data();
        model.addAttribute("readyConf", readyConf(data));
        return "install/ready";
    }

    /**
     * 开始异步安装。
     */
    @PostMapping("/install/ready")
    @ResponseBody
    public JsonResponse<Void> acceptReady() {
        String error = installService.startInstall();
        if (!error.isBlank()) {
            throw new SystemException(error);
        }
        return JsonResponse.success("", "/install/end", 300);
    }

    /**
     * 安装结果页。
     */
    @GetMapping("/install/end")
    public String end() {
        if (installService.data().getStatus() == InstallData.INSTALL_READY && !installService.installed()) {
            return "redirect:/install/ready";
        }
        return "install/end";
    }

    /**
     * 安装状态轮询接口。
     */
    @PostMapping("/install/status")
    @ResponseBody
    public JsonResponse<Map<String, Object>> status() {
        InstallData data = installService.data();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", data.getStatus());
        status.put("is_success", data.getIsSuccess());
        status.put("result", data.getResult());
        return JsonResponse.success("ok", status, "", 300);
    }

    private boolean installed(Model model) {
        if (!installService.installed()) {
            return false;
        }
        model.addAttribute("message", "系统已经安装完成，不能重复安装。如需重新安装，请先停止服务、备份数据，并手动删除 install.lock。");
        model.addAttribute("redirect", "/author/index");
        model.addAttribute("sleep", 3000);
        return true;
    }

    private java.util.List<Map<String, Object>> readyConf(InstallData data) {
        return java.util.List.of(
                readyRow("许可协议", data.getLicense() == InstallData.LICENSE_AGREE ? "同意" : "未同意", data.getLicense() == InstallData.LICENSE_AGREE, "/install/license"),
                readyRow("环境检测", data.getEnv() == InstallData.ENV_ACCESS ? "通过" : "未通过", data.getEnv() == InstallData.ENV_ACCESS, "/install/env"),
                readyRow("系统配置", data.getSystem() == InstallData.SYS_ACCESS ? "完成" : "未完成", data.getSystem() == InstallData.SYS_ACCESS, "/install/config"),
                readyRow("数据库配置", data.getDatabase() == InstallData.DATABASE_ACCESS ? "完成" : "未完成", data.getDatabase() == InstallData.DATABASE_ACCESS, "/install/database")
        );
    }

    private Map<String, Object> readyRow(String name, String value, boolean ok, String url) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("value", value);
        row.put("result", ok ? "1" : "0");
        row.put("url", url);
        return row;
    }
}
