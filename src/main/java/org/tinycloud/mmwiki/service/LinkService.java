package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.Link;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.LinkMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

import java.time.LocalDateTime;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class LinkService {

    @Autowired
    private LinkMapper linkMapper;

    public PageModel<Link> pageModel(String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        PageInfo<Link> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> {
                    if (search.isEmpty()) {
                        linkMapper.pageAll();
                    } else {
                        linkMapper.pageByKeyword(search);
                    }
                });
        return PageModel.from(pageInfo);
    }

    public Link findById(Integer linkId) {
        return linkId == null ? null : linkMapper.findById(linkId);
    }

    public JsonResponse<Void> save(Link link) {
        JsonResponse<Void> validation = validate(link, null);
        if (validation != null) {
            return validation;
        }
        LocalDateTime now = LocalDateTime.now();
        link.setCreateTime(now);
        link.setUpdateTime(now);
        linkMapper.insert(link);
        return JsonResponse.success("添加链接成功", "/system/link/list");
    }

    public JsonResponse<Void> update(Link link) {
        if (link.getLinkId() == null || findById(link.getLinkId()) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "链接不存在。");
        }
        JsonResponse<Void> validation = validate(link, link.getLinkId());
        if (validation != null) {
            return validation;
        }
        link.setUpdateTime(LocalDateTime.now());
        linkMapper.update(link);
        return JsonResponse.success("修改链接成功", "/system/link/list");
    }

    public JsonResponse<Void> delete(Integer linkId) {
        if (findById(linkId) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "链接不存在。");
        }
        linkMapper.deleteById(linkId);
        return JsonResponse.success("删除链接成功", "/system/link/list");
    }

    private JsonResponse<Void> validate(Link link, Integer currentId) {
        if (link == null) {
            throw new SystemException("链接参数错误。");
        }
        if (!StringUtils.hasText(link.getName())) {
            throw new SystemException("链接名称不能为空。");
        }
        if (!StringUtils.hasText(link.getUrl())) {
            throw new SystemException("链接地址不能为空。");
        }
        try {
            java.net.URI uri = java.net.URI.create(link.getUrl().trim());
            if (uri.getScheme() == null || (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))) {
                throw new SystemException("链接地址格式不正确。");
            }
        } catch (Exception ex) {
            throw new SystemException("链接地址格式不正确。");
        }
        long duplicate = currentId == null ? linkMapper.countByName(link.getName().trim()) : linkMapper.countByNameAndNotId(currentId, link.getName().trim());
        if (duplicate > 0) {
            throw new SystemException("链接名称已经存在。");
        }
        link.setName(link.getName().trim());
        link.setUrl(link.getUrl().trim());
        link.setSequence(link.getSequence() == null ? 0 : link.getSequence());
        return null;
    }
}
