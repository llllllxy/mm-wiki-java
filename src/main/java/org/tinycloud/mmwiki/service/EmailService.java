package org.tinycloud.mmwiki.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.EmailServer;
import org.tinycloud.mmwiki.mapper.EmailMapper;
import org.tinycloud.mmwiki.web.JsonResponse;

@Service
public class EmailService {

    private final EmailMapper emailMapper;

    public EmailService(EmailMapper emailMapper) {
        this.emailMapper = emailMapper;
    }

    public List<EmailServer> list(String keyword) {
        String search = keyword == null ? "" : keyword.trim();
        return search.isEmpty() ? emailMapper.findAll() : emailMapper.findByNameLike(search);
    }

    public EmailServer findById(Integer emailId) {
        return emailId == null ? null : emailMapper.findById(emailId);
    }

    public EmailServer findUsed() {
        return emailMapper.findUsed();
    }

    public JsonResponse<Void> save(EmailServer emailServer) {
        JsonResponse<Void> validation = validate(emailServer, null);
        if (validation != null) {
            return validation;
        }
        if (emailMapper.countByName(emailServer.getName()) > 0) {
            return JsonResponse.error("邮件服务器名称已经存在。", null, "", 2000);
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        emailServer.setCreateTime(now);
        emailServer.setUpdateTime(now);
        emailServer.setIsUsed(0);
        emailMapper.insert(emailServer);
        return JsonResponse.success("添加邮件服务器成功", null, "/system/email/list", 2000);
    }

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

    public JsonResponse<Void> markUsed(Integer emailId) {
        EmailServer email = findById(emailId);
        if (email == null) {
            return JsonResponse.error("邮件服务器不存在。", null, "", 2000);
        }
        emailMapper.clearUsed();
        emailMapper.markUsed(emailId);
        return JsonResponse.success("启用邮件服务器成功", null, "/system/email/list", 2000);
    }

    public JsonResponse<Void> delete(Integer emailId) {
        EmailServer email = findById(emailId);
        if (email == null) {
            return JsonResponse.error("邮件服务器不存在。", null, "", 2000);
        }
        emailMapper.deleteById(emailId);
        return JsonResponse.success("删除邮件服务器成功", null, "/system/email/list", 2000);
    }

    public JsonResponse<Void> testSend(EmailServer emailServer, String emails) {
        JsonResponse<Void> validation = validate(emailServer, emailServer.getEmailId());
        if (validation != null) {
            return validation;
        }
        if (!StringUtils.hasText(emails)) {
            return JsonResponse.error("收件人邮箱地址不能为空。", null, "", 2000);
        }
        return JsonResponse.error("测试发信功能尚未迁移完成，请先保存配置后通过实际业务流程联调。", null, "", 2000);
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
            return JsonResponse.error("发件用户名不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(emailServer.getPassword())) {
            return JsonResponse.error("发件人密码不能为空。", null, "", 2000);
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
