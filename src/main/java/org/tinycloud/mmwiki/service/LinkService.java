package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;

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
 * 友情链接服务，负责链接配置的分页查询、新增、修改和删除。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class LinkService {

    @Autowired
    private LinkMapper linkMapper;

    /**
     * 分页查询友情链接，支持按名称或地址关键字过滤。
     *
     * @param keyword  查询关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 友情链接分页数据
     */
    public PageModel<Link> pageModel(String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        Page<Link> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> {
                    if (search.isEmpty()) {
                        linkMapper.pageAll();
                    } else {
                        linkMapper.pageByKeyword(search);
                    }
                });
        return PageModel.from(pageInfo);
    }

    /**
     * 根据链接ID查询友情链接。
     *
     * @param linkId 链接ID
     * @return 链接记录，不存在时返回 null
     */
    public Link findById(Integer linkId) {
        return linkId == null ? null : linkMapper.findById(linkId);
    }

    /**
     * 新增友情链接，保存前会校验名称、地址格式和名称唯一性。
     *
     * @param link 待新增的链接信息
     * @return 前端统一 JSON 响应
     */
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

    /**
     * 更新友情链接，要求目标记录存在并通过字段校验。
     *
     * @param link 待更新的链接信息
     * @return 前端统一 JSON 响应
     */
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

    /**
     * 删除指定友情链接，删除前会确认记录存在。
     *
     * @param linkId 链接ID
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> delete(Integer linkId) {
        if (findById(linkId) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "链接不存在。");
        }
        linkMapper.deleteById(linkId);
        return JsonResponse.success("删除链接成功", "/system/link/list");
    }

    /**
     * 校验并规范化友情链接字段，包含必填、URL 协议和名称唯一性检查。
     *
     * @param link      待校验的链接信息
     * @param currentId 当前更新记录ID，新增时为 null
     * @return 校验通过时返回 null，校验失败时抛出业务异常
     */
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
