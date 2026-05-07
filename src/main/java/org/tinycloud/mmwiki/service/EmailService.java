package org.tinycloud.mmwiki.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
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

    private final EmailMapper emailMapper;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final ConfigService configService;
    private final TemplateEngine templateEngine;
    private final MmwikiProperties properties;

    public EmailService(
            EmailMapper emailMapper,
            FollowMapper followMapper,
            UserMapper userMapper,
            ConfigService configService,
            TemplateEngine templateEngine,
            MmwikiProperties properties
    ) {
        this.emailMapper = emailMapper;
        this.followMapper = followMapper;
        this.userMapper = userMapper;
        this.configService = configService;
        this.templateEngine = templateEngine;
        this.properties = properties;
    }

    /**
     * 按名称模糊查询邮件服务器配置。
     */
    public List<EmailServer> list(String keyword) {
        String search = keyword == null ? "" : keyword.trim();
        return search.isEmpty() ? emailMapper.findAll() : emailMapper.findByNameLike(search);
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
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        emailServer.setCreateTime(now);
        emailServer.setUpdateTime(now);
        emailServer.setIsUsed(0);
        emailMapper.insert(emailServer);
        return JsonResponse.success("添加邮件服务器成功", null, "/system/email/list", 2000);
    }

    /**
     * 更新邮件服务器配置。
     */
    public JsonResponse<Void> update(EmailServer emailServer) {
        if (emailServer.getEmailId() == null || findById(emailServer.getEmailId()) == null) {
            return JsonResponse.error("邮件服务器不存在。", null, "", 2000);
        }
        JsonResponse<Void> validation = validate(emailServer, emailServer.getEmailId());
        if (validation != null) {
            return validation;
        }
        emailServer.setUpdateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        emailMapper.update(emailServer);
        return JsonResponse.success("修改邮件服务器成功", null, "/system/email/list", 2000);
    }

    /**
     * 将指定邮件服务器设置为启用状态。
     */
    public JsonResponse<Void> markUsed(Integer emailId) {
        EmailServer email = findById(emailId);
        if (email == null) {
            return JsonResponse.error("邮件服务器不存在。", null, "", 2000);
        }
        emailMapper.clearUsed();
        emailMapper.markUsed(emailId);
        return JsonResponse.success("启用邮件服务器成功", null, "/system/email/list", 2000);
    }

    /**
     * 删除邮件服务器配置。
     */
    public JsonResponse<Void> delete(Integer emailId) {
        EmailServer email = findById(emailId);
        if (email == null) {
            return JsonResponse.error("邮件服务器不存在。", null, "", 2000);
        }
        emailMapper.deleteById(emailId);
        return JsonResponse.success("删除邮件服务器成功", null, "/system/email/list", 2000);
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
            return JsonResponse.error("收件人邮箱地址不能为空。", null, "", 2000);
        }

        Context context = baseContext();
        context.setVariable("document_name", "MM-Wiki 邮件服务器测试");
        context.setVariable("username", "System");
        context.setVariable("update_time", TimeUtils.formatUnix(Math.toIntExact(Instant.now().getEpochSecond())));
        context.setVariable("document_content", "如果你收到这封邮件，说明当前 SMTP 邮件服务器配置可用。");
        String body = templateEngine.process(TEST_NOTICE_TEMPLATE, context);

        boolean sent = send(emailServer, recipients, "测试邮件服务器", body);
        if (!sent) {
            return JsonResponse.error("测试邮件发送失败，请检查 SMTP 地址、端口、SSL、发件邮箱和授权码。", null, "", 2000);
        }
        return JsonResponse.success("测试邮件发送成功", null, "", 2000);
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
        context.setVariable("update_time", TimeUtils.formatUnix(document.getUpdateTime()));
        context.setVariable("document_content", abbreviate(content, 500));
        context.setVariable("comment", StringUtils.hasText(comment) ? comment : "无");
        context.setVariable("document_url", documentUrl);

        String body = templateEngine.process(DOCUMENT_NOTICE_TEMPLATE, context);
        boolean sent = send(emailServer, recipients, "文档更新通知", body);
        if (!sent) {
            log.error("更新文档时发送邮件通知失败，documentId={}, recipients={}", document.getDocumentId(), recipients);
        }
    }

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

    private String normalizeTitle(EmailServer emailServer, String title) {
        String prefix = StringUtils.hasText(emailServer.getSenderTitlePrefix()) ? emailServer.getSenderTitlePrefix() : "[MM-Wiki]";
        return prefix + title;
    }

    private Context baseContext() {
        Context context = new Context();
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        context.setVariable("now_time", TimeUtils.formatUnix(now));
        context.setVariable("copyright", properties.getCopyright());
        return context;
    }

    private List<String> findDocumentFollowerEmails(String documentId) {
        List<Follow> follows = followMapper.findByObjectIdAndType(documentId, FollowService.TYPE_DOC);
        if (follows.isEmpty()) {
            return List.of();
        }
        List<Integer> userIds = follows.stream()
                .map(Follow::getUserId)
                .filter(userId -> userId != null && userId > 0)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        Set<String> emails = new LinkedHashSet<>();
        for (User user : userMapper.findActiveByIds(userIds)) {
            if (StringUtils.hasText(user.getEmail())) {
                emails.add(user.getEmail().trim());
            }
        }
        return new ArrayList<>(emails);
    }

    private List<String> parseEmails(String emails) {
        if (!StringUtils.hasText(emails)) {
            return List.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String email : emails.split("[;,，；\\s]+")) {
            if (StringUtils.hasText(email)) {
                result.add(email.trim());
            }
        }
        return new ArrayList<>(result);
    }

    private String abbreviate(String content, int maxLength) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String clean = content.trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }

    private JsonResponse<Void> validate(EmailServer emailServer, Integer currentId) {
        if (emailServer == null) {
            return JsonResponse.error("邮件服务器参数错误。", null, "", 2000);
        }
        if (!StringUtils.hasText(emailServer.getName())) {
            return JsonResponse.error("邮件服务器名称不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(emailServer.getHost())) {
            return JsonResponse.error("邮件服务器主机不能为空。", null, "", 2000);
        }
        if (emailServer.getPort() == null || emailServer.getPort() <= 0 || emailServer.getPort() > 65535) {
            return JsonResponse.error("邮件服务器端口格式不正确。", null, "", 2000);
        }
        if (!StringUtils.hasText(emailServer.getSenderAddress())) {
            return JsonResponse.error("发件人邮箱不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(emailServer.getUsername())) {
            return JsonResponse.error("邮箱用户名不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(emailServer.getPassword())) {
            return JsonResponse.error("邮箱密码不能为空。", null, "", 2000);
        }
        long duplicateCount = currentId == null
            ? emailMapper.countByName(emailServer.getName().trim())
            : emailMapper.countByNameAndNotId(currentId, emailServer.getName().trim());
        if (duplicateCount > 0) {
            return JsonResponse.error("邮件服务器名称已经存在。", null, "", 2000);
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
