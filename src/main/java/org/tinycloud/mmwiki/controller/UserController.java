package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.service.UserDirectoryService;
import org.tinycloud.mmwiki.vo.UserFollowedDocument;
import org.tinycloud.mmwiki.vo.UserProfileView;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * 前台用户控制器，负责用户列表、用户主页、关注列表和个人入口跳转。
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
        this.nav(model, "user");
        return "user/index";
    }

    @GetMapping("/user/list")
    public String list(@RequestParam(defaultValue = "") String username,
                       Model model) {
        CurrentUser currentUser = this.currentUser();
        model.addAttribute("username", username == null ? "" : username.trim());
        model.addAttribute("login_user_id", currentUser.getUserId());
        return "user/list";
    }

    @PostMapping("/user/list")
    @ResponseBody
    public JsonResponse<PageModel<User>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "20") int pageSize,
                                                  @RequestParam(defaultValue = "") String username) {
        return JsonResponse.success("查询成功", this.userDirectoryService.userPage(this.currentUser(), username, pageNum, pageSize));
    }

    @GetMapping("/user/follow")
    public String follow() {
        return "user/follow";
    }

    /**
     * 异步分页加载当前登录用户关注的用户列表。
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 关注用户分页数据
     */
    @PostMapping("/user/follow")
    @ResponseBody
    public JsonResponse<PageModel<User>> followData(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return JsonResponse.success("查询成功", this.userDirectoryService.followedUserPage(this.currentUser().getUserId(), pageNum, pageSize));
    }

    @GetMapping("/user/info")
    public String info(@RequestParam("user_id") Integer userId, Model model) {
        CurrentUser currentUser = this.currentUser();
        if (currentUser.getUserId().equals(userId)) {
            return "redirect:/system/main/index";
        }
        UserProfileView view = this.userDirectoryService.loadProfile(userId, currentUser);
        model.addAttribute("user", view.getUser());
        model.addAttribute("logDocuments", view.getLogDocuments());
        model.addAttribute("count", view.getCount());
        return "user/info";
    }

    @GetMapping("/user/followUser")
    public String followUser(@RequestParam("user_id") Integer userId, Model model) {
        CurrentUser currentUser = this.currentUser();
        model.addAttribute("user", this.userDirectoryService.loadActiveUser(userId));
        model.addAttribute("login_user_id", currentUser.getUserId());
        return "user/follow_user";
    }

    /**
     * 异步分页加载指定用户的关注用户或粉丝列表。
     *
     * @param userId   被访问用户ID
     * @param relation 列表关系，follow 表示关注，fans 表示粉丝
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 用户关注或粉丝分页数据
     */
    @PostMapping("/user/followUser")
    @ResponseBody
    public JsonResponse<PageModel<User>> followUserData(@RequestParam("user_id") Integer userId,
                                                        @RequestParam(defaultValue = "follow") String relation,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        PageModel<User> pageModel = "fans".equalsIgnoreCase(relation)
                ? this.userDirectoryService.profileFansUserPage(userId, pageNum, pageSize)
                : this.userDirectoryService.profileFollowedUserPage(userId, pageNum, pageSize);
        return JsonResponse.success("查询成功", pageModel);
    }

    @GetMapping("/user/followPage")
    public String followPage(@RequestParam("user_id") Integer userId, Model model) {
        CurrentUser currentUser = this.currentUser();
        model.addAttribute("user", this.userDirectoryService.loadActiveUser(userId));
        model.addAttribute("login_user_id", currentUser.getUserId());
        return "user/follow_page";
    }

    /**
     * 异步分页加载指定用户关注的文档列表，并按当前登录用户权限过滤不可见文档。
     *
     * @param userId   被访问用户ID
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 关注文档分页数据
     */
    @PostMapping("/user/followPage")
    @ResponseBody
    public JsonResponse<PageModel<UserFollowedDocument>> followPageData(@RequestParam("user_id") Integer userId,
                                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return JsonResponse.success("查询成功", this.userDirectoryService.followDocPage(userId, this.currentUser(), pageNum, pageSize));
    }
}
