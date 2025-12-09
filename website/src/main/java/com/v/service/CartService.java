package com.v.service;

import org.springframework.security.core.Authentication;

import com.v.model.CartItem;

import java.util.List;

public interface CartService {

    void add(Long bookId);

    void decrement(Long id);

    void remove(Long id);

    List<CartItem> getItems();

    int getItemCount();

    double getTotalPrice();

    void clear();

    // Đồng bộ với CartServiceImpl anh gửi
    void addToCart(Long id, int quantity, Authentication auth);
}
