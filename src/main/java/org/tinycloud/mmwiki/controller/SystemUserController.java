package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.service.RoleService;
import org.tinycloud.mmwiki.service.UserService;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

@Controller
public class SystemUserController extends ControllerSupport {

    private final UserService userService;
    private final RoleService roleService;

    public SystemUserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/system/user/list")
    public String list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        @RequestParam(defaultValue = "") String username,
        @RequestParam(value = "role_id", required = false) Integer roleId,
        Model model
    ) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String keyword = username == null ? "" : username.trim();

        long count = userService.countByFilters(keyword, roleId);
        List<User> users = userService.findByFilters(keyword, roleId, offset, safeNumber);
        Map<Integer, String> roleNameIndex = roleService.roleNameIndex();

        model.addAttribute("users", users);
        model.addAttribute("username", keyword);
        model.addAttribute("roleId", roleId);
        model.addAttribute("roles", roleService.findAllActive());
        model.addAttribute("roleNameIndex", roleNameIndex);
        model.addAttribute("paginator", Paginator.of(safePage, safeNumber, count, "/system/user/list?username=" + keyword + rolePart(roleId)));
        return "system/user/list";
    }

    @GetMapping("/system/user/info")
    public String info(@RequestParam("user_id") Integer userId, Model model) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在。");
        }
        model.addAttribute("user", user);
        model.addAttribute("roleName", roleService.roleName(user.getRoleId()));
        model.addAttribute("createTimeText", TimeUtils.formatUnix(user.getCreateTime()));
        model.addAttribute("updateTimeText", TimeUtils.formatUnix(user.getUpdateTime()));
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
            return JsonResponse.error("不能屏蔽当前登录用户。", null, "", 2000);
        }
        User user = userService.findActiveById(userId);
        if (user == null) {
            return JsonResponse.error("用户不存在。", null, "", 2000);
        }
        if (RoleService.ROOT_ROLE_ID == (user.getRoleId() == null ? 0 : user.getRoleId())) {
            return JsonResponse.error("不能操作超级管理员。", null, "", 2000);
        }
        userService.updateForbidden(userId, 1);
        return JsonResponse.success("屏蔽用户成功", null, "/system/user/list", 2000);
    }

    @PostMapping("/system/user/recover")
    @ResponseBody
    public JsonResponse<Void> recover(@RequestParam("user_id") Integer userId) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            return JsonResponse.error("用户不存在。", null, "", 2000);
        }
        if (RoleService.ROOT_ROLE_ID == (user.getRoleId() == null ? 0 : user.getRoleId())) {
            return JsonResponse.error("不能操作超级管理员。", null, "", 2000);
        }
        userService.updateForbidden(userId, 0);
        return JsonResponse.success("恢复用户成功", null, "/system/user/list", 2000);
    }

    private String rolePart(Integer roleId) {
        return roleId == null ? "" : "&role_id=" + roleId;
    }

    private List<org.tinycloud.mmwiki.domain.Role> assignableRoles(HttpServletRequest request) {
        boolean root = currentUser(request).getRoleId() != null && currentUser(request).getRoleId() == RoleService.ROOT_ROLE_ID;
        List<org.tinycloud.mmwiki.domain.Role> roles = roleService.findAllActive();
        if (root) {
            return roles;
        }
        return roles.stream().filter(role -> role.getRoleId() != RoleService.ROOT_ROLE_ID).toList();
    }
}
