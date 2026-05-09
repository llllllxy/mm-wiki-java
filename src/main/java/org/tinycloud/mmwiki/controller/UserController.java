package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.FollowDocView;
import org.tinycloud.mmwiki.vo.FollowUserListPage;
import org.tinycloud.mmwiki.vo.UserFollowView;
import org.tinycloud.mmwiki.vo.UserListPage;
import org.tinycloud.mmwiki.vo.UserProfileView;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserDirectoryService userDirectoryService;

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
        UserListPage view = userDirectoryService.listUsers(currentUser, username, page, number);
        model.addAttribute("users", view.getUsers());
        model.addAttribute("count", view.getCount());
        model.addAttribute("username", view.getUsername());
        model.addAttribute("paginator", view.getPaginator());
        model.addAttribute("login_user_id", currentUser.getUserId());
        return "user/list";
    }

    @GetMapping("/user/follow")
    public String follow(HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        FollowUserListPage view = userDirectoryService.listFollowedUsers(currentUser.getUserId());
        model.addAttribute("users", view.getUsers());
        model.addAttribute("count", view.getCount());
        return "user/follow";
    }

    @GetMapping("/user/info")
    public String info(@RequestParam("user_id") Integer userId, HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        if (currentUser.getUserId().equals(userId)) {
            return "redirect:/system/main/index";
        }
        UserProfileView view = userDirectoryService.loadProfile(userId);
        model.addAttribute("user", view.getUser());
        model.addAttribute("logDocuments", view.getLogDocuments());
        model.addAttribute("count", view.getCount());
        return "user/info";
    }

    @GetMapping("/user/followUser")
    public String followUser(@RequestParam("user_id") Integer userId, HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        UserFollowView view = userDirectoryService.loadUserFollowView(userId, currentUser.getUserId());
        model.addAttribute("user", view.getUser());
        model.addAttribute("users", view.getUsers());
        model.addAttribute("fansUsers", view.getFansUsers());
        model.addAttribute("followCount", view.getFollowCount());
        model.addAttribute("fansCount", view.getFansCount());
        model.addAttribute("login_user_id", view.getLoginUserId());
        return "user/follow_user";
    }

    @GetMapping("/user/followPage")
    public String followPage(@RequestParam("user_id") Integer userId, Model model) {
        FollowDocView view = userDirectoryService.loadFollowDocs(userId);
        model.addAttribute("user", view.getUser());
        model.addAttribute("pages", view.getPages());
        model.addAttribute("count", view.getCount());
        return "user/follow_page";
    }
}
