package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
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
 * 系统联系人服务，负责联系人分页查询、新增、修改、删除和导入校验。
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

    /**
     * 查询全部联系人记录。
     *
     * @return 联系人列表
     */
    public List<Contact> findAll() {
        return contactMapper.findAll();
    }

    /**
     * 分页查询联系人，支持按姓名、职位、电话、邮箱模糊过滤。
     *
     * @param keyword  查询关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 联系人分页数据
     */
    public PageModel<Contact> pageModel(String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        Page<Contact> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> contactMapper.pageByKeyword(search));
        return PageModel.from(pageInfo);
    }

    /**
     * 根据联系人ID查询联系人。
     *
     * @param contactId 联系人ID
     * @return 联系人记录，不存在时返回 null
     */
    public Contact findById(Integer contactId) {
        return contactId == null ? null : contactMapper.findById(contactId);
    }

    /**
     * 新增联系人，保存前会校验姓名、职位、电话和邮箱。
     *
     * @param contact 待新增的联系人信息
     * @return 前端统一 JSON 响应
     */
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

    /**
     * 更新联系人，要求目标联系人存在并通过字段校验。
     *
     * @param contact 待更新的联系人信息
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> update(Contact contact) {
        if (contact.getContactId() == null || findById(contact.getContactId()) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "联系人不存在。");
        }
        JsonResponse<Void> validation = validate(contact);
        if (validation != null) {
            return validation;
        }
        contact.setUpdateTime(TimeUtils.now());
        contactMapper.update(contact);
        return JsonResponse.success("修改联系人成功", "/system/contact/list");
    }

    /**
     * 删除指定联系人，删除前会确认记录存在。
     *
     * @param contactId 联系人ID
     * @return 前端统一 JSON 响应
     */
    public JsonResponse<Void> delete(Integer contactId) {
        if (findById(contactId) == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "联系人不存在。");
        }
        contactMapper.deleteById(contactId);
        return JsonResponse.success("删除联系人成功", "/system/contact/list");
    }

    /**
     * 分页查询可导入为联系人的活跃用户，支持按用户名模糊过滤。
     *
     * @param username 用户名关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 可导入用户分页数据
     */
    public PageModel<User> importCandidatePage(String username, int pageNum, int pageSize) {
        String search = username == null ? "" : username.trim();
        Page<User> pageInfo = PaginateRequest.of(pageNum, pageSize)
                .request(() -> {
                    if (search.isEmpty()) {
                        userService.pageAllActive();
                    } else {
                        userService.pageByUsernameLike(search);
                    }
                });
        return PageModel.from(pageInfo);
    }

    /**
     * 校验并规范化联系人字段，包含必填项和邮箱格式检查。
     *
     * @param contact 待校验的联系人信息
     * @return 校验通过时返回 null，校验失败时抛出业务异常
     */
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
