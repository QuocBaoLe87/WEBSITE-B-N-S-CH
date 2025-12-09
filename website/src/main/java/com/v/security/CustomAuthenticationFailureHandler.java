package com.v.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Xử lý login failure - log chi tiết lỗi
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    public CustomAuthenticationFailureHandler() {
        setDefaultFailureUrl("/login?error");
        setUseForward(false); // Sử dụng redirect thay vì forward
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        // Log chi tiết lỗi
        String username = request.getParameter("username");
        log.warn("Login failed for user: {}, reason: {}", username, exception.getClass().getSimpleName());

        if (exception instanceof UsernameNotFoundException) {
            log.warn("User not found: {}", username);
        } else if (exception instanceof BadCredentialsException) {
            log.warn("Bad credentials for: {}", username);
        } else if (exception instanceof DisabledException) {
            log.warn("User disabled: {}", username);
        } else {
            log.warn("Authentication error: {}", exception.getMessage());
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}
