package org.tinycloud.mmwiki.controller;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.GlobalConstant;
import org.tinycloud.mmwiki.domain.Role;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.service.PrivilegeService;
import org.tinycloud.mmwiki.service.RoleService;
import org.tinycloud.mmwiki.service.UserService;
import org.tinycloud.mmwiki.vo.PrivilegeGroups;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

import java.util.List;


/**
 * 后台角色控制器，负责角色列表、角色维护、授权和角色用户管理。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/role")
public class SystemRoleController extends ControllerSupport {

    @Autowired
    private RoleService roleService;
    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/role/list";
    }

    @PostMapping("/list")
    @ResponseBody
    public JsonResponse<PageModel<Role>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "20") int pageSize,
                                                  @RequestParam(defaultValue = "") String keyword) {
        return JsonResponse.success("查询成功", roleService.pageModel(keyword, pageNum, pageSize));
    }

    @GetMapping("/add")
    public String add() {
        return "system/role/form";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("role_id") Integer roleId, Model model) {
        Role role = roleService.findActiveById(roleId);
        if (role == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在");
        }
        if (roleId == GlobalConstant.ROOT_ROLE_ID) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "超级管理员不能修改");
        }
        model.addAttribute("role", role);
        return "system/role/form";
    }

    @PostMapping("/save")
    @ResponseBody
    public JsonResponse<Void> save(Role role) {
        return roleService.save(role);
    }

    @PostMapping("/modify")
    @ResponseBody
    public JsonResponse<Void> modify(Role role) {
        return roleService.update(role);
    }

    @GetMapping("/user")
    public String user(@RequestParam("role_id") Integer roleId, Model model) {
        Role role = roleService.findActiveById(roleId);
        if (role == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在");
        }
        model.addAttribute("role", role);
        model.addAttribute("roleId", roleId);
        return "system/role/user";
    }

    @PostMapping("/user")
    @ResponseBody
    public JsonResponse<PageModel<org.tinycloud.mmwiki.domain.User>> userData(@RequestParam("role_id") Integer roleId,
                                                                              @RequestParam(defaultValue = "1") int pageNum,
                                                                              @RequestParam(defaultValue = "15") int pageSize) {
        Page<org.tinycloud.mmwiki.domain.User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> userService.pageByFilters("", roleId));
        return JsonResponse.success("查询成功", PageModel.from(pageInfo));
    }

    @GetMapping("/privilege")
    public String privilege(@RequestParam("role_id") Integer roleId, Model model) {
        Role role = roleService.findActiveById(roleId);
        if (role == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "角色不存在");
        }
        PrivilegeGroups groups = privilegeService.groups();
        List<Integer> granted = roleId == GlobalConstant.ROOT_ROLE_ID
                ? groups.getMenus().stream().map(item -> item.getPrivilegeId()).toList()
            : roleService.rolePrivilegeIds(roleId);
        if (roleId == GlobalConstant.ROOT_ROLE_ID) {
            granted = java.util.stream.Stream
                    .concat(groups.getMenus().stream(), groups.getControllers().stream())
                .map(item -> item.getPrivilegeId())
                .toList();
        }
        model.addAttribute("role", role);
        model.addAttribute("menus", groups.getMenus());
        model.addAttribute("controllers", groups.getControllers());
        model.addAttribute("rolePrivileges", granted);
        model.addAttribute("disabledPrivilegeIds", GlobalConstant.DEFAULT_PRIVILEGE_IDS);
        return "system/role/privilege";
    }

    @PostMapping("/grantPrivilege")
    @ResponseBody
    public JsonResponse<Void> grantPrivilege(
        @RequestParam("role_id") Integer roleId,
        @RequestParam(value = "privilege_id", required = false) List<Integer> privilegeIds
    ) {
        return roleService.grantPrivileges(roleId, privilegeIds);
    }

    @PostMapping("/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("role_id") Integer roleId) {
        return roleService.delete(roleId);
    }

    @PostMapping("/resetUser")
    @ResponseBody
    public JsonResponse<Void> resetUser(@RequestParam("user_id") Integer userId) {
        return roleService.resetUserRole(userId);
    }
}
