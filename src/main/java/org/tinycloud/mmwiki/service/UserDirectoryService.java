package org.tinycloud.mmwiki.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.FollowTypeEnum;
import org.tinycloud.mmwiki.domain.Follow;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.vo.UserFollowedDocument;
import org.tinycloud.mmwiki.vo.UserProfileView;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.PageModel;
import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;

/**
 * 前台用户目录服务，负责用户列表、用户主页、关注用户和关注文档分页数据。
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
    private FollowMapper followMapper;

    /**
     * 分页查询用户目录，并标记当前登录用户是否已关注每个用户。
     *
     * @param currentUser 当前登录用户
     * @param username    用户名关键字
     * @param pageNum     当前页码
     * @param pageSize    每页数量
     * @return 用户分页数据
     */
    public PageModel<User> userPage(CurrentUser currentUser, String username, int pageNum, int pageSize) {
        String keyword = username == null ? "" : username.trim();
        Page<User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> {
                    if (keyword.isBlank()) {
                        this.userService.pageAllActive();
                    } else {
                        this.userService.pageByUsernameLike(keyword);
                    }
                });
        this.markFollows(currentUser.getUserId(), pageInfo.getRecords());
        return PageModel.from(pageInfo);
    }

    /**
     * 加载前台用户页头所需的有效用户信息，用户不存在时抛出业务异常。
     *
     * @param userId 用户ID
     * @return 有效用户
     */
    public User loadActiveUser(Integer userId) {
        User user = this.userService.findActiveById(userId);
        if (user == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在。");
        }
        return user;
    }

    /**
     * 分页查询当前登录用户关注的用户列表。
     *
     * @param userId   当前登录用户ID
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 关注用户分页数据
     */
    public PageModel<User> followedUserPage(Integer userId, int pageNum, int pageSize) {
        Page<User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.followMapper.pageFollowedUsers(userId, FollowTypeEnum.USER.getCode()));
        return PageModel.from(pageInfo);
    }

    /**
     * 加载用户主页资料和当前用户可见的最近文档动态。
     *
     * @param userId      被访问用户ID
     * @param currentUser 当前登录用户
     * @return 用户主页视图数据
     */
    public UserProfileView loadProfile(Integer userId, CurrentUser currentUser) {
        User user = this.userService.findActiveById(userId);
        if (user == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在。");
        }
        Page<LogDocumentView> pageInfo = PaginateRequest.of(1, 10)
                .request(() -> this.logDocumentMapper.pageByUserIdVisibleToViewer(userId, currentUser.getUserId(), AccessService.isRoot(currentUser), ""));
        List<LogDocumentView> activities = pageInfo.getRecords();
        return new UserProfileView(user, activities, activities.size());
    }

    /**
     * 分页查询用户主页中的关注用户列表。
     *
     * @param profileUserId 被访问用户ID
     * @param pageNum       当前页码
     * @param pageSize      每页数量
     * @return 关注用户分页数据
     */
    public PageModel<User> profileFollowedUserPage(Integer profileUserId, int pageNum, int pageSize) {
        this.loadActiveUser(profileUserId);
        Page<User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.followMapper.pageFollowedUsers(profileUserId, FollowTypeEnum.USER.getCode()));
        return PageModel.from(pageInfo);
    }

    /**
     * 分页查询用户主页中的粉丝用户列表。
     *
     * @param profileUserId 被访问用户ID
     * @param pageNum       当前页码
     * @param pageSize      每页数量
     * @return 粉丝用户分页数据
     */
    public PageModel<User> profileFansUserPage(Integer profileUserId, int pageNum, int pageSize) {
        this.loadActiveUser(profileUserId);
        Page<User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.followMapper.pageFansUsers(String.valueOf(profileUserId), FollowTypeEnum.USER.getCode()));
        return PageModel.from(pageInfo);
    }

    /**
     * 分页查询用户关注的文档，并按当前登录用户的空间权限过滤不可见文档。
     *
     * @param userId      被访问用户ID
     * @param currentUser 当前登录用户
     * @param pageNum     当前页码
     * @param pageSize    每页数量
     * @return 关注文档分页数据
     */
    public PageModel<UserFollowedDocument> followDocPage(Integer userId, CurrentUser currentUser, int pageNum, int pageSize) {
        this.loadActiveUser(userId);
        Page<UserFollowedDocument> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.followMapper.pageFollowedDocuments(
                        userId,
                        FollowTypeEnum.DOCUMENT.getCode(),
                        currentUser.getUserId(),
                        AccessService.isRoot(currentUser)
                ));
        return PageModel.from(pageInfo);
    }

    /**
     * 为用户列表标记当前登录用户的关注状态和关注记录ID。
     *
     * @param loginUserId 当前登录用户ID
     * @param users       待标记的用户列表
     */
    private void markFollows(Integer loginUserId, List<User> users) {
        Map<String, Follow> followIndex = this.followService.indexByObjectId(loginUserId, FollowTypeEnum.USER.getCode());
        for (User user : users) {
            Follow follow = followIndex.get(String.valueOf(user.getUserId()));
            if (follow != null) {
                user.setFollow(true);
                user.setFollowId(follow.getFollowId());
            }
        }
    }
}
