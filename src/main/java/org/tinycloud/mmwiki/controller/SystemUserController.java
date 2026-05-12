package org.tinycloud.mmwiki.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Role;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.service.RoleService;
import org.tinycloud.mmwiki.service.UserService;
import org.tinycloud.mmwiki.util.TimeUtils;
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
public class SystemUserController extends ControllerSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @GetMapping("/system/user/list")
    public String list(@RequestParam(defaultValue = "") String username,
                       @RequestParam(value = "role_id", required = false) Integer roleId,
                       Model model) {
        model.addAttribute("username", username == null ? "" : username.trim());
        model.addAttribute("roleId", roleId);
        model.addAttribute("roles", roleService.findAllActive());
        model.addAttribute("roleNameIndex", roleService.roleNameIndex());
        return "system/user/list";
    }

    @PostMapping("/system/user/list")
    @ResponseBody
    public JsonResponse<PageModel<User>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "20") int pageSize,
                                                  @RequestParam(defaultValue = "") String username,
                                                  @RequestParam(value = "role_id", required = false) Integer roleId) {
        String keyword = username == null ? "" : username.trim();
        PageInfo<User> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> userService.pageByFilters(keyword, roleId));
        return JsonResponse.success("查询成功", PageModel.from(pageInfo));
    }

    @GetMapping("/system/user/info")
    public String info(@RequestParam("user_id") Integer userId, Model model) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在。");
        }
        model.addAttribute("user", user);
        model.addAttribute("roleName", roleService.roleName(user.getRoleId()));
        model.addAttribute("createTimeText", TimeUtils.format(user.getCreateTime()));
        model.addAttribute("updateTimeText", TimeUtils.format(user.getUpdateTime()));
        model.addAttribute("lastTimeText", TimeUtils.formatUnix(user.getLastTime()));
        return "system/user/info";
    }

    @GetMapping("/system/user/add")
    public String add(HttpServletRequest request, Model model) {
        model.addAttribute("roles", assignableRoles(request));
        return "system/user/form";
    }

    @PostMapping("/system/user/save")
    @ResponseBody
    public JsonResponse<Void> save(HttpServletRequest request, User user) {
        return userService.saveSystemUser(user, currentUser(request));
    }

    @GetMapping("/system/user/edit")
    public String edit(@RequestParam("user_id") Integer userId, HttpServletRequest request, Model model) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在！");
        }
        if (user.getRoleId() != null && user.getRoleId() == RoleService.ROOT_ROLE_ID
                && currentUser(request).getRoleId() != RoleService.ROOT_ROLE_ID) {
            throw new IllegalStateException("没有权限修改超级管理员！");
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", assignableRoles(request));
        model.addAttribute("canChangePassword", currentUser(request).getRoleId() == RoleService.ROOT_ROLE_ID);
        return "system/user/edit";
    }

    @PostMapping("/system/user/modify")
    @ResponseBody
    public JsonResponse<Void> modify(HttpServletRequest request, User user) {
        return userService.updateSystemUser(user, currentUser(request));
    }

    @PostMapping("/system/user/forbidden")
    @ResponseBody
    public JsonResponse<Void> forbidden(HttpServletRequest request, @RequestParam("user_id") Integer userId) {
        if (currentUser(request).getUserId().equals(userId)) {
            return JsonResponse.error("不能屏蔽当前登录用户。");
        }
        User user = userService.findActiveById(userId);
        if (user == null) {
            return JsonResponse.error("用户不存在。");
        }
        if (RoleService.ROOT_ROLE_ID == (user.getRoleId() == null ? 0 : user.getRoleId())) {
            return JsonResponse.error("不能操作超级管理员。");
        }
        userService.updateForbidden(userId, 1);
        return JsonResponse.success("屏蔽用户成功", "/system/user/list");
    }

    @PostMapping("/system/user/recover")
    @ResponseBody
    public JsonResponse<Void> recover(@RequestParam("user_id") Integer userId) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            return JsonResponse.error("用户不存在。");
        }
        if (RoleService.ROOT_ROLE_ID == (user.getRoleId() == null ? 0 : user.getRoleId())) {
            return JsonResponse.error("不能操作超级管理员。");
        }
        userService.updateForbidden(userId, 0);
        return JsonResponse.success("恢复用户成功", "/system/user/list");
    }

    private List<Role> assignableRoles(HttpServletRequest request) {
        boolean root = currentUser(request).getRoleId() != null && currentUser(request).getRoleId() == RoleService.ROOT_ROLE_ID;
        List<Role> roles = roleService.findAllActive();
        if (root) {
            return roles;
        }
        return roles.stream().filter(role -> role.getRoleId() != RoleService.ROOT_ROLE_ID).toList();
    }
}
