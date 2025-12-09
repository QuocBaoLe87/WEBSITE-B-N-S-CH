package com.v.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Xử lý login success:
 * - Redirect theo role (admin → /admin/dashboard, user → /books)
 */
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        log.info("✅ Login success for user: {}", authentication.getName());

        // Xác định target URL dựa trên role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String targetUrl = isAdmin ? "/admin/dashboard" : "/books";

        log.info("📍 Redirecting to: {}", targetUrl);

        // Clear bất kỳ saved request nào để tránh conflict
        request.getSession().removeAttribute("SPRING_SECURITY_SAVED_REQUEST");

        // Redirect trực tiếp và đảm bảo response được flush
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
