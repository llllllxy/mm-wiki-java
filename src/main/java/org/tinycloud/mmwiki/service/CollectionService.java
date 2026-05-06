package org.tinycloud.mmwiki.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.CollectionEntry;
import org.tinycloud.mmwiki.mapper.CollectionMapper;
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

    private final CollectionMapper collectionMapper;

    public CollectionService(CollectionMapper collectionMapper) {
        this.collectionMapper = collectionMapper;
    }

    public CollectionEntry findByUserTypeAndResourceId(Integer userId, int type, String resourceId) {
        return collectionMapper.findByUserTypeAndResourceId(userId, type, resourceId);
    }

    public java.util.List<CollectionEntry> findByUserIdAndType(Integer userId, int type) {
        return collectionMapper.findByUserIdAndType(userId, type);
    }

    public JsonResponse<Void> add(Integer userId, int type, String resourceId, String redirect) {
        if (resourceId == null || resourceId.isBlank()) {
            return JsonResponse.error("没有选择收藏资源！", null, "", 2000);
        }
        if (type != TYPE_DOC && type != TYPE_SPACE) {
            return JsonResponse.error("收藏类型错误！", null, "", 2000);
        }
        CollectionEntry exists = collectionMapper.findByUserTypeAndResourceId(userId, type, resourceId);
        if (exists != null) {
            return JsonResponse.error("您已收藏过，不能重复收藏！", null, "", 2000);
        }
        CollectionEntry entry = new CollectionEntry();
        entry.setUserId(userId);
        entry.setType(type);
        entry.setResourceId(resourceId);
        entry.setCreateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        collectionMapper.insert(entry);
        return JsonResponse.success("收藏成功！", null, redirect, 2000);
    }

    public JsonResponse<Void> cancel(Integer currentUserId, Integer collectionId, String redirect) {
        if (collectionId == null) {
            return JsonResponse.error("没有选择收藏资源！", null, "", 2000);
        }
        CollectionEntry entry = collectionMapper.findById(collectionId);
        if (entry == null) {
            return JsonResponse.error("收藏资源不存在！", null, "", 2000);
        }
        if (!currentUserId.equals(entry.getUserId())) {
            return JsonResponse.error("您只能取消自己的收藏！", null, "", 2000);
        }
        collectionMapper.deleteById(collectionId);
        return JsonResponse.success("已取消收藏！", null, redirect, 2000);
    }
}
