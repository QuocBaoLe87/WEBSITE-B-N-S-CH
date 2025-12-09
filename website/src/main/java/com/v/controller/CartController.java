package com.v.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.v.model.CartItem;
import com.v.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Xem giỏ
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> items = cartService.getItems();
        model.addAttribute("cartItems", items);
        model.addAttribute("totalCount", cartService.getItemCount());

        long totalPrice = items.stream()
                .mapToLong(i -> (long) (i.getBook().getPrice() * i.getQuantity()))
                .sum();
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    // Thêm 1 sản phẩm
    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, RedirectAttributes ra) {
        try {
            cartService.add(id);
            ra.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // Giảm 1
    @PostMapping("/decrement/{id}")
    public String decrement(@PathVariable Long id, RedirectAttributes ra) {
        try {
            cartService.decrement(id);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // Xóa 1 mục
    @PostMapping("/remove/{id}")
    public String removeItem(@PathVariable Long id, RedirectAttributes ra) {
        try {
            cartService.remove(id);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // Xóa hết
    @PostMapping("/clear")
    public String clearCart() {
        cartService.clear();
        return "redirect:/cart";
    }
}
