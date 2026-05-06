package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tinycloud.mmwiki.service.UserDirectoryService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class UserController extends ControllerSupport {

    private final UserDirectoryService userDirectoryService;

    public UserController(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    @GetMapping("/user/index")
    public String index(Model model) {
        nav(model, "user");
        return "user/index";
    }

    @GetMapping("/user/list")
    public String list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        @RequestParam(defaultValue = "") String username,
        HttpServletRequest request,
        Model model
    ) {
        CurrentUser currentUser = currentUser(request);
        UserDirectoryService.UserListPage view = userDirectoryService.listUsers(currentUser, username, page, number);
        model.addAttribute("users", view.users());
        model.addAttribute("count", view.count());
        model.addAttribute("username", view.username());
        model.addAttribute("paginator", view.paginator());
        model.addAttribute("login_user_id", currentUser.getUserId());
        return "user/list";
    }

    @GetMapping("/user/follow")
    public String follow(HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        UserDirectoryService.FollowUserListPage view = userDirectoryService.listFollowedUsers(currentUser.getUserId());
        model.addAttribute("users", view.users());
        model.addAttribute("count", view.count());
        return "user/follow";
    }

    @GetMapping("/user/info")
    public String info(@RequestParam("user_id") Integer userId, HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        if (currentUser.getUserId().equals(userId)) {
            return "redirect:/system/main/index";
        }
        UserDirectoryService.UserProfileView view = userDirectoryService.loadProfile(userId);
        model.addAttribute("user", view.user());
        model.addAttribute("logDocuments", view.logDocuments());
        model.addAttribute("count", view.count());
        return "user/info";
    }

    @GetMapping("/user/followUser")
    public String followUser(@RequestParam("user_id") Integer userId, HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        UserDirectoryService.UserFollowView view = userDirectoryService.loadUserFollowView(userId, currentUser.getUserId());
        model.addAttribute("user", view.user());
        model.addAttribute("users", view.users());
        model.addAttribute("fansUsers", view.fansUsers());
        model.addAttribute("followCount", view.followCount());
        model.addAttribute("fansCount", view.fansCount());
        model.addAttribute("login_user_id", view.loginUserId());
        return "user/follow_user";
    }

    @GetMapping("/user/followPage")
    public String followPage(@RequestParam("user_id") Integer userId, Model model) {
        UserDirectoryService.FollowDocView view = userDirectoryService.loadFollowDocs(userId);
        model.addAttribute("user", view.user());
        model.addAttribute("pages", view.pages());
        model.addAttribute("count", view.count());
        return "user/follow_page";
    }
}
