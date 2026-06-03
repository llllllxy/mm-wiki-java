package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.constant.FollowTypeEnum;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.util.EmailUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.Document;
import org.tinycloud.mmwiki.domain.EmailServer;
import org.tinycloud.mmwiki.domain.Follow;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.EmailMapper;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;
import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;

/**
 * 邮件服务器配置、测试发信与文档更新通知服务。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String DOCUMENT_NOTICE_TEMPLATE = "system/email/template";
    private static final String TEST_NOTICE_TEMPLATE = "system/email/template_test";

    @Autowired
    private EmailMapper emailMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConfigService configService;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private MmwikiProperties properties;

    /**
     * 按名称模糊查询邮件服务器配置。
     */
    public List<EmailServer> list(String keyword) {
        String search = keyword == null ? "" : keyword.trim();
        return search.isEmpty() ? emailMapper.findAll() : emailMapper.findByNameLike(search);
    }

    /**
     * 分页查询邮件服务器配置。
     */
    public PageModel<EmailServer> pageModel(String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        Page<EmailServer> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> {
                    if (search.isEmpty()) {
                        emailMapper.findAll();
                    } else {
                        emailMapper.findByNameLike(search);
                    }
                });
        return PageModel.from(pageInfo);
    }

    /**
     * 查询指定邮件服务器配置。
     */
    public EmailServer findById(Integer emailId) {
        return emailId == null ? null : emailMapper.findById(emailId);
    }

    /**
     * 查询当前启用的邮件服务器配置。
     */
    public EmailServer findUsed() {
        return emailMapper.findUsed();
    }

    /**
     * 保存邮件服务器配置。
     */
    public JsonResponse<Void> save(EmailServer emailServer) {
        JsonResponse<Void> validation = validate(emailServer, null);
        if (validation != null) {
            return validation;
        }
        LocalDateTime now = LocalDateTime.now();
        emailServer.setCreateTime(now);
        emailServer.setUpdateTime(now);
        emailServer.setIsUsed(0);
        emailMapper.insert(emailServer);
        return JsonResponse.success("添加邮件服务器成功", "/system/email/list");
    }

    /**
     * 更新邮件服务器配置。
     */
    public JsonResponse<Void> update(EmailServer emailServer) {
        if (emailServer.getEmailId() == null || findById(emailServer.getEmailId()) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "邮件服务器不存在。");
        }
        JsonResponse<Void> validation = validate(emailServer, emailServer.getEmailId());
        if (validation != null) {
            return validation;
        }
        emailServer.setUpdateTime(TimeUtils.now());
        emailMapper.update(emailServer);
        return JsonResponse.success("修改邮件服务器成功", "/system/email/list");
    }

    /**
     * 将指定邮件服务器设置为启用状态。
     */
    @Transactional
    public JsonResponse<Void> markUsed(Integer emailId) {
        EmailServer email = findById(emailId);
        if (email == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "邮件服务器不存在。");
        }
        emailMapper.clearUsed();
        emailMapper.markUsed(emailId);
        return JsonResponse.success("启用邮件服务器成功", "/system/email/list");
    }

    /**
     * 删除邮件服务器配置。
     */
    public JsonResponse<Void> delete(Integer emailId) {
        EmailServer email = findById(emailId);
        if (email == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "邮件服务器不存在。");
        }
        emailMapper.deleteById(emailId);
        return JsonResponse.success("删除邮件服务器成功", "/system/email/list");
    }

    /**
     * 使用表单里的邮件服务器参数发送测试邮件。
     */
    public JsonResponse<Void> testSend(EmailServer emailServer, String emails) {
        JsonResponse<Void> validation = validate(emailServer, emailServer.getEmailId());
        if (validation != null) {
            return validation;
        }
        List<String> recipients = parseEmails(emails);
        if (recipients.isEmpty()) {
            throw new SystemException("收件人邮箱地址不能为空。");
        }

        Context context = baseContext();
        context.setVariable("document_name", "MM-Wiki 邮件服务器测试");
        context.setVariable("username", "System");
        context.setVariable("update_time", TimeUtils.format(TimeUtils.now()));
        context.setVariable("document_content", "如果你收到这封邮件，说明当前 SMTP 邮件服务器配置可用。");
        String body = templateEngine.process(TEST_NOTICE_TEMPLATE, context);

        boolean sent = send(emailServer, recipients, "测试邮件服务器", body);
        if (!sent) {
            throw new SystemException("测试邮件发送失败，请检查 SMTP 地址、端口、SSL、发件邮箱和授权码。");
        }
        return JsonResponse.success("测试邮件发送成功");
    }

    /**
     * 文档更新后，向所有关注该文档且配置了邮箱的用户发送通知。
     */
    public void sendDocumentUpdateNotice(Document document, String username, String content, String comment, String documentUrl) {
        if (document == null || !"1".equals(configService.getValue("send_email_open", "0"))) {
            return;
        }
        EmailServer emailServer = findUsed();
        if (emailServer == null) {
            return;
        }

        List<String> recipients = findDocumentFollowerEmails(document.getDocumentId());
        if (recipients.isEmpty()) {
            return;
        }

        Context context = baseContext();
        context.setVariable("document_name", document.getName());
        context.setVariable("username", StringUtils.hasText(username) ? username : "System");
        context.setVariable("update_time", TimeUtils.format(document.getUpdateTime()));
        context.setVariable("document_content", abbreviate(content, 500));
        context.setVariable("comment", StringUtils.hasText(comment) ? comment : "无");
        context.setVariable("document_url", documentUrl);

        String body = templateEngine.process(DOCUMENT_NOTICE_TEMPLATE, context);
        boolean sent = send(emailServer, recipients, "文档更新通知", body);
        if (!sent) {
            log.error("更新文档时发送邮件通知失败，documentId={}, recipients={}", document.getDocumentId(), recipients);
        }
    }

    /**
     * 使用指定邮件服务器发送 HTML 邮件。
     *
     * @param emailServer 邮件服务器配置
     * @param recipients  收件人邮箱列表
     * @param title       邮件标题
     * @param body        邮件正文
     * @return true 表示发送成功
     */
    private boolean send(EmailServer emailServer, List<String> recipients, String title, String body) {
        EmailUtils.EmailRequest request = EmailUtils.EmailRequest.builder()
                .account(emailServer.getSenderAddress())
                .password(emailServer.getPassword())
                .host(emailServer.getHost())
                .port(String.valueOf(emailServer.getPort()))
                .ssl(emailServer.getIsSsl() != null && emailServer.getIsSsl() == 1)
                .debug(false)
                .sendName(emailServer.getSenderName())
                .toMails(recipients)
                .title(normalizeTitle(emailServer, title))
                .content(body)
                .build();
        return EmailUtils.sendMsg(request);
    }

    /**
     * 为邮件标题添加配置的标题前缀，未配置时使用默认前缀。
     *
     * @param emailServer 邮件服务器配置
     * @param title       原始标题
     * @return 带前缀的邮件标题
     */
    private String normalizeTitle(EmailServer emailServer, String title) {
        String prefix = StringUtils.hasText(emailServer.getSenderTitlePrefix()) ? emailServer.getSenderTitlePrefix() : "[MM-Wiki]";
        return prefix + title;
    }

    /**
     * 构造邮件模板基础上下文，填充当前时间和版权信息。
     *
     * @return Thymeleaf 模板上下文
     */
    private Context baseContext() {
        Context context = new Context();
        LocalDateTime now = LocalDateTime.now();
        context.setVariable("now_time", TimeUtils.format(now));
        context.setVariable("copyright", properties.getCopyright());
        return context;
    }

    /**
     * 查询关注指定文档的有效用户邮箱，并去重后返回。
     *
     * @param documentId 文档ID
     * @return 关注用户邮箱列表
     */
    private List<String> findDocumentFollowerEmails(String documentId) {
        List<Follow> follows = followMapper.findByObjectIdAndType(documentId, FollowTypeEnum.DOCUMENT.getCode());
        if (follows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> userIds = follows.stream()
                .map(Follow::getUserId)
                .filter(userId -> userId != null && userId > 0)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> emails = new LinkedHashSet<>();
        for (User user : userMapper.findActiveByIds(userIds)) {
            if (StringUtils.hasText(user.getEmail())) {
                emails.add(user.getEmail().trim());
            }
        }
        return new ArrayList<>(emails);
    }

    /**
     * 解析用户输入的邮箱列表，支持逗号、分号和空白字符分隔。
     *
     * @param emails 邮箱字符串
     * @return 去重后的邮箱列表
     */
    private List<String> parseEmails(String emails) {
        if (!StringUtils.hasText(emails)) {
            return Collections.emptyList();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String email : emails.split("[;,，；\\s]+")) {
            if (StringUtils.hasText(email)) {
                result.add(email.trim());
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 截断文本内容到指定长度，空内容返回空字符串。
     *
     * @param content   原始内容
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    private String abbreviate(String content, int maxLength) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String clean = content.trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }

    /**
     * 校验并规范化邮件服务器配置，包含必填项、端口范围和名称唯一性。
     *
     * @param emailServer 待校验的邮件服务器配置
     * @param currentId   当前更新记录ID，新增时为 null
     * @return 校验通过时返回 null，校验失败时抛出业务异常
     */
    private JsonResponse<Void> validate(EmailServer emailServer, Integer currentId) {
        if (emailServer == null) {
            throw new SystemException("邮件服务器参数错误。");
        }
        if (!StringUtils.hasText(emailServer.getName())) {
            throw new SystemException("邮件服务器名称不能为空。");
        }
        if (!StringUtils.hasText(emailServer.getHost())) {
            throw new SystemException("邮件服务器主机不能为空。");
        }
        if (emailServer.getPort() == null || emailServer.getPort() <= 0 || emailServer.getPort() > 65535) {
            throw new SystemException("邮件服务器端口格式不正确。");
        }
        if (!StringUtils.hasText(emailServer.getSenderAddress())) {
            throw new SystemException("发件人邮箱不能为空。");
        }
        if (!StringUtils.hasText(emailServer.getUsername())) {
            throw new SystemException("邮箱用户名不能为空。");
        }
        if (!StringUtils.hasText(emailServer.getPassword())) {
            throw new SystemException("邮箱密码不能为空。");
        }
        long duplicateCount = currentId == null
            ? emailMapper.countByName(emailServer.getName().trim())
            : emailMapper.countByNameAndNotId(currentId, emailServer.getName().trim());
        if (duplicateCount > 0) {
            throw new SystemException("邮件服务器名称已经存在。");
        }
        emailServer.setName(emailServer.getName().trim());
        emailServer.setHost(emailServer.getHost().trim());
        emailServer.setSenderAddress(emailServer.getSenderAddress().trim());
        emailServer.setSenderName(StringUtils.hasText(emailServer.getSenderName()) ? emailServer.getSenderName().trim() : "MM-Wiki");
        emailServer.setSenderTitlePrefix(StringUtils.hasText(emailServer.getSenderTitlePrefix()) ? emailServer.getSenderTitlePrefix().trim() : "[MM-Wiki]");
        emailServer.setUsername(emailServer.getUsername().trim());
        emailServer.setPassword(emailServer.getPassword().trim());
        emailServer.setIsSsl(emailServer.getIsSsl() != null && emailServer.getIsSsl() == 1 ? 1 : 0);
        return null;
    }
}
