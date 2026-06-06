package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.FollowTypeEnum;
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
 * 关注业务服务，负责用户和文档关注关系的查询、添加、取消及可见性校验。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class FollowService {

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

    /**
     * 根据系统配置自动关注文档，未开启自动关注时直接返回。
     *
     * @param userId     用户ID
     * @param documentId 文档ID
     */
    public void autoFollowDocument(Integer userId, String documentId) {
        if (!"1".equals(configService.getValue("auto_follow_doc_open", "0"))) {
            return;
        }
        followDocument(userId, documentId);
    }

    /**
     * 关注指定文档，已关注时不会重复插入记录。
     *
     * @param userId     用户ID
     * @param documentId 文档ID
     */
    public void followDocument(Integer userId, String documentId) {
        if (userId == null || documentId == null || documentId.isBlank()) {
            return;
        }
        Follow exists = followMapper.findByUserTypeAndObjectId(userId, FollowTypeEnum.DOCUMENT.getCode(), documentId);
        if (exists != null) {
            return;
        }
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setType(FollowTypeEnum.DOCUMENT.getCode());
        follow.setObjectId(documentId);
        follow.setCreateTime(TimeUtils.now());
        followMapper.insert(follow);
    }

    /**
     * 查询用户指定类型的关注记录。
     *
     * @param userId 用户ID
     * @param type   关注对象类型
     * @return 关注记录列表
     */
    public List<Follow> findByUserIdAndType(Integer userId, Integer type) {
        return followMapper.findByUserIdAndType(userId, type);
    }

    /**
     * 查询指定对象被关注的记录。
     *
     * @param objectId 关注对象ID
     * @param type     关注对象类型
     * @return 关注记录列表
     */
    public List<Follow> findByObjectIdAndType(String objectId, Integer type) {
        return followMapper.findByObjectIdAndType(objectId, type);
    }

    /**
     * 查询用户对指定对象的关注记录。
     *
     * @param userId   用户ID
     * @param type     关注对象类型
     * @param objectId 关注对象ID
     * @return 关注记录，不存在时返回 null
     */
    public Follow findByUserTypeAndObjectId(Integer userId, Integer type, String objectId) {
        return followMapper.findByUserTypeAndObjectId(userId, type, objectId);
    }

    /**
     * 查询用户指定类型关注记录，并按对象ID建立索引。
     *
     * @param userId 用户ID
     * @param type   关注对象类型
     * @return 以对象ID为键的关注记录映射
     */
    public Map<String, Follow> indexByObjectId(Integer userId, Integer type) {
        return findByUserIdAndType(userId, type).stream()
            .collect(Collectors.toMap(Follow::getObjectId, Function.identity(), (left, right) -> left));
    }

    /**
     * 添加关注，关注前会校验对象类型、访问权限和重复关注。
     *
     * @param currentUser 当前登录用户
     * @param type        关注对象类型
     * @param objectId    关注对象ID
     * @param redirect    操作成功后的跳转地址
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> add(CurrentUser currentUser, Integer type, String objectId, String redirect) {
        if (objectId == null || objectId.isBlank()) {
            throw new SystemException("没有选择关注对象。");
        }
        if (!FollowTypeEnum.DOCUMENT.is(type) && !FollowTypeEnum.USER.is(type)) {
            throw new SystemException("关注类型错误。");
        }
        if (FollowTypeEnum.USER.is(type) && objectId.equals(String.valueOf(currentUser.getUserId()))) {
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

    /**
     * 判断当前用户是否可以关注指定对象，用户对象要求存在，文档对象要求可访问。
     *
     * @param currentUser 当前登录用户
     * @param type        关注对象类型
     * @param objectId    关注对象ID
     * @return true 表示允许关注，false 表示不允许关注
     */
    private boolean canFollow(CurrentUser currentUser, Integer type, String objectId) {
        if (FollowTypeEnum.USER.is(type)) {
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

    /**
     * 取消关注，只允许当前用户取消自己的关注记录。
     *
     * @param currentUserId 当前登录用户ID
     * @param followId      关注记录ID
     * @param redirect      操作成功后的跳转地址
     * @return 前端统一 JSON 响应
     */
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

    /**
     * 删除指定文档的全部关注记录，通常在文档删除时调用。
     *
     * @param documentId 文档ID
     */
    public void deleteDocumentFollowers(String documentId) {
        followMapper.deleteByObjectIdAndType(documentId, FollowTypeEnum.DOCUMENT.getCode());
    }
}

