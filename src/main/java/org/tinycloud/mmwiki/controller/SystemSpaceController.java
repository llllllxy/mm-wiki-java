package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

@Controller
public class SystemSpaceController extends ControllerSupport {

    private final SpaceService spaceService;

    public SystemSpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping("/system/space/list")
    public String list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        @RequestParam(defaultValue = "") String keyword,
        HttpServletRequest request,
        Model model
    ) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        SpaceService.SpacePage view = spaceService.listSpaces(currentUser(request), keyword, safePage, safeNumber);
        model.addAttribute("spaces", view.spaces());
        model.addAttribute("keyword", view.keyword());
        model.addAttribute("paginator", Paginator.of(safePage, safeNumber, view.count(), "/system/space/list?keyword=" + view.keyword()));
        return "system/space/list";
    }

    @GetMapping("/system/space/member")
    public String member(
        @RequestParam("space_id") Integer spaceId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "15") int number,
        HttpServletRequest request,
        Model model
    ) {
        SpaceService.MemberPage view = spaceService.listMembers(currentUser(request), spaceId, page, number);
        model.addAttribute("users", view.users());
        model.addAttribute("space_id", spaceId);
        model.addAttribute("otherUsers", view.otherUsers());
        model.addAttribute("paginator", Paginator.of(page, number, view.paginator().getNums(), "/system/space/member?space_id=" + spaceId));
        return "system/space/member";
    }

    @GetMapping("/system/space/add")
    public String add() {
        return "system/space/form";
    }

    @PostMapping("/system/space/save")
    @ResponseBody
    public JsonResponse<Void> save(HttpServletRequest request, Space space) throws IOException {
        return spaceService.createSpace(currentUser(request), space);
    }

    @GetMapping("/system/space/edit")
    public String edit(@RequestParam("space_id") Integer spaceId, Model model) {
        model.addAttribute("space", spaceService.requireSpace(spaceId));
        return "system/space/form";
    }

    @PostMapping("/system/space/modify")
    @ResponseBody
    public JsonResponse<Void> modify(HttpServletRequest request, Space space) throws IOException {
        return spaceService.updateSpace(currentUser(request), space);
    }

    @PostMapping("/system/space/delete")
    @ResponseBody
    public JsonResponse<Void> delete(HttpServletRequest request, @RequestParam("space_id") Integer spaceId) throws IOException {
        return spaceService.deleteSpace(currentUser(request), spaceId);
    }

    @GetMapping("/system/space/download")
    public ResponseEntity<ByteArrayResource> download(@RequestParam("space_id") Integer spaceId) throws IOException {
        SpaceService.SpaceDownload payload = spaceService.downloadSpace(spaceId);
        String encoded = URLEncoder.encode(payload.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(payload.resource().contentLength())
            .body(payload.resource());
    }
}
