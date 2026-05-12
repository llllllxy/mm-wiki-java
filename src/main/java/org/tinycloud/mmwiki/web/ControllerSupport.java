package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * MM-Wiki Web 层支持组件。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public abstract class ControllerSupport {

    protected CurrentUser currentUser() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            return null;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) attributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        HttpSession session = request.getSession(false);
        return session == null ? null : (CurrentUser) session.getAttribute(AuthInterceptor.SESSION_AUTHOR);
    }

    protected void nav(Model model, String navName) {
        model.addAttribute("navName", navName);
    }
}
