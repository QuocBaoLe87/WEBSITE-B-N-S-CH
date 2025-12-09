package com.v.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Xử lý sau khi logout thành công - chuyển hướng đến trang đăng nhập
 * Đảm bảo dọn dẹp toàn bộ session attributes trước khi logout
 */
@Component
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    public LogoutSuccessHandler() {
        setDefaultTargetUrl("/login");
        // Cho phép redirect đến trang đăng nhập thay vì trang trước logout
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        // Spring Security sẽ tự động invalidate session sau đây
        super.onLogoutSuccess(request, response, authentication);
    }
}
