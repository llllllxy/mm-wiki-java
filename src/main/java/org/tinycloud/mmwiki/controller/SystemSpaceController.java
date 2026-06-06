package org.tinycloud.mmwiki.controller;

import org.tinycloud.mmwiki.vo.MemberPage;
import org.tinycloud.mmwiki.vo.MemberView;
import org.tinycloud.mmwiki.vo.SpaceDownload;

import org.springframework.beans.factory.annotation.Autowired;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.service.SpaceService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * 后台空间控制器，负责空间列表、空间表单、成员管理、下载和删除。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/space")
public class SystemSpaceController extends ControllerSupport {

    @Autowired
    private SpaceService spaceService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "system/space/list";
    }

    @PostMapping("/list")
    @ResponseBody
    public JsonResponse<PageModel<Space>> listData(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "") String keyword
    ) {
        return JsonResponse.success("查询成功", spaceService.listSpacesPage(currentUser(), keyword, pageNum, pageSize));
    }

    @GetMapping("/member")
    public String member(@RequestParam("space_id") Integer spaceId, Model model) {
        MemberPage view = spaceService.getMemberPageInfo(currentUser(), spaceId, "/system/space/member?space_id=" + spaceId);
        model.addAttribute("space_id", spaceId);
        model.addAttribute("otherUsers", view.getOtherUsers());
        return "system/space/member";
    }

    @PostMapping("/member")
    @ResponseBody
    public JsonResponse<PageModel<MemberView>> memberData(
            @RequestParam("space_id") Integer spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize
    ) {
        return JsonResponse.success("查询成功", spaceService.listMembersPage(currentUser(), spaceId, pageNum, pageSize));
    }

    @GetMapping("/add")
    public String add() {
        return "system/space/form";
    }

    @PostMapping("/save")
    @ResponseBody
    public JsonResponse<Void> save(Space space) throws IOException {
        return spaceService.createSpace(currentUser(), space);
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("space_id") Integer spaceId, Model model) {
        model.addAttribute("space", spaceService.requireSpace(spaceId));
        return "system/space/form";
    }

    @PostMapping("/modify")
    @ResponseBody
    public JsonResponse<Void> modify(Space space) throws IOException {
        return spaceService.updateSpace(currentUser(), space);
    }

    @PostMapping("/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("space_id") Integer spaceId) throws IOException {
        return spaceService.deleteSpace(currentUser(), spaceId);
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> download(@RequestParam("space_id") Integer spaceId) throws IOException {
        SpaceDownload payload = spaceService.downloadSpace(currentUser(), spaceId);
        String encoded = URLEncoder.encode(payload.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(payload.getResource().contentLength())
                .body(payload.getResource());
    }
}
