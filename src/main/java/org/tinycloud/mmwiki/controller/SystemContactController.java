package org.tinycloud.mmwiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinycloud.mmwiki.domain.Contact;
import org.tinycloud.mmwiki.service.ContactService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

/**
 * MM-Wiki 页面与接口控制器。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Controller
public class SystemContactController extends ControllerSupport {

    @Autowired
    private ContactService contactService;

    @GetMapping("/system/contact/list")
    public String list(Model model) {
        model.addAttribute("contacts", contactService.findAll());
        return "system/contact/list";
    }

    @GetMapping("/system/contact/add")
    public String add() {
        return "system/contact/form";
    }

    @GetMapping("/system/contact/edit")
    public String edit(@RequestParam("contact_id") Integer contactId, Model model) {
        Contact contact = contactService.findById(contactId);
        if (contact == null) {
            throw new IllegalStateException("联系人不存在。");
        }
        model.addAttribute("contact", contact);
        return "system/contact/form";
    }

    @PostMapping("/system/contact/save")
    @ResponseBody
    public JsonResponse<Void> save(Contact contact) {
        return contactService.save(contact);
    }

    @PostMapping("/system/contact/modify")
    @ResponseBody
    public JsonResponse<Void> modify(Contact contact) {
        return contactService.update(contact);
    }

    @PostMapping("/system/contact/delete")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam("contact_id") Integer contactId) {
        return contactService.delete(contactId);
    }

    @GetMapping("/system/contact/import")
    public String importPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int number,
        @RequestParam(defaultValue = "") String username,
        Model model
    ) {
        ContactService.ImportPage view = contactService.importCandidates(username, page, number);
        model.addAttribute("users", view.users());
        model.addAttribute("username", view.username());
        model.addAttribute("paginator", view.paginator());
        return "system/contact/import";
    }
}
