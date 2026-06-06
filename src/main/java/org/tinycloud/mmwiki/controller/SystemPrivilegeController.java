package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.vo.PrivilegeGroups;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Privilege;
import org.tinycloud.mmwiki.service.PrivilegeService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * 后台权限控制器，负责权限列表、权限表单、保存、修改和删除。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/privilege")
public class SystemPrivilegeController extends ControllerSupport {

    @Autowired
    private PrivilegeService privilegeService;

    @GetMapping("/list")
    public String list(Model model) {
        PrivilegeGroups groups = privilegeService.groups();
        model.addAttribute("menus", groups.getMenus());
        model.addAttribute("controllers", groups.getControllers());
        model.addAttribute("mode", privilegeService.mode());
        return "system/privilege/list";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("menus", privilegeService.groups().getMenus());
        model.addAttribute("mode", privilegeService.mode());
        return "system/privilege/form";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("privilege_id") Integer privilegeId, Model model) {
        Privilege privilege = privilegeService.findById(privilegeId);
        if (privilege == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "权限不存在");
        }
        model.addAttribute("privilege", privilege);
        model.addAttribute("menus", privilegeService.groups().getMenus());
        model.addAttribute("mode", privilegeService.mode());
        return "system/privilege/form";
    }

    @PostMapping("/save")
    @ResponseBody
    public JsonResponse<Void> save(Privilege privilege) {
        return privilegeService.save(privilege);
    }

    @PostMapping("/modify")
    @ResponseBody
    public JsonResponse<Void> modify(Privilege privilege) {
        return privilegeService.update(privilege);
    }

    @PostMapping("/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("privilege_id") Integer privilegeId) {
        return privilegeService.delete(privilegeId);
    }
}
