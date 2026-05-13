package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.Access;
import org.tinycloud.mmwiki.vo.MemberPage;
import org.tinycloud.mmwiki.vo.MemberView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.service.AccessService;
import org.tinycloud.mmwiki.service.DocumentService;
import org.tinycloud.mmwiki.service.SpaceService;
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
public class SpaceController extends ControllerSupport {

    @Autowired
    private SpaceService spaceService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private AccessService accessService;

    @GetMapping("/space/index")
    public String index(Model model) {
        nav(model, "space");
        model.addAttribute("spaceTags", spaceService.listTags(currentUser()));
        return "space/index";
    }

    @GetMapping("/space/list")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       Model model) {
        nav(model, "space");
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "space/list";
    }

    @PostMapping("/space/list")
    @ResponseBody
    public JsonResponse<PageModel<Space>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                   @RequestParam(defaultValue = "") String keyword) {
        return JsonResponse.success("查询成功", spaceService.listSpacesPage(currentUser(), keyword, pageNum, pageSize));
    }

    @GetMapping("/space/collection")
    public String collection(Model model) {
        nav(model, "space");
        return "space/collection";
    }

    @PostMapping("/space/collection")
    @ResponseBody
    public JsonResponse<PageModel<Space>> collectionData(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "20") int pageSize) {
        return JsonResponse.success("查询成功", spaceService.listCollectedSpaces(currentUser(), pageNum, pageSize));
    }

    @GetMapping("/space/search")
    public String search(@RequestParam("tag") String tag, Model model) {
        nav(model, "space");
        model.addAttribute("tag", tag);
        return "space/search";
    }

    @PostMapping("/space/search")
    @ResponseBody
    public JsonResponse<PageModel<Space>> searchData(@RequestParam("tag") String tag,
                                                     @RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        return JsonResponse.success("查询成功", spaceService.searchByTag(currentUser(), tag, pageNum, pageSize));
    }

    @GetMapping("/space/document")
    public String document(@RequestParam("space_id") Integer spaceId) {
        CurrentUser currentUser = currentUser();
        Space space = spaceService.requireSpace(spaceId);
        Access access = accessService.access(currentUser, space);
        if (!access.isVisit()) {
            return "error/403";
        }
        Document spaceDefault = documentService.findSpaceDefaultDocument(spaceId);
        return "redirect:/document/index?document_id=" + spaceDefault.getDocumentId();
    }

    @GetMapping("/space/member")
    public String member(@RequestParam("space_id") Integer spaceId,
                         Model model) {
        MemberPage view = spaceService.getMemberPageInfo(currentUser(), spaceId);
        model.addAttribute("space_id", spaceId);
        // 获取非空间成员otherUsers
        model.addAttribute("otherUsers", view.getOtherUsers());
        // 根据权限判断跳转的页面
        return view.isManager() ? "space/manager_member" : "space/member";
    }

    @PostMapping("/space/member")
    @ResponseBody
    public JsonResponse<PageModel<MemberView>> memberData(@RequestParam("space_id") Integer spaceId,
                                                          @RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "20") int pageSize) {
        return JsonResponse.success("查询成功", spaceService.listMembersPage(currentUser(), spaceId, pageNum, pageSize));
    }

    @PostMapping("/space/addMember")
    @ResponseBody
    public JsonResponse<Void> addMember(
            @RequestParam("space_id") Integer spaceId,
            @RequestParam("user_id") Integer userId,
            @RequestParam("privilege") Integer privilege
    ) {
        spaceService.addMember(currentUser(), spaceId, userId, privilege);
        return JsonResponse.success("添加成员成功", "/space/member?space_id=" + spaceId);
    }

    @PostMapping("/space/removeMember")
    @ResponseBody
    public JsonResponse<Void> removeMember(@RequestParam("space_id") Integer spaceId,
                                           @RequestParam("user_id") Integer userId,
                                           @RequestParam("space_user_id") Integer spaceUserId) {
        spaceService.removeMember(currentUser(), spaceId, userId, spaceUserId);
        return JsonResponse.success("移除成员成功", "/space/member?space_id=" + spaceId);
    }

    @PostMapping("/space/modifyMember")
    @ResponseBody
    public JsonResponse<Void> modifyMember(@RequestParam("space_id") Integer spaceId,
                                           @RequestParam("space_user_id") Integer spaceUserId,
                                           @RequestParam("privilege") Integer privilege) {
        spaceService.updateMemberPrivilege(currentUser(), spaceId, spaceUserId, privilege);
        return JsonResponse.success("更新权限成功");
    }
}
