package com.v.config;

import com.v.model.User;
import com.v.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserService userService;

    @Autowired
    public AdminInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            String adminUsername = "admin";
            String adminPassword = "123";
            
            User existingAdmin = userService.findByUsername(adminUsername);
            
            if (existingAdmin == null) {
                // Tạo admin mới
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail("admin@example.com");
                admin.setPhone("0123456789");
                admin.setFullName("Administrator");
                admin.setRole("ROLE_ADMIN");
                admin.setPassword(adminPassword);
                admin.setEnabled(true);

                userService.save(admin);
                log.info("✅ Tài khoản admin đã được tạo: username='{}', password='{}'", adminUsername, adminPassword);
            } else {
                // Admin đã tồn tại: cập nhật mật khẩu lại (force reset mỗi lần startup)
                try {
                    userService.updatePassword(adminUsername, adminPassword);
                    log.info("✅ Tài khoản admin đã tồn tại — mật khẩu được đặt lại thành '{}' (mỗi lần startup)", adminPassword);
                } catch (Exception e) {
                    log.error("❌ Lỗi khi cập nhật mật khẩu admin: {}", e.getMessage(), e);
                }
            }
        } catch (Exception ex) {
            log.error("❌ Lỗi tạo/cập nhật tài khoản admin: {}", ex.getMessage(), ex);
        }
    }
}
