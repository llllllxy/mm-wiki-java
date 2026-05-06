package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.SystemProfileService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemProfileController extends ControllerSupport {

    private final SystemProfileService systemProfileService;

    public SystemProfileController(SystemProfileService systemProfileService) {
        this.systemProfileService = systemProfileService;
    }

    @GetMapping("/system/profile/info")
    public String info(HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        SystemProfileService.ProfileInfoView view = systemProfileService.loadInfo(currentUser.getUserId());
        model.addAttribute("user", view.user());
        model.addAttribute("logDocuments", view.logDocuments());
        model.addAttribute("count", view.count());
        return "system/profile/info";
    }

    @GetMapping("/system/profile/edit")
    public String edit(HttpServletRequest request, Model model) {
        model.addAttribute("user", systemProfileService.loadEditableProfile(currentUser(request).getUserId()));
        return "system/profile/edit";
    }

    @PostMapping("/system/profile/modify")
    @ResponseBody
    public JsonResponse<Void> modify(
        HttpServletRequest request,
        @RequestParam("given_name") String givenName,
        @RequestParam("email") String email,
        @RequestParam("mobile") String mobile,
        @RequestParam(value = "phone", defaultValue = "") String phone,
        @RequestParam(value = "department", defaultValue = "") String department,
        @RequestParam(value = "position", defaultValue = "") String position,
        @RequestParam(value = "location", defaultValue = "") String location,
        @RequestParam(value = "im", defaultValue = "") String im
    ) {
        return systemProfileService.modifyProfile(
            currentUser(request).getUserId(),
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

    @GetMapping("/system/profile/followUser")
    public String followUser(HttpServletRequest request, Model model) {
        SystemProfileService.FollowUserView view = systemProfileService.loadFollowUsers(currentUser(request).getUserId());
        model.addAttribute("user", view.user());
        model.addAttribute("users", view.users());
        model.addAttribute("fansUsers", view.fansUsers());
        model.addAttribute("followCount", view.followCount());
        model.addAttribute("fansCount", view.fansCount());
        return "system/profile/follow_user";
    }

    @GetMapping("/system/profile/followDoc")
    public String followDoc(
        HttpServletRequest request,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int number,
        Model model
    ) {
        SystemProfileService.FollowDocPage view =
            systemProfileService.loadFollowDocs(currentUser(request).getUserId(), page, number);
        model.addAttribute("user", view.user());
        model.addAttribute("followDocuments", view.followDocuments());
        model.addAttribute("count", view.count());
        model.addAttribute("autoFollowDoc", view.autoFollowDoc());
        model.addAttribute("paginator", view.paginator());
        return "system/profile/follow_doc";
    }

    @GetMapping("/system/profile/activity")
    public String activity(
        HttpServletRequest request,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "15") int number,
        @RequestParam(defaultValue = "") String keyword,
        Model model
    ) {
        SystemProfileService.ActivityPage view =
            systemProfileService.loadActivity(currentUser(request).getUserId(), keyword, page, number);
        model.addAttribute("logDocuments", view.logDocuments());
        model.addAttribute("keyword", view.keyword());
        model.addAttribute("paginator", view.paginator());
        return "system/profile/activity";
    }

    @GetMapping("/system/profile/password")
    public String password() {
        return "system/profile/password";
    }

    @PostMapping("/system/profile/savePass")
    @ResponseBody
    public JsonResponse<Void> savePass(
        HttpServletRequest request,
        @RequestParam("pwd") String password,
        @RequestParam("pwd_new") String passwordNew,
        @RequestParam("pwd_confirm") String passwordConfirm
    ) {
        return systemProfileService.savePassword(currentUser(request).getUserId(), password, passwordNew, passwordConfirm);
    }
}
