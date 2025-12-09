package com.v.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter này force tạo CSRF cookie ngay khi request đến,
 * thay vì đợi đến khi token được sử dụng.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // Lấy CSRF token từ request attribute
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

        // Chỉ cần gọi getToken() là CookieCsrfTokenRepository sẽ tự động tạo cookie
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        filterChain.doFilter(request, response);
    }
}
