package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.Follow;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class SystemProfileService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Autowired
    private UserService userService;
    @Autowired
    private FollowService followService;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private LogDocumentMapper logDocumentMapper;
    @Autowired
    private ConfigService configService;

    /**
     * 加载个人中心首页资料与最近动态。
     */
    public ProfileInfoView loadInfo(Integer userId) {
        User user = requireUser(userId);
        List<LogDocumentView> logs = decorateLogs(logDocumentMapper.findByUserId(userId, 0, 10));
        return new ProfileInfoView(user, logs, logs.size());
    }

    /**
     * 加载可编辑的个人资料。
     */
    public User loadEditableProfile(Integer userId) {
        return requireUser(userId);
    }

    /**
     * 保存当前用户的个人资料。
     */
    public JsonResponse<Void> modifyProfile(
        Integer userId,
        String givenName,
        String email,
        String mobile,
        String phone,
        String department,
        String position,
        String location,
        String im
    ) {
        if (!StringUtils.hasText(givenName)) {
            return JsonResponse.error("姓名不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(email)) {
            return JsonResponse.error("邮箱不能为空。", null, "", 2000);
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return JsonResponse.error("邮箱格式不正确。", null, "", 2000);
        }
        if (!StringUtils.hasText(mobile)) {
            return JsonResponse.error("手机号不能为空。", null, "", 2000);
        }

        userService.updateProfile(
            userId,
            givenName.trim(),
            email.trim(),
            mobile.trim(),
            trim(phone),
            trim(department),
            trim(position),
            trim(location),
            trim(im)
        );
        return JsonResponse.success("个人资料修改成功", null, "/system/profile/info", 2000);
    }

    /**
     * 加载当前用户关注与粉丝信息。
     */
    public FollowUserView loadFollowUsers(Integer userId) {
        User user = requireUser(userId);

        List<Follow> follows = followService.findByUserIdAndType(userId, FollowService.TYPE_USER);
        List<Integer> followedIds = follows.stream()
            .map(item -> Integer.valueOf(item.getObjectId()))
            .toList();
        Map<String, Follow> followIndex = follows.stream()
            .collect(java.util.stream.Collectors.toMap(Follow::getObjectId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<User> users = new ArrayList<>();
        for (User followedUser : userService.findActiveByIds(followedIds)) {
            Follow follow = followIndex.get(String.valueOf(followedUser.getUserId()));
            if (follow != null) {
                followedUser.setFollowId(follow.getFollowId());
                followedUser.setFollow(true);
                users.add(followedUser);
            }
        }

        List<Follow> fans = followService.findByObjectIdAndType(String.valueOf(userId), FollowService.TYPE_USER);
        List<Integer> fanIds = fans.stream().map(Follow::getUserId).toList();
        List<User> fansUsers = userService.findActiveByIds(fanIds);
        return new FollowUserView(user, users, fansUsers, users.size(), fansUsers.size());
    }

    /**
     * 分页加载当前用户关注的文档。
     */
    public FollowDocPage loadFollowDocs(Integer userId, int page, int number) {
        User user = requireUser(userId);
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));

        List<Follow> follows = followService.findByUserIdAndType(userId, FollowService.TYPE_DOC);
        List<String> docIds = follows.stream().map(Follow::getObjectId).toList();
        Map<String, Document> docs = documentMapper.findActiveByIds(docIds).stream()
            .collect(java.util.stream.Collectors.toMap(Document::getDocumentId, item -> item, (left, right) -> left));

        List<FollowedDocument> items = new ArrayList<>();
        for (Follow follow : follows) {
            Document document = docs.get(follow.getObjectId());
            if (document != null) {
                items.add(new FollowedDocument(document, follow.getFollowId(), TimeUtils.formatUnix(document.getUpdateTime())));
            }
        }

        int offset = (safePage - 1) * safeNumber;
        List<FollowedDocument> pageItems = offset >= items.size()
            ? List.of()
            : items.subList(offset, Math.min(items.size(), offset + safeNumber));

        return new FollowDocPage(
            user,
            pageItems,
            items.size(),
            configService.getValue("auto_follow_doc_open", "0"),
            Paginator.of(safePage, safeNumber, items.size(), "/system/profile/followDoc")
        );
    }

    /**
     * 分页加载当前用户的文档操作动态。
     */
    public ActivityPage loadActivity(Integer userId, String keyword, int page, int number) {
        requireUser(userId);
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String search = trim(keyword);

        List<LogDocumentView> logs;
        long count;
        if (search.isBlank()) {
            logs = logDocumentMapper.findByUserId(userId, offset, safeNumber);
            count = logDocumentMapper.countByUserId(userId);
        } else {
            logs = logDocumentMapper.findByUserIdAndKeyword(userId, search, offset, safeNumber);
            count = logDocumentMapper.countByUserIdAndKeyword(userId, search);
        }
        logs = decorateLogs(logs);
        return new ActivityPage(logs, search, Paginator.of(safePage, safeNumber, count, "/system/profile/activity?keyword=" + search));
    }

    /**
     * 校验旧密码并保存新密码。
     */
    public JsonResponse<Void> savePassword(Integer userId, String password, String passwordNew, String passwordConfirm) {
        if (!StringUtils.hasText(password) || !StringUtils.hasText(passwordNew) || !StringUtils.hasText(passwordConfirm)) {
            return JsonResponse.error("密码不能为空。", null, "", 2000);
        }
        User user = requireUser(userId);
        String currentEncoded = userService.encodePassword(password);
        if (!currentEncoded.equals(user.getPassword())) {
            return JsonResponse.error("当前密码错误。", null, "", 2000);
        }
        if (!passwordNew.equals(passwordConfirm)) {
            return JsonResponse.error("确认密码和新密码不一致。", null, "", 2000);
        }
        userService.updatePassword(userId, userService.encodePassword(passwordNew));
        return JsonResponse.success("密码修改成功，下次登录生效。", null, "/system/profile/password", 2000);
    }

    private User requireUser(Integer userId) {
        User user = userService.findActiveById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在。");
        }
        return user;
    }

    private List<LogDocumentView> decorateLogs(List<LogDocumentView> logs) {
        for (LogDocumentView log : logs) {
            log.setCreateTimeText(TimeUtils.formatUnix(log.getCreateTime()));
        }
        return logs;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public record ProfileInfoView(User user, List<LogDocumentView> logDocuments, int count) {
    }

    public record FollowUserView(User user, List<User> users, List<User> fansUsers, int followCount, int fansCount) {
    }

    public record FollowedDocument(Document document, Integer followId, String updateTimeText) {
    }

    public record FollowDocPage(User user, List<FollowedDocument> followDocuments, int count, String autoFollowDoc, Paginator paginator) {
    }

    public record ActivityPage(List<LogDocumentView> logDocuments, String keyword, Paginator paginator) {
    }
}
