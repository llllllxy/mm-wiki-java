package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.util.TimeUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.Contact;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.ContactMapper;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;


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
        LocalDateTime now = LocalDateTime.now();
        contact.setCreateTime(now);
        contact.setUpdateTime(now);
        contactMapper.insert(contact);
        return JsonResponse.success("添加联系人成功", "/system/contact/list");
    }

    public JsonResponse<Void> update(Contact contact) {
        if (contact.getContactId() == null || findById(contact.getContactId()) == null) {
            throw new SystemException("联系人不存在。");
        }
        JsonResponse<Void> validation = validate(contact);
        if (validation != null) {
            return validation;
        }
        contact.setUpdateTime(TimeUtils.now());
        contactMapper.update(contact);
        return JsonResponse.success("修改联系人成功", "/system/contact/list");
    }

    public JsonResponse<Void> delete(Integer contactId) {
        if (findById(contactId) == null) {
            throw new SystemException("联系人不存在。");
        }
        contactMapper.deleteById(contactId);
        return JsonResponse.success("删除联系人成功", "/system/contact/list");
    }

    public PageModel<User> importCandidatePage(String username, int pageNum, int pageSize) {
        String search = username == null ? "" : username.trim();
        PageInfo<User> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> {
                    if (search.isEmpty()) {
                        userService.pageAllActive();
                    } else {
                        userService.pageByUsernameLike(search);
                    }
                });
        return PageModel.from(pageInfo);
    }

    private JsonResponse<Void> validate(Contact contact) {
        if (contact == null) {
            throw new SystemException("联系人参数错误。");
        }
        if (!StringUtils.hasText(contact.getName())) {
            throw new SystemException("联系人姓名不能为空。");
        }
        if (!StringUtils.hasText(contact.getPosition())) {
            throw new SystemException("职位不能为空。");
        }
        if (!StringUtils.hasText(contact.getMobile())) {
            throw new SystemException("联系电话不能为空。");
        }
        if (!StringUtils.hasText(contact.getEmail()) || !contact.getEmail().contains("@")) {
            throw new SystemException("邮箱格式不正确。");
        }
        contact.setName(contact.getName().trim());
        contact.setPosition(contact.getPosition().trim());
        contact.setMobile(contact.getMobile().trim());
        contact.setEmail(contact.getEmail().trim());
        return null;
    }
}
