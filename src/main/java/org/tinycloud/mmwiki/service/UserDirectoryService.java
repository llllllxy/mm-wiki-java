package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Follow;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.vo.*;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.PageModel;

import java.util.List;
import java.util.Map;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class UserDirectoryService {

    @Autowired
    private UserService userService;
    @Autowired
    private FollowService followService;
    @Autowired
    private LogDocumentMapper logDocumentMapper;
    @Autowired
    private DocumentMapper documentMapper;

    public PageModel<User> userPage(CurrentUser currentUser, String username, int pageNum, int pageSize) {
        String keyword = username == null ? "" : username.trim();
        PageInfo<User> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> {
                    if (keyword.isBlank()) {
                        userService.pageAllActive();
                    } else {
                        userService.pageByUsernameLike(keyword);
                    }
                });
        markFollows(currentUser.getUserId(), pageInfo.getList());
        return PageModel.from(pageInfo);
    }

    public FollowUserListPage listFollowedUsers(Integer userId) {
        List<Follow> follows = followService.findByUserIdAndType(userId, FollowService.TYPE_USER);
        List<Integer> ids = follows.stream().map(follow -> Integer.valueOf(follow.getObjectId())).toList();
        List<User> users = ids.isEmpty() ? List.of() : userService.findActiveByIds(ids);
        Map<String, Follow> followIndex = follows.stream().collect(java.util.stream.Collectors.toMap(Follow::getObjectId, follow -> follow, (left, right) -> left));
        for (User user : users) {
            Follow follow = followIndex.get(String.valueOf(user.getUserId()));
            if (follow != null) {
                user.setFollow(true);
                user.setFollowId(follow.getFollowId());
            }
        }
        return new FollowUserListPage(users, users.size());
    }

    public UserProfileView loadProfile(Integer userId, CurrentUser currentUser) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在。");
        }
        PageInfo<LogDocumentView> pageInfo = PageHelper.startPage(1, 10)
                .doSelectPageInfo(() -> logDocumentMapper.pageByUserIdVisibleToViewer(userId, currentUser.getUserId(), AccessService.isRoot(currentUser), ""));
        List<LogDocumentView> activities = pageInfo.getList();
        return new UserProfileView(user, activities, activities.size());
    }

    public UserFollowView loadUserFollowView(Integer profileUserId, Integer loginUserId) {
        User user = userService.findActiveById(profileUserId);
        if (user == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在。");
        }

        List<Follow> follows = followService.findByUserIdAndType(profileUserId, FollowService.TYPE_USER);
        List<Integer> followedIds = follows.stream().map(follow -> Integer.valueOf(follow.getObjectId())).toList();
        List<User> followedUsers = followedIds.isEmpty() ? List.of() : userService.findActiveByIds(followedIds);
        Map<String, Follow> followIndex = follows.stream().collect(java.util.stream.Collectors.toMap(Follow::getObjectId, follow -> follow, (left, right) -> left));
        for (User followedUser : followedUsers) {
            Follow follow = followIndex.get(String.valueOf(followedUser.getUserId()));
            if (follow != null) {
                followedUser.setFollowId(follow.getFollowId());
                followedUser.setFollow(true);
            }
        }

        List<Follow> fans = followService.findByObjectIdAndType(String.valueOf(profileUserId), FollowService.TYPE_USER);
        List<Integer> fanIds = fans.stream().map(Follow::getUserId).toList();
        List<User> fansUsers = fanIds.isEmpty() ? List.of() : userService.findActiveByIds(fanIds);

        return new UserFollowView(user, followedUsers, fansUsers, followedUsers.size(), fansUsers.size(), loginUserId);
    }

    public FollowDocView loadFollowDocs(Integer userId, CurrentUser currentUser) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在。");
        }
        List<Follow> follows = followService.findByUserIdAndType(userId, FollowService.TYPE_DOC);
        List<String> docIds = follows.stream().map(Follow::getObjectId).toList();
        List<Document> documents = docIds.isEmpty()
                ? List.of()
                : documentMapper.findVisibleByIds(currentUser.getUserId(), AccessService.isRoot(currentUser), docIds);
        Map<String, Follow> followIndex = follows.stream().collect(java.util.stream.Collectors.toMap(Follow::getObjectId, follow -> follow, (left, right) -> left));
        List<UserFollowedDocument> items = documents.stream()
                .map(document -> new UserFollowedDocument(document, followIndex.get(document.getDocumentId()).getFollowId()))
            .toList();
        return new FollowDocView(user, items, items.size());
    }

    private void markFollows(Integer loginUserId, List<User> users) {
        Map<String, Follow> followIndex = followService.indexByObjectId(loginUserId, FollowService.TYPE_USER);
        for (User user : users) {
            Follow follow = followIndex.get(String.valueOf(user.getUserId()));
            if (follow != null) {
                user.setFollow(true);
                user.setFollowId(follow.getFollowId());
            }
        }
    }
}
