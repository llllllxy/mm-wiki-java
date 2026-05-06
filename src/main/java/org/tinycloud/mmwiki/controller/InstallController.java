package org.tinycloud.mmwiki.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.web.JsonResponse;

@Controller
public class InstallController {

    @GetMapping({"/install", "/install/", "/install/index"})
    public String index(Model model) {
        model.addAttribute("step", "欢迎");
        model.addAttribute("message", "当前 Spring Boot 迁移版已使用 application.yaml 管理配置。若数据库已经导入，可直接启动系统使用。");
        model.addAttribute("nextUrl", "/install/license");
        return "install/simple";
    }

    @GetMapping("/install/license")
    public String license(Model model) {
        model.addAttribute("step", "许可协议");
        model.addAttribute("message", "MM-Wiki 使用 MIT License。迁移版保留原项目协议与数据库结构。");
        model.addAttribute("nextUrl", "/install/env");
        return "install/simple";
    }

    @PostMapping("/install/license")
    @ResponseBody
    public JsonResponse<Void> acceptLicense() {
        return JsonResponse.success("", null, "/install/env", 500);
    }

    @GetMapping("/install/env")
    public String env(Model model) {
        model.addAttribute("step", "环境检测");
        model.addAttribute("message", "Java 21、Spring Boot、MySQL 与 Thymeleaf 环境由当前工程管理；请确认 application.yaml 中数据库与文档目录配置正确。");
        model.addAttribute("nextUrl", "/install/config");
        return "install/simple";
    }

    @PostMapping("/install/env")
    @ResponseBody
    public JsonResponse<Void> acceptEnv() {
        return JsonResponse.success("", null, "/install/config", 500);
    }

    @GetMapping("/install/config")
    public String config(Model model) {
        model.addAttribute("step", "系统配置");
        model.addAttribute("message", "Go 版 .conf 配置已迁移到 Spring Boot application.yaml。请在该文件维护端口、文档目录、数据源和会话配置。");
        model.addAttribute("nextUrl", "/install/database");
        return "install/simple";
    }

    @PostMapping("/install/config")
    @ResponseBody
    public JsonResponse<Void> acceptConfig() {
        return JsonResponse.success("", null, "/install/database", 500);
    }

    @GetMapping("/install/database")
    public String database(Model model) {
        model.addAttribute("step", "数据库配置");
        model.addAttribute("message", "迁移版不在安装页自动覆盖数据库，避免误删现有数据。请手动导入 docs/databases/table.sql 与 data.sql。");
        model.addAttribute("nextUrl", "/install/ready");
        return "install/simple";
    }

    @PostMapping("/install/database")
    @ResponseBody
    public JsonResponse<Void> acceptDatabase() {
        return JsonResponse.success("", null, "/install/ready", 500);
    }

    @GetMapping("/install/ready")
    public String ready(Model model) {
        model.addAttribute("step", "准备完成");
        model.addAttribute("message", "如果数据库和 application.yaml 已配置完成，可以进入系统登录页。");
        model.addAttribute("nextUrl", "/author/index");
        return "install/simple";
    }

    @PostMapping("/install/ready")
    @ResponseBody
    public JsonResponse<Void> acceptReady() {
        return JsonResponse.success("", null, "/author/index", 500);
    }

    @GetMapping({"/install/end", "/install/status"})
    public String end(Model model) {
        model.addAttribute("step", "安装完成");
        model.addAttribute("message", "系统已切换为 Spring Boot 迁移版，请使用初始化管理员账号登录。");
        model.addAttribute("nextUrl", "/author/index");
        return "install/simple";
    }
}
