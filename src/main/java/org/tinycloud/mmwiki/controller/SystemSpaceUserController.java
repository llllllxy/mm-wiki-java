package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

@Controller
public class SystemSpaceUserController extends ControllerSupport {

    private final SpaceService spaceService;

    public SystemSpaceUserController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping("/system/space_user/save")
    @ResponseBody
    public JsonResponse<Void> save(
        HttpServletRequest request,
        @RequestParam("space_id") Integer spaceId,
        @RequestParam("user_id") Integer userId,
        @RequestParam("privilege") Integer privilege
    ) {
        spaceService.addMember(currentUser(request), spaceId, userId, privilege);
        return JsonResponse.success("添加成员成功", null, "/system/space/member?space_id=" + spaceId, 2000);
    }

    @PostMapping("/system/space_user/remove")
    @ResponseBody
    public JsonResponse<Void> remove(
        HttpServletRequest request,
        @RequestParam("space_id") Integer spaceId,
        @RequestParam("user_id") Integer userId,
        @RequestParam("space_user_id") Integer spaceUserId
    ) {
        spaceService.removeMember(currentUser(request), spaceId, userId, spaceUserId);
        return JsonResponse.success("移除成员成功", null, "/system/space/member?space_id=" + spaceId, 2000);
    }

    @PostMapping("/system/space_user/modify")
    @ResponseBody
    public JsonResponse<Void> modify(
        HttpServletRequest request,
        @RequestParam("space_id") Integer spaceId,
        @RequestParam("space_user_id") Integer spaceUserId,
        @RequestParam("privilege") Integer privilege
    ) {
        spaceService.updateMemberPrivilege(currentUser(request), spaceId, spaceUserId, privilege);
        return JsonResponse.success("更新权限成功", null, "", 2000);
    }
}
