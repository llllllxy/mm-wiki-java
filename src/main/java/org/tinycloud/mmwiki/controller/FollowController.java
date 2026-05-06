package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.FollowService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

@Controller
public class FollowController extends ControllerSupport {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follow/add")
    @ResponseBody
    public JsonResponse<Void> add(
        @RequestParam("type") Integer type,
        @RequestParam("object_id") String objectId,
        HttpServletRequest request
    ) {
        String redirect = request.getHeader("Referer");
        return followService.add(currentUser(request).getUserId(), type, objectId, redirect == null ? "" : redirect);
    }

    @PostMapping("/follow/cancel")
    @ResponseBody
    public JsonResponse<Void> cancel(@RequestParam("follow_id") Integer followId, HttpServletRequest request) {
        String redirect = request.getHeader("Referer");
        return followService.cancel(currentUser(request).getUserId(), followId, redirect == null ? "" : redirect);
    }
}
