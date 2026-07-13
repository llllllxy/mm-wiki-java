package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.service.SystemProfileService;
import org.tinycloud.mmwiki.vo.FollowDocPage;
import org.tinycloud.mmwiki.vo.ProfileFollowedDocument;
import org.tinycloud.mmwiki.vo.ProfileInfoView;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * 个人中心控制器，负责个人资料、密码、关注用户、关注文档和个人动态页面。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/profile")
public class SystemProfileController extends ControllerSupport {

    @Autowired
    private SystemProfileService systemProfileService;

    @GetMapping("/info")
    public String info(Model model) {
        CurrentUser currentUser = this.currentUser();
        ProfileInfoView view = this.systemProfileService.loadInfo(currentUser.getUserId());
        model.addAttribute("user", view.getUser());
        model.addAttribute("logDocuments", view.getLogDocuments());
        model.addAttribute("count", view.getCount());
        return "system/profile/info";
    }

    @GetMapping("/edit")
    public String edit(Model model) {
        model.addAttribute("user", this.systemProfileService.loadEditableProfile(this.currentUser().getUserId()));
        return "system/profile/edit";
    }

    @PostMapping("/modify")
    @ResponseBody
    public JsonResponse<Void> modify(
            @RequestParam("given_name") String givenName,
            @RequestParam("email") String email,
            @RequestParam("mobile") String mobile,
            @RequestParam(value = "phone", defaultValue = "") String phone,
            @RequestParam(value = "department", defaultValue = "") String department,
            @RequestParam(value = "position", defaultValue = "") String position,
            @RequestParam(value = "location", defaultValue = "") String location,
            @RequestParam(value = "im", defaultValue = "") String im
    ) {
        return this.systemProfileService.modifyProfile(
                this.currentUser().getUserId(),
                givenName,
                email,
                mobile,
                phone,
                department,
                position,
                location,
                im
        );
    }

    @GetMapping("/followUser")
    public String followUser(Model model) {
        model.addAttribute("user", this.systemProfileService.loadEditableProfile(this.currentUser().getUserId()));
        return "system/profile/follow_user";
    }

    /**
     * 异步分页加载当前用户的关注用户或粉丝列表。
     *
     * @param relation 列表关系，follow 表示关注，fans 表示粉丝
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 关注用户或粉丝分页数据
     */
    @PostMapping("/followUser")
    @ResponseBody
    public JsonResponse<PageModel<User>> followUserData(@RequestParam(defaultValue = "follow") String relation,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        Integer userId = this.currentUser().getUserId();
        PageModel<User> pageModel = "fans".equalsIgnoreCase(relation)
                ? this.systemProfileService.loadFansUserPage(userId, pageNum, pageSize)
                : this.systemProfileService.loadFollowedUserPage(userId, pageNum, pageSize);
        return JsonResponse.success("查询成功", pageModel);
    }

    @GetMapping("/followDoc")
    public String followDoc(Model model) {
        FollowDocPage view = this.systemProfileService.loadFollowDocs(this.currentUser().getUserId());
        model.addAttribute("user", view.getUser());
        model.addAttribute("autoFollowDoc", view.getAutoFollowDoc());
        return "system/profile/follow_doc";
    }

    @PostMapping("/followDoc")
    @ResponseBody
    public JsonResponse<PageModel<ProfileFollowedDocument>> followDocData(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return JsonResponse.success("查询成功", this.systemProfileService.loadFollowDocPage(this.currentUser(), pageNum, pageSize));
    }

    @GetMapping("/activity")
    public String activity(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/profile/activity";
    }

    @PostMapping("/activity")
    @ResponseBody
    public JsonResponse<PageModel<LogDocumentView>> activityData(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(defaultValue = "") String keyword
    ) {
        return JsonResponse.success("查询成功", this.systemProfileService.loadActivityPage(this.currentUser().getUserId(), keyword, pageNum, pageSize));
    }

    @GetMapping("/password")
    public String password() {
        return "system/profile/password";
    }

    @PostMapping("/savePass")
    @ResponseBody
    public JsonResponse<Void> savePass(
            @RequestParam("pwd") String password,
            @RequestParam("pwd_new") String passwordNew,
            @RequestParam("pwd_confirm") String passwordConfirm
    ) {
        return this.systemProfileService.savePassword(this.currentUser().getUserId(), password, passwordNew, passwordConfirm);
    }
}
