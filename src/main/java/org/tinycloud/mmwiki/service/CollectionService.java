package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.CollectionTypeEnum;
import org.tinycloud.mmwiki.domain.CollectionEntry;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.CollectionMapper;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.SpaceMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.vo.Access;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class CollectionService {

    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private SpaceMapper spaceMapper;
    @Autowired
    private AccessService accessService;

    /**
     * 查询用户对指定资源的收藏记录。
     *
     * @param userId     用户ID
     * @param type       收藏资源类型
     * @param resourceId 资源ID
     * @return 收藏记录，不存在时返回 null
     */
    public CollectionEntry findByUserTypeAndResourceId(Integer userId, int type, String resourceId) {
        return collectionMapper.findByUserTypeAndResourceId(userId, type, resourceId);
    }

    /**
     * 查询用户指定类型的全部收藏记录。
     *
     * @param userId 用户ID
     * @param type   收藏资源类型
     * @return 收藏记录列表
     */
    public java.util.List<CollectionEntry> findByUserIdAndType(Integer userId, int type) {
        return collectionMapper.findByUserIdAndType(userId, type);
    }

    /**
     * 添加收藏，收藏前会校验资源类型、访问权限和重复收藏。
     *
     * @param currentUser 当前登录用户
     * @param type        收藏资源类型
     * @param resourceId  资源ID
     * @param redirect    操作成功后的跳转地址
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> add(CurrentUser currentUser, int type, String resourceId, String redirect) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new SystemException("没有选择收藏资源！");
        }
        if (!CollectionTypeEnum.DOCUMENT.is(type) && !CollectionTypeEnum.SPACE.is(type)) {
            throw new SystemException("收藏类型错误！");
        }
        if (!canVisit(currentUser, type, resourceId)) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限收藏该资源。");
        }
        CollectionEntry exists = collectionMapper.findByUserTypeAndResourceId(currentUser.getUserId(), type, resourceId);
        if (exists != null) {
            throw new SystemException("您已收藏过，不能重复收藏！");
        }
        CollectionEntry entry = new CollectionEntry();
        entry.setUserId(currentUser.getUserId());
        entry.setType(type);
        entry.setResourceId(resourceId);
        entry.setCreateTime(TimeUtils.now());
        collectionMapper.insert(entry);
        return JsonResponse.success("收藏成功！", redirect);
    }

    /**
     * 判断当前用户是否可以访问待收藏资源，空间和文档分别按空间权限校验。
     *
     * @param currentUser 当前登录用户
     * @param type        收藏资源类型
     * @param resourceId  资源ID
     * @return true 表示可访问，false 表示不可访问
     */
    private boolean canVisit(CurrentUser currentUser, int type, String resourceId) {
        if (CollectionTypeEnum.SPACE.is(type)) {
            try {
                Space space = spaceMapper.findActiveById(Integer.valueOf(resourceId));
                return accessService.access(currentUser, space).isVisit();
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        Document document = documentMapper.findActiveById(resourceId);
        if (document == null) {
            return false;
        }
        Space space = spaceMapper.findActiveById(document.getSpaceId());
        Access access = accessService.access(currentUser, space);
        return access.isVisit();
    }

    /**
     * 取消收藏，只允许当前用户取消自己的收藏记录。
     *
     * @param currentUserId 当前登录用户ID
     * @param collectionId  收藏记录ID
     * @param redirect      操作成功后的跳转地址
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> cancel(Integer currentUserId, Integer collectionId, String redirect) {
        if (collectionId == null) {
            throw new SystemException("没有选择收藏资源！");
        }
        CollectionEntry entry = collectionMapper.findById(collectionId);
        if (entry == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "收藏资源不存在！");
        }
        if (!currentUserId.equals(entry.getUserId())) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您只能取消自己的收藏！");
        }
        collectionMapper.deleteById(collectionId);
        return JsonResponse.success("已取消收藏！", redirect);
    }
}
