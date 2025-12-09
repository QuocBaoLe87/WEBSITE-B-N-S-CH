package com.v.service;

import java.util.List;

import com.v.model.User;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User findByUsername(String username);
    User findByEmail(String email);        // <— thêm để phục vụ quên mật khẩu (nhập email)
    User save(User u);
    void deleteById(Long id);
    void updatePassword(String username, String newPassword);
}
