package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public abstract class ControllerSupport {

    protected CurrentUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (CurrentUser) session.getAttribute(AuthInterceptor.SESSION_AUTHOR);
    }

    protected void nav(Model model, String navName) {
        model.addAttribute("navName", navName);
    }
}
