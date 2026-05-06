package org.tinycloud.mmwiki.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Link;
import org.tinycloud.mmwiki.mapper.LinkMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

@Service
public class LinkService {

    private final LinkMapper linkMapper;

    public LinkService(LinkMapper linkMapper) {
        this.linkMapper = linkMapper;
    }

    public LinkPage list(String keyword, int page, int number) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String search = keyword == null ? "" : keyword.trim();
        long count = search.isEmpty() ? linkMapper.countAll() : linkMapper.countByKeyword(search);
        var links = search.isEmpty()
            ? linkMapper.findPaged(offset, safeNumber)
            : linkMapper.findByKeywordPaged(search, offset, safeNumber);
        return new LinkPage(links, search, Paginator.of(safePage, safeNumber, count, "/system/link/list?keyword=" + search));
    }

    public Link findById(Integer linkId) {
        return linkId == null ? null : linkMapper.findById(linkId);
    }

    public JsonResponse<Void> save(Link link) {
        JsonResponse<Void> validation = validate(link, null);
        if (validation != null) {
            return validation;
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        link.setCreateTime(now);
        link.setUpdateTime(now);
        linkMapper.insert(link);
        return JsonResponse.success("添加链接成功", null, "/system/link/list", 2000);
    }

    public JsonResponse<Void> update(Link link) {
        if (link.getLinkId() == null || findById(link.getLinkId()) == null) {
            return JsonResponse.error("链接不存在。", null, "", 2000);
        }
        JsonResponse<Void> validation = validate(link, link.getLinkId());
        if (validation != null) {
            return validation;
        }
        link.setUpdateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        linkMapper.update(link);
        return JsonResponse.success("修改链接成功", null, "/system/link/list", 2000);
    }

    public JsonResponse<Void> delete(Integer linkId) {
        if (findById(linkId) == null) {
            return JsonResponse.error("链接不存在。", null, "", 2000);
        }
        linkMapper.deleteById(linkId);
        return JsonResponse.success("删除链接成功", null, "/system/link/list", 2000);
    }

    private JsonResponse<Void> validate(Link link, Integer currentId) {
        if (link == null) {
            return JsonResponse.error("链接参数错误。", null, "", 2000);
        }
        if (!StringUtils.hasText(link.getName())) {
            return JsonResponse.error("链接名称不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(link.getUrl())) {
            return JsonResponse.error("链接地址不能为空。", null, "", 2000);
        }
        try {
            java.net.URI uri = java.net.URI.create(link.getUrl().trim());
            if (uri.getScheme() == null || (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))) {
                return JsonResponse.error("链接地址格式不正确。", null, "", 2000);
            }
        } catch (Exception ex) {
            return JsonResponse.error("链接地址格式不正确。", null, "", 2000);
        }
        long duplicate = currentId == null ? linkMapper.countByName(link.getName().trim()) : linkMapper.countByNameAndNotId(currentId, link.getName().trim());
        if (duplicate > 0) {
            return JsonResponse.error("链接名称已经存在。", null, "", 2000);
        }
        link.setName(link.getName().trim());
        link.setUrl(link.getUrl().trim());
        link.setSequence(link.getSequence() == null ? 0 : link.getSequence());
        return null;
    }

    public record LinkPage(java.util.List<Link> links, String keyword, Paginator paginator) {
    }
}
