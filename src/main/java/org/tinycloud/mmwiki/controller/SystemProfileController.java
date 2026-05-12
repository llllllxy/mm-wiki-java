package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.FollowDocPage;
import org.tinycloud.mmwiki.vo.FollowUserView;
import org.tinycloud.mmwiki.vo.ProfileFollowedDocument;
import org.tinycloud.mmwiki.vo.ProfileInfoView;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.service.SystemProfileService;
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
public class SystemProfileController extends ControllerSupport {

    @Autowired
    private SystemProfileService systemProfileService;

    @GetMapping("/system/profile/info")
    public String info(HttpServletRequest request, Model model) {
        CurrentUser currentUser = currentUser(request);
        ProfileInfoView view = systemProfileService.loadInfo(currentUser.getUserId());
        model.addAttribute("user", view.getUser());
        model.addAttribute("logDocuments", view.getLogDocuments());
        model.addAttribute("count", view.getCount());
        return "system/profile/info";
    }

    @GetMapping("/system/profile/edit")
    public String edit(HttpServletRequest request, Model model) {
        model.addAttribute("user", systemProfileService.loadEditableProfile(currentUser(request).getUserId()));
        return "system/profile/edit";
    }

    @PostMapping("/system/profile/modify")
    @ResponseBody
    public JsonResponse<Void> modify(HttpServletRequest request,
                                     @RequestParam("given_name") String givenName,
                                     @RequestParam("email") String email,
                                     @RequestParam("mobile") String mobile,
                                     @RequestParam(value = "phone", defaultValue = "") String phone,
                                     @RequestParam(value = "department", defaultValue = "") String department,
                                     @RequestParam(value = "position", defaultValue = "") String position,
                                     @RequestParam(value = "location", defaultValue = "") String location,
                                     @RequestParam(value = "im", defaultValue = "") String im) {
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
        FollowUserView view = systemProfileService.loadFollowUsers(currentUser(request).getUserId());
        model.addAttribute("user", view.getUser());
        model.addAttribute("users", view.getUsers());
        model.addAttribute("fansUsers", view.getFansUsers());
        model.addAttribute("followCount", view.getFollowCount());
        model.addAttribute("fansCount", view.getFansCount());
        return "system/profile/follow_user";
    }

    @GetMapping("/system/profile/followDoc")
    public String followDoc(HttpServletRequest request, Model model) {
        FollowDocPage view = systemProfileService.loadFollowDocs(currentUser(request).getUserId());
        model.addAttribute("user", view.getUser());
        model.addAttribute("autoFollowDoc", view.getAutoFollowDoc());
        return "system/profile/follow_doc";
    }

    @PostMapping("/system/profile/followDoc")
    @ResponseBody
    public JsonResponse<PageModel<ProfileFollowedDocument>> followDocData(HttpServletRequest request,
                                                                          @RequestParam(defaultValue = "1") int pageNum,
                                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return JsonResponse.success("查询成功", systemProfileService.loadFollowDocPage(currentUser(request).getUserId(), pageNum, pageSize));
    }

    @GetMapping("/system/profile/activity")
    public String activity(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/profile/activity";
    }

    @PostMapping("/system/profile/activity")
    @ResponseBody
    public JsonResponse<PageModel<LogDocumentView>> activityData(HttpServletRequest request,
                                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "15") int pageSize,
                                                                 @RequestParam(defaultValue = "") String keyword) {
        return JsonResponse.success("查询成功", systemProfileService.loadActivityPage(currentUser(request).getUserId(), keyword, pageNum, pageSize));
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
