package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.FollowDocView;
import org.tinycloud.mmwiki.vo.FollowUserListPage;
import org.tinycloud.mmwiki.vo.UserFollowView;
import org.tinycloud.mmwiki.vo.UserProfileView;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.service.UserDirectoryService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

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
            @RequestParam(defaultValue = "") String username,
            Model model
    ) {
        CurrentUser currentUser = currentUser();
        model.addAttribute("username", username == null ? "" : username.trim());
        model.addAttribute("login_user_id", currentUser.getUserId());
        return "user/list";
    }

    @PostMapping("/user/list")
    @ResponseBody
    public JsonResponse<PageModel<User>> listData(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "") String username
    ) {
        return JsonResponse.success("查询成功", userDirectoryService.userPage(currentUser(), username, pageNum, pageSize));
    }

    @GetMapping("/user/follow")
    public String follow(Model model) {
        CurrentUser currentUser = currentUser();
        FollowUserListPage view = userDirectoryService.listFollowedUsers(currentUser.getUserId());
        model.addAttribute("users", view.getUsers());
        model.addAttribute("count", view.getCount());
        return "user/follow";
    }

    @GetMapping("/user/info")
    public String info(@RequestParam("user_id") Integer userId, Model model) {
        CurrentUser currentUser = currentUser();
        if (currentUser.getUserId().equals(userId)) {
            return "redirect:/system/main/index";
        }
        UserProfileView view = userDirectoryService.loadProfile(userId, currentUser);
        model.addAttribute("user", view.getUser());
        model.addAttribute("logDocuments", view.getLogDocuments());
        model.addAttribute("count", view.getCount());
        return "user/info";
    }

    @GetMapping("/user/followUser")
    public String followUser(@RequestParam("user_id") Integer userId, Model model) {
        CurrentUser currentUser = currentUser();
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
        FollowDocView view = userDirectoryService.loadFollowDocs(userId, currentUser());
        model.addAttribute("user", view.getUser());
        model.addAttribute("pages", view.getPages());
        model.addAttribute("count", view.getCount());
        return "user/follow_page";
    }
}
