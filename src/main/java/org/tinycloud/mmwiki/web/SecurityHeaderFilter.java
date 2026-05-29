package org.tinycloud.mmwiki.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 统一写入基础安全响应头。
 */
@Component
public class SecurityHeaderFilter extends OncePerRequestFilter {

    /**
     * 为每个响应添加 Referrer-Policy，降低页面地址和资源地址通过 Referer 泄露到外站的概率。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "0");
        filterChain.doFilter(request, response);
    }
}
