package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Contact;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.ContactMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class ContactService {

    @Autowired
    private ContactMapper contactMapper;
    @Autowired
    private UserService userService;

    public List<Contact> findAll() {
        return contactMapper.findAll();
    }

    public Contact findById(Integer contactId) {
        return contactId == null ? null : contactMapper.findById(contactId);
    }

    public JsonResponse<Void> save(Contact contact) {
        JsonResponse<Void> validation = validate(contact);
        if (validation != null) {
            return validation;
        }
        int now = Math.toIntExact(Instant.now().getEpochSecond());
        contact.setCreateTime(now);
        contact.setUpdateTime(now);
        contactMapper.insert(contact);
        return JsonResponse.success("添加联系人成功", null, "/system/contact/list", 2000);
    }

    public JsonResponse<Void> update(Contact contact) {
        if (contact.getContactId() == null || findById(contact.getContactId()) == null) {
            return JsonResponse.error("联系人不存在。", null, "", 2000);
        }
        JsonResponse<Void> validation = validate(contact);
        if (validation != null) {
            return validation;
        }
        contact.setUpdateTime(Math.toIntExact(Instant.now().getEpochSecond()));
        contactMapper.update(contact);
        return JsonResponse.success("修改联系人成功", null, "/system/contact/list", 2000);
    }

    public JsonResponse<Void> delete(Integer contactId) {
        if (findById(contactId) == null) {
            return JsonResponse.error("联系人不存在。", null, "", 2000);
        }
        contactMapper.deleteById(contactId);
        return JsonResponse.success("删除联系人成功", null, "/system/contact/list", 2000);
    }

    public ImportPage importCandidates(String username, int page, int number) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String search = username == null ? "" : username.trim();
        long count = search.isEmpty() ? userService.countAllActive() : userService.countByUsernameLike(search);
        List<User> users = search.isEmpty()
            ? userService.findAllActivePaged(offset, safeNumber)
            : userService.findByUsernameLikePaged(search, offset, safeNumber);
        return new ImportPage(users, search, Paginator.of(safePage, safeNumber, count, "/system/contact/import?username=" + search));
    }

    private JsonResponse<Void> validate(Contact contact) {
        if (contact == null) {
            return JsonResponse.error("联系人参数错误。", null, "", 2000);
        }
        if (!StringUtils.hasText(contact.getName())) {
            return JsonResponse.error("联系人姓名不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(contact.getPosition())) {
            return JsonResponse.error("职位不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(contact.getMobile())) {
            return JsonResponse.error("联系电话不能为空。", null, "", 2000);
        }
        if (!StringUtils.hasText(contact.getEmail()) || !contact.getEmail().contains("@")) {
            return JsonResponse.error("邮箱格式不正确。", null, "", 2000);
        }
        contact.setName(contact.getName().trim());
        contact.setPosition(contact.getPosition().trim());
        contact.setMobile(contact.getMobile().trim());
        contact.setEmail(contact.getEmail().trim());
        return null;
    }

    public record ImportPage(List<User> users, String username, Paginator paginator) {
    }
}
