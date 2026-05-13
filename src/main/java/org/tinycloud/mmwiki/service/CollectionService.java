package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.CollectionEntry;
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

    public static final int TYPE_DOC = 1;
    public static final int TYPE_SPACE = 2;

    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private SpaceMapper spaceMapper;
    @Autowired
    private AccessService accessService;

    public CollectionEntry findByUserTypeAndResourceId(Integer userId, int type, String resourceId) {
        return collectionMapper.findByUserTypeAndResourceId(userId, type, resourceId);
    }

    public java.util.List<CollectionEntry> findByUserIdAndType(Integer userId, int type) {
        return collectionMapper.findByUserIdAndType(userId, type);
    }

    public JsonResponse<Void> add(CurrentUser currentUser, int type, String resourceId, String redirect) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new SystemException("没有选择收藏资源！");
        }
        if (type != TYPE_DOC && type != TYPE_SPACE) {
            throw new SystemException("收藏类型错误！");
        }
        if (!canVisit(currentUser, type, resourceId)) {
            throw new SystemException("您没有权限收藏该资源。");
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

    private boolean canVisit(CurrentUser currentUser, int type, String resourceId) {
        if (type == TYPE_SPACE) {
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

    public JsonResponse<Void> cancel(Integer currentUserId, Integer collectionId, String redirect) {
        if (collectionId == null) {
            throw new SystemException("没有选择收藏资源！");
        }
        CollectionEntry entry = collectionMapper.findById(collectionId);
        if (entry == null) {
            throw new SystemException("收藏资源不存在！");
        }
        if (!currentUserId.equals(entry.getUserId())) {
            throw new SystemException("您只能取消自己的收藏！");
        }
        collectionMapper.deleteById(collectionId);
        return JsonResponse.success("已取消收藏！", redirect);
    }
}
