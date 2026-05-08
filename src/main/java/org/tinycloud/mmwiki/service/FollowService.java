package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.Follow;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class FollowService {

    public static final int TYPE_DOC = 1;
    public static final int TYPE_USER = 2;

    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private ConfigService configService;

    public void autoFollowDocument(Integer userId, String documentId) {
        if (!"1".equals(configService.getValue("auto_follow_doc_open", "0"))) {
            return;
        }
        followDocument(userId, documentId);
    }

    public void followDocument(Integer userId, String documentId) {
        if (userId == null || documentId == null || documentId.isBlank()) {
            return;
        }
        Follow exists = followMapper.findByUserTypeAndObjectId(userId, TYPE_DOC, documentId);
        if (exists != null) {
            return;
        }
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setType(TYPE_DOC);
        follow.setObjectId(documentId);
        follow.setCreateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        followMapper.insert(follow);
    }

    public List<Follow> findByUserIdAndType(Integer userId, Integer type) {
        return followMapper.findByUserIdAndType(userId, type);
    }

    public List<Follow> findByObjectIdAndType(String objectId, Integer type) {
        return followMapper.findByObjectIdAndType(objectId, type);
    }

    public Map<String, Follow> indexByObjectId(Integer userId, Integer type) {
        return findByUserIdAndType(userId, type).stream()
            .collect(Collectors.toMap(Follow::getObjectId, Function.identity(), (left, right) -> left));
    }

    public JsonResponse<Void> add(Integer currentUserId, Integer type, String objectId, String redirect) {
        if (objectId == null || objectId.isBlank()) {
            return JsonResponse.error("没有选择关注对象。", null, "", 2000);
        }
        if (!TYPE_DOCEquals(type) && !TYPE_USEREquals(type)) {
            return JsonResponse.error("关注类型错误。", null, "", 2000);
        }
        if (TYPE_USEREquals(type) && objectId.equals(String.valueOf(currentUserId))) {
            return JsonResponse.error("不能关注自己。", null, "", 2000);
        }
        if (followMapper.findByUserTypeAndObjectId(currentUserId, type, objectId) != null) {
            return JsonResponse.error("您已关注过，不能重复关注。", null, "", 2000);
        }
        Follow follow = new Follow();
        follow.setUserId(currentUserId);
        follow.setType(type);
        follow.setObjectId(objectId);
        follow.setCreateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        followMapper.insert(follow);
        return JsonResponse.success("关注成功", null, redirect, 2000);
    }

    public JsonResponse<Void> cancel(Integer currentUserId, Integer followId, String redirect) {
        if (followId == null) {
            return JsonResponse.error("没有选择关注对象。", null, "", 2000);
        }
        Follow follow = followMapper.findById(followId);
        if (follow == null) {
            return JsonResponse.error("关注对象不存在。", null, "", 2000);
        }
        if (!currentUserId.equals(follow.getUserId())) {
            return JsonResponse.error("您只能取消自己的关注。", null, "", 2000);
        }
        followMapper.deleteById(followId);
        return JsonResponse.success("已取消关注", null, redirect, 2000);
    }

    public void deleteDocumentFollowers(String documentId) {
        followMapper.deleteByObjectIdAndType(documentId, TYPE_DOC);
    }

    private boolean TYPE_DOCEquals(Integer type) {
        return type != null && type == TYPE_DOC;
    }

    private boolean TYPE_USEREquals(Integer type) {
        return type != null && type == TYPE_USER;
    }
}
