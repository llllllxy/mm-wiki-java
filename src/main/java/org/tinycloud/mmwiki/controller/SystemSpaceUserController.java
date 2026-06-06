package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * 后台空间成员控制器，负责空间成员添加、权限修改和移除。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/space_user")
public class SystemSpaceUserController extends ControllerSupport {

    @Autowired
    private SpaceService spaceService;

    @PostMapping("/save")
    @ResponseBody
    public JsonResponse<Void> save(@RequestParam("space_id") Integer spaceId,
                                   @RequestParam("user_id") Integer userId,
                                   @RequestParam("privilege") Integer privilege) {
        this.spaceService.addMember(currentUser(), spaceId, userId, privilege);
        return JsonResponse.success("添加成员成功", "/system/space/member?space_id=" + spaceId);
    }

    @PostMapping("/remove")
    @ResponseBody
    public JsonResponse<Void> remove(@RequestParam("space_id") Integer spaceId,
                                     @RequestParam("user_id") Integer userId,
                                     @RequestParam("space_user_id") Integer spaceUserId) {
        this.spaceService.removeMember(currentUser(), spaceId, userId, spaceUserId);
        return JsonResponse.success("移除成员成功", "/system/space/member?space_id=" + spaceId);
    }

    @PostMapping("/modify")
    @ResponseBody
    public JsonResponse<Void> modify(@RequestParam("space_id") Integer spaceId,
                                     @RequestParam("space_user_id") Integer spaceUserId,
                                     @RequestParam("privilege") Integer privilege) {
        this.spaceService.updateMemberPrivilege(currentUser(), spaceId, spaceUserId, privilege);
        return JsonResponse.success("更新权限成功");
    }
}
