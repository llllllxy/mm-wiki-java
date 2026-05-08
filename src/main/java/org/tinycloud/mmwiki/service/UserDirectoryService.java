package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Follow;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.Paginator;

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

    public UserListPage listUsers(CurrentUser currentUser, String username, int page, int number) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String keyword = username == null ? "" : username.trim();

        long count;
        List<User> users;
        if (keyword.isBlank()) {
            count = userService.countAllActive();
            users = userService.findAllActivePaged(offset, safeNumber);
        } else {
            count = userService.countByUsernameLike(keyword);
            users = userService.findByUsernameLikePaged(keyword, offset, safeNumber);
        }

        markFollows(currentUser.getUserId(), users);
        return new UserListPage(users, keyword, count, Paginator.of(safePage, safeNumber, count, "/user/list"));
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

    public UserProfileView loadProfile(Integer userId) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在。");
        }
        List<LogDocumentView> activities = logDocumentMapper.findByUserId(userId, 0, 10);
        return new UserProfileView(user, activities, activities.size());
    }

    public UserFollowView loadUserFollowView(Integer profileUserId, Integer loginUserId) {
        User user = userService.findActiveById(profileUserId);
        if (user == null) {
            throw new IllegalStateException("用户不存在。");
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

    public FollowDocView loadFollowDocs(Integer userId) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在。");
        }
        List<Follow> follows = followService.findByUserIdAndType(userId, FollowService.TYPE_DOC);
        List<String> docIds = follows.stream().map(Follow::getObjectId).toList();
        List<Document> documents = docIds.isEmpty() ? List.of() : documentMapper.findActiveByIds(docIds);
        Map<String, Follow> followIndex = follows.stream().collect(java.util.stream.Collectors.toMap(Follow::getObjectId, follow -> follow, (left, right) -> left));
        List<FollowedDocument> items = documents.stream()
            .map(document -> new FollowedDocument(document, followIndex.get(document.getDocumentId()).getFollowId()))
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

    public record UserListPage(List<User> users, String username, long count, Paginator paginator) {
    }

    public record FollowUserListPage(List<User> users, int count) {
    }

    public record UserProfileView(User user, List<LogDocumentView> logDocuments, int count) {
    }

    public record UserFollowView(User user, List<User> users, List<User> fansUsers, int followCount, int fansCount, Integer loginUserId) {
    }

    public record FollowedDocument(Document document, Integer followId) {
    }

    public record FollowDocView(User user, List<FollowedDocument> pages, int count) {
    }
}
