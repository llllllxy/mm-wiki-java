package org.tinycloud.mmwiki.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.tinycloud.mmwiki.vo.PrivilegeGroups;
import org.tinycloud.mmwiki.vo.RolePage;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Role;
import org.tinycloud.mmwiki.service.PrivilegeService;
import org.tinycloud.mmwiki.service.RoleService;
import org.tinycloud.mmwiki.service.UserService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemRoleController extends ControllerSupport {

    @Autowired
    private RoleService roleService;
    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private UserService userService;

    @GetMapping("/system/role/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/role/list";
    }

    @PostMapping("/system/role/list")
    @ResponseBody
    public JsonResponse<PageModel<Role>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "20") int pageSize,
                                                  @RequestParam(defaultValue = "") String keyword) {
        return JsonResponse.success("查询成功", roleService.pageModel(keyword, pageNum, pageSize));
    }

    @GetMapping("/system/role/add")
    public String add() {
        return "system/role/form";
    }

    @GetMapping("/system/role/edit")
    public String edit(@RequestParam("role_id") Integer roleId, Model model) {
        Role role = roleService.findActiveById(roleId);
        if (role == null || roleId == RoleService.ROOT_ROLE_ID) {
            throw new IllegalStateException("角色不存在或不可修改");
        }
        model.addAttribute("role", role);
        return "system/role/form";
    }

    @PostMapping("/system/role/save")
    @ResponseBody
    public JsonResponse<Void> save(Role role) {
        return roleService.save(role);
    }

    @PostMapping("/system/role/modify")
    @ResponseBody
    public JsonResponse<Void> modify(Role role) {
        return roleService.update(role);
    }

    @GetMapping("/system/role/user")
    public String user(@RequestParam("role_id") Integer roleId, Model model) {
        Role role = roleService.findActiveById(roleId);
        if (role == null) {
            throw new IllegalStateException("角色不存在");
        }
        model.addAttribute("role", role);
        model.addAttribute("roleId", roleId);
        return "system/role/user";
    }

    @PostMapping("/system/role/user")
    @ResponseBody
    public JsonResponse<PageModel<org.tinycloud.mmwiki.domain.User>> userData(@RequestParam("role_id") Integer roleId,
                                                                              @RequestParam(defaultValue = "1") int pageNum,
                                                                              @RequestParam(defaultValue = "15") int pageSize) {
        PageInfo<org.tinycloud.mmwiki.domain.User> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> userService.pageByFilters("", roleId));
        return JsonResponse.success("查询成功", PageModel.from(pageInfo));
    }

    @GetMapping("/system/role/privilege")
    public String privilege(@RequestParam("role_id") Integer roleId, Model model) {
        Role role = roleService.findActiveById(roleId);
        if (role == null) {
            throw new IllegalStateException("角色不存在");
        }
        PrivilegeGroups groups = privilegeService.groups();
        List<Integer> granted = roleId == RoleService.ROOT_ROLE_ID
                ? groups.getMenus().stream().map(item -> item.getPrivilegeId()).toList()
            : roleService.rolePrivilegeIds(roleId);
        if (roleId == RoleService.ROOT_ROLE_ID) {
            granted = java.util.stream.Stream
                    .concat(groups.getMenus().stream(), groups.getControllers().stream())
                .map(item -> item.getPrivilegeId())
                .toList();
        }
        model.addAttribute("role", role);
        model.addAttribute("menus", groups.getMenus());
        model.addAttribute("controllers", groups.getControllers());
        model.addAttribute("rolePrivileges", granted);
        model.addAttribute("disabledPrivilegeIds", RoleService.DEFAULT_PRIVILEGE_IDS);
        return "system/role/privilege";
    }

    @PostMapping("/system/role/grantPrivilege")
    @ResponseBody
    public JsonResponse<Void> grantPrivilege(
        @RequestParam("role_id") Integer roleId,
        @RequestParam(value = "privilege_id", required = false) List<Integer> privilegeIds
    ) {
        return roleService.grantPrivileges(roleId, privilegeIds);
    }

    @PostMapping("/system/role/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("role_id") Integer roleId) {
        return roleService.delete(roleId);
    }

    @PostMapping("/system/role/resetUser")
    @ResponseBody
    public JsonResponse<Void> resetUser(@RequestParam("user_id") Integer userId) {
        return roleService.resetUserRole(userId);
    }
}
