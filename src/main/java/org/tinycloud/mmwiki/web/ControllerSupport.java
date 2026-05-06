package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public abstract class ControllerSupport {

    protected CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTR);
    }

    protected void nav(Model model, String navName) {
        model.addAttribute("navName", navName);
    }
}
