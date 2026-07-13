package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.FollowTypeEnum;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.util.BCrypt;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.vo.FollowDocPage;
import org.tinycloud.mmwiki.vo.ProfileFollowedDocument;
import org.tinycloud.mmwiki.vo.ProfileInfoView;
import org.tinycloud.mmwiki.vo.UserFollowedDocument;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 个人中心服务，负责当前用户资料、关注、粉丝和个人文档动态的展示数据。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class SystemProfileService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Autowired
    private UserService userService;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private LogDocumentMapper logDocumentMapper;
    @Autowired
    private ConfigService configService;
    @Autowired
    private PasswordCryptoService passwordCryptoService;

    /**
     * 加载个人中心首页资料与最近动态。
     */
    public ProfileInfoView loadInfo(Integer userId) {
        User user = this.requireUser(userId);
        // 最近动态，查询前10条
        Page<LogDocumentView> pageInfo = PaginateRequest.of(1, 10)
                .request(() -> this.logDocumentMapper.pageByUserId(userId));
        List<LogDocumentView> logs = pageInfo.getRecords();
        logs.forEach(log -> log.setCreateTimeText(TimeUtils.format(log.getCreateTime())));
        return new ProfileInfoView(user, logs, logs.size());
    }

    /**
     * 加载可编辑的个人资料。
     */
    public User loadEditableProfile(Integer userId) {
        return this.requireUser(userId);
    }

    /**
     * 保存当前用户的个人资料。
     */
    public JsonResponse<Void> modifyProfile(Integer userId, String givenName,
                                            String email, String mobile,
                                            String phone, String department,
                                            String position, String location, String im) {
        if (!StringUtils.hasText(givenName)) {
            throw new SystemException("姓名不能为空。");
        }
        if (!StringUtils.hasText(email)) {
            throw new SystemException("邮箱不能为空。");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new SystemException("邮箱格式不正确。");
        }
        if (!StringUtils.hasText(mobile)) {
            throw new SystemException("手机号不能为空。");
        }

        this.userService.updateProfile(userId, givenName.trim(),
                email.trim(), mobile.trim(),
                phone == null ? "" : phone.trim(),
                department == null ? "" : department.trim(),
                position == null ? "" : position.trim(),
                location == null ? "" : location.trim(),
                im == null ? "" : im.trim()
        );
        return JsonResponse.success("个人资料修改成功", "/system/profile/info");
    }

    /**
     * 分页查询当前用户关注的用户列表，保留关注关系ID供前端取消关注。
     *
     * @param userId   当前用户ID
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 关注用户分页数据
     */
    public PageModel<User> loadFollowedUserPage(Integer userId, int pageNum, int pageSize) {
        this.requireUser(userId);
        Page<User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.followMapper.pageFollowedUsers(userId, FollowTypeEnum.USER.getCode()));
        return PageModel.from(pageInfo);
    }

    /**
     * 分页查询当前用户的粉丝列表。
     *
     * @param userId   当前用户ID
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 粉丝用户分页数据
     */
    public PageModel<User> loadFansUserPage(Integer userId, int pageNum, int pageSize) {
        this.requireUser(userId);
        Page<User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.followMapper.pageFansUsers(String.valueOf(userId), FollowTypeEnum.USER.getCode()));
        return PageModel.from(pageInfo);
    }

    /**
     * 加载当前用户关注文档页面的基础信息。
     */
    public FollowDocPage loadFollowDocs(Integer userId) {
        User user = this.requireUser(userId);
        return new FollowDocPage(user, this.configService.getValue("auto_follow_doc_open", "0"));
    }

    /**
     * 加载当前用户关注的文档列表。
     *
     * @param currentUser 会话用户
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @return 分页数据
     */
    public PageModel<ProfileFollowedDocument> loadFollowDocPage(CurrentUser currentUser, int pageNum, int pageSize) {
        this.requireUser(currentUser.getUserId());
        Page<UserFollowedDocument> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> this.followMapper.pageFollowedDocuments(
                        currentUser.getUserId(),
                        FollowTypeEnum.DOCUMENT.getCode(),
                        currentUser.getUserId(),
                        AccessService.isRoot(currentUser)
                ));
        List<ProfileFollowedDocument> records = pageInfo.getRecords().stream()
                .map(item -> new ProfileFollowedDocument(
                        item.getDocument(),
                        item.getFollowId(),
                        TimeUtils.format(item.getDocument().getUpdateTime())
                ))
                .toList();
        return PageModel.build(pageInfo.getPageNum(), pageInfo.getPageSize(), records, pageInfo.getTotal(), pageInfo.getPages());
    }

    /**
     * 分页加载指定用户的文档动态，支持按关键字过滤。
     *
     * @param userId   用户ID
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 文档动态分页数据
     */
    public PageModel<LogDocumentView> loadActivityPage(Integer userId, String keyword, int pageNum, int pageSize) {
        this.requireUser(userId);
        String search = keyword == null ? "" : keyword.trim();
        Page<LogDocumentView> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> {
                    if (search.isBlank()) {
                        this.logDocumentMapper.pageByUserId(userId);
                    } else {
                        this.logDocumentMapper.pageByUserIdAndKeyword(userId, search);
                    }
                });
        pageInfo.getRecords().forEach(log -> log.setCreateTimeText(TimeUtils.format(log.getCreateTime())));
        return PageModel.from(pageInfo);
    }

    /**
     * 校验旧密码并保存新密码。
     */
    public JsonResponse<Void> savePassword(Integer userId, String password, String passwordNew, String passwordConfirm) {
        if (!StringUtils.hasText(password) || !StringUtils.hasText(passwordNew) || !StringUtils.hasText(passwordConfirm)) {
            throw new SystemException("密码不能为空。");
        }
        String plainPassword = this.passwordCryptoService.decryptPassword(password);
        String plainPasswordNew = this.passwordCryptoService.decryptPassword(passwordNew);
        String plainPasswordConfirm = this.passwordCryptoService.decryptPassword(passwordConfirm);
        User user = this.requireUser(userId);
        boolean isMatch = BCrypt.checkpw(plainPassword, user.getPassword());
        if (!isMatch) {
            throw new SystemException("当前密码错误。");
        }
        if (!plainPasswordNew.equals(plainPasswordConfirm)) {
            throw new SystemException("确认密码和新密码不一致。");
        }
        this.userService.updatePassword(userId, BCrypt.hashpw(plainPasswordNew, BCrypt.gensalt()));
        return JsonResponse.success("密码修改成功，下次登录生效。", "/system/profile/password");
    }


    /**
     * 获取用户。
     *
     * @param userId 用户ID
     * @return 用户
     */
    private User requireUser(Integer userId) {
        User user = this.userService.findActiveById(userId);
        if (user == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "用户不存在！");
        }
        return user;
    }

}
