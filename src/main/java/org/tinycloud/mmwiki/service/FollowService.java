package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Follow;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.mapper.SpaceMapper;
import org.tinycloud.mmwiki.vo.Access;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.util.TimeUtils;

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
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private SpaceMapper spaceMapper;
    @Autowired
    private AccessService accessService;
    @Autowired
    private UserService userService;

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
        follow.setCreateTime(TimeUtils.now());
        followMapper.insert(follow);
    }

    public List<Follow> findByUserIdAndType(Integer userId, Integer type) {
        return followMapper.findByUserIdAndType(userId, type);
    }

    public List<Follow> findByObjectIdAndType(String objectId, Integer type) {
        return followMapper.findByObjectIdAndType(objectId, type);
    }

    public Follow findByUserTypeAndObjectId(Integer userId, Integer type, String objectId) {
        return followMapper.findByUserTypeAndObjectId(userId, type, objectId);
    }

    public Map<String, Follow> indexByObjectId(Integer userId, Integer type) {
        return findByUserIdAndType(userId, type).stream()
            .collect(Collectors.toMap(Follow::getObjectId, Function.identity(), (left, right) -> left));
    }

    public JsonResponse<Void> add(CurrentUser currentUser, Integer type, String objectId, String redirect) {
        if (objectId == null || objectId.isBlank()) {
            throw new SystemException("没有选择关注对象。");
        }
        if (!TYPE_DOCEquals(type) && !TYPE_USEREquals(type)) {
            throw new SystemException("关注类型错误。");
        }
        if (TYPE_USEREquals(type) && objectId.equals(String.valueOf(currentUser.getUserId()))) {
            throw new SystemException("不能关注自己。");
        }
        if (!canFollow(currentUser, type, objectId)) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您没有权限关注该对象。");
        }
        if (followMapper.findByUserTypeAndObjectId(currentUser.getUserId(), type, objectId) != null) {
            throw new SystemException("您已关注过，不能重复关注。");
        }
        Follow follow = new Follow();
        follow.setUserId(currentUser.getUserId());
        follow.setType(type);
        follow.setObjectId(objectId);
        follow.setCreateTime(TimeUtils.now());
        followMapper.insert(follow);
        return JsonResponse.success("关注成功", redirect);
    }

    private boolean canFollow(CurrentUser currentUser, Integer type, String objectId) {
        if (TYPE_USEREquals(type)) {
            try {
                User user = userService.findActiveById(Integer.valueOf(objectId));
                return user != null;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        Document document = documentMapper.findActiveById(objectId);
        if (document == null) {
            return false;
        }
        Space space = spaceMapper.findActiveById(document.getSpaceId());
        Access access = accessService.access(currentUser, space);
        return access.isVisit();
    }

    public JsonResponse<Void> cancel(Integer currentUserId, Integer followId, String redirect) {
        if (followId == null) {
            throw new SystemException("没有选择关注对象。");
        }
        Follow follow = followMapper.findById(followId);
        if (follow == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "关注对象不存在。");
        }
        if (!currentUserId.equals(follow.getUserId())) {
            throw new SystemException(ErrorCodeEnum.FORBIDDEN, "您只能取消自己的关注。");
        }
        followMapper.deleteById(followId);
        return JsonResponse.success("已取消关注", redirect);
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

