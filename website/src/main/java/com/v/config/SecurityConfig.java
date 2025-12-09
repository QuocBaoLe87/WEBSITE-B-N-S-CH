package com.v.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.v.model.User;
import com.v.repository.UserRepository;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableMethodSecurity // cho phép @PreAuthorize ở controller
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(UserRepository userRepository) {
                return username -> {
                        User u = userRepository.findByUsername(username);
                        if (u == null) {
                                throw new UsernameNotFoundException("Không tìm thấy user: " + username);
                        }

                        String userRole = u.getRole();
                        if (userRole == null || userRole.trim().isEmpty()) {
                                userRole = "ROLE_USER";
                        }

                        // Role đã được normalize thành "ROLE_XXX" format ở User model
                        // .roles() sẽ tự thêm "ROLE_", nên phải bỏ prefix trước
                        String roleWithoutPrefix = userRole.replaceAll("^ROLE_", "");

                        return org.springframework.security.core.userdetails.User
                                        .withUsername(u.getUsername())
                                        .password(u.getPassword())
                                        .roles(roleWithoutPrefix)
                                        .build();
                };
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        LogoutSuccessHandler logoutSuccessHandler,
                        AuthenticationFailureHandler failureHandler,
                        AuthenticationSuccessHandler successHandler)
                        throws Exception {
                // Cho phép Spring 6 đọc CSRF token từ tham số _csrf (hidden field) hoặc header
                CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                requestHandler.setCsrfRequestAttributeName("_csrf");

                http
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(requestHandler)
                                                // BỎ CSRF cho WebSocket handshake + REST chat (nếu cần)
                                                .ignoringRequestMatchers(
                                                                "/ws-chat/**",
                                                                "/api/chat/**"))

                                // Thêm filter để force tạo CSRF cookie ngay lập tức
                                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                                                "/", "/books/**",
                                                                "/product/**",
                                                                "/login", "/register",
                                                                "/auth/**",
                                                                "/forgot-password", "/reset-password",
                                                                "/oauth2/**",
                                                                "/ws-chat/**",
                                                                "/contact")
                                                .permitAll()

                                                // Trang chat (view) yêu cầu đăng nhập
                                                .requestMatchers("/chat").hasAnyRole("USER", "CUSTOMER", "ADMIN")

                                                // API chat phải đăng nhập
                                                .requestMatchers("/api/chat/partners").hasRole("ADMIN")
                                                .requestMatchers("/api/chat/**").authenticated()

                                                .requestMatchers("/my-orders/**")
                                                .hasAnyRole("USER", "CUSTOMER", "ADMIN")
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .permitAll()
                                                .successHandler(successHandler)
                                                .failureHandler(failureHandler))

                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/books", true))

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessHandler(logoutSuccessHandler)
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                                                .permitAll())

                                .sessionManagement(session -> session
                                                .sessionConcurrency(concurrency -> concurrency
                                                                .maximumSessions(1)
                                                                .expiredUrl("/login?expired")))

                                .exceptionHandling(ex -> ex
                                                .accessDeniedPage("/error/403"));
                return http.build();
        }
}
