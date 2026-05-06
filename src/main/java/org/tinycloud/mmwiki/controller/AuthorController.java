package org.tinycloud.mmwiki.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.tinycloud.mmwiki.service.AuthService;
import org.tinycloud.mmwiki.web.ControllerSupport;
import org.tinycloud.mmwiki.web.JsonResponse;

@Controller
@RequestMapping("/author")
public class AuthorController extends ControllerSupport {

    private final AuthService authService;

    public AuthorController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("sso_open", authService.isSsoOpen() ? "1" : "0");
        return "author/index";
    }

    @PostMapping("/login")
    @ResponseBody
    public JsonResponse<Void> login(
        @RequestParam String username,
        @RequestParam String password,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return authService.login(username, password, request, response);
    }

    @PostMapping("/authLogin")
    @ResponseBody
    public JsonResponse<Void> authLogin() {
        return authService.authLoginStub();
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return "redirect:/author/index";
    }
}
