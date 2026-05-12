package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.MemberPage;
import org.tinycloud.mmwiki.vo.MemberView;
import org.tinycloud.mmwiki.vo.SpaceDownload;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.tinycloud.mmwiki.web.PageModel;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemSpaceController extends ControllerSupport {

    @Autowired
    private SpaceService spaceService;

    @GetMapping("/system/space/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/space/list";
    }

    @PostMapping("/system/space/list")
    @ResponseBody
    public JsonResponse<PageModel<Space>> listData(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                   @RequestParam(defaultValue = "") String keyword,
                                                   HttpServletRequest request) {
        return JsonResponse.success("查询成功", spaceService.listSpacesPage(currentUser(request), keyword, pageNum, pageSize));
    }

    @GetMapping("/system/space/member")
    public String member(@RequestParam("space_id") Integer spaceId,
                         HttpServletRequest request,
                         Model model) {
        MemberPage view = spaceService.listMembers(currentUser(request), spaceId, 1, 15, "/system/space/member?space_id=" + spaceId);
        model.addAttribute("space_id", spaceId);
        model.addAttribute("otherUsers", view.getOtherUsers());
        return "system/space/member";
    }

    @PostMapping("/system/space/member")
    @ResponseBody
    public JsonResponse<PageModel<MemberView>> memberData(@RequestParam("space_id") Integer spaceId,
                                                          @RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "15") int pageSize,
                                                          HttpServletRequest request) {
        return JsonResponse.success("查询成功", spaceService.listMembersPage(currentUser(request), spaceId, pageNum, pageSize));
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
        SpaceDownload payload = spaceService.downloadSpace(spaceId);
        String encoded = URLEncoder.encode(payload.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(payload.getResource().contentLength())
                .body(payload.getResource());
    }
}
