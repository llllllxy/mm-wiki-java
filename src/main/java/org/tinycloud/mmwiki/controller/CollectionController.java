package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.service.CollectionService;
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
@RequestMapping("/collection")
public class CollectionController extends ControllerSupport {

    @Autowired
    private CollectionService collectionService;

    @PostMapping("/add")
    @ResponseBody
    public JsonResponse<Void> add(
        @RequestParam("type") int type,
        @RequestParam("resource_id") String resourceId,
        HttpServletRequest request
    ) {
        CurrentUser currentUser = currentUser(request);
        return collectionService.add(currentUser.getUserId(), type, resourceId, request.getHeader("Referer"));
    }

    @PostMapping("/cancel")
    @ResponseBody
    public JsonResponse<Void> cancel(
        @RequestParam("collection_id") Integer collectionId,
        HttpServletRequest request
    ) {
        CurrentUser currentUser = currentUser(request);
        return collectionService.cancel(currentUser.getUserId(), collectionId, request.getHeader("Referer"));
    }
}
