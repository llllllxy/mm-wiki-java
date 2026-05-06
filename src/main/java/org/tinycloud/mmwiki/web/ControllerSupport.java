package org.tinycloud.mmwiki.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

public abstract class ControllerSupport {

    protected CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTR);
    }

    protected void nav(Model model, String navName) {
        model.addAttribute("navName", navName);
    }
}
