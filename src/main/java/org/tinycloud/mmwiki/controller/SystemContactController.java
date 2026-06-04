package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.constant.ErrorCodeEnum;
import org.tinycloud.mmwiki.domain.Contact;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.service.ContactService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * 系统联系人 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
@RequestMapping("/system/contact")
public class SystemContactController extends ControllerSupport {

    @Autowired
    private ContactService contactService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("contacts", contactService.findAll());
        return "system/contact/list";
    }

    @GetMapping("/add")
    public String add() {
        return "system/contact/form";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("contact_id") Integer contactId, Model model) {
        Contact contact = contactService.findById(contactId);
        if (contact == null) {
            throw new SystemException(ErrorCodeEnum.NOT_FOUND, "联系人不存在。");
        }
        model.addAttribute("contact", contact);
        return "system/contact/form";
    }

    @PostMapping("/save")
    @ResponseBody
    public JsonResponse<Void> save(Contact contact) {
        return contactService.save(contact);
    }

    @PostMapping("/modify")
    @ResponseBody
    public JsonResponse<Void> modify(Contact contact) {
        return contactService.update(contact);
    }

    @PostMapping("/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("contact_id") Integer contactId) {
        return contactService.delete(contactId);
    }

    @GetMapping("/import")
    public String importPage(@RequestParam(defaultValue = "") String username, Model model) {
        model.addAttribute("username", username == null ? "" : username.trim());
        return "system/contact/import";
    }

    @PostMapping("/import")
    @ResponseBody
    public JsonResponse<PageModel<User>> importData(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "20") int pageSize,
                                                    @RequestParam(defaultValue = "") String username) {
        return JsonResponse.success("查询成功", contactService.importCandidatePage(username, pageNum, pageSize));
    }
}
