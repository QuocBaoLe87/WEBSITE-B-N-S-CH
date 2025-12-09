package com.v.controller;

import jakarta.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.v.dto.CheckoutForm;
import com.v.model.CartItem;
import com.v.model.Order;
import com.v.model.OrderStatus;
import com.v.model.User;
import com.v.service.CartService;
import com.v.service.OrderService;
import com.v.service.UserService;
import com.v.service.impl.GmailService;
import com.v.service.impl.VNPayService;

@Controller
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private VNPayService vnpService;
    @Autowired
    private GmailService gmailService;

    // ===== Helpers cho OAuth2/UserDetails =====
    private String extractUsernameOrEmail(Authentication auth) {
        if (auth == null)
            return null;
        Object p = auth.getPrincipal();
        if (p instanceof OidcUser oidc) {
            if (oidc.getEmail() != null && !oidc.getEmail().isBlank())
                return oidc.getEmail();
            return auth.getName();
        }
        if (p instanceof OAuth2User ou) {
            Object email = ou.getAttribute("email");
            return email != null ? email.toString() : auth.getName();
        }
        if (p instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return auth.getName();
    }

    /** Tải User từ DB theo username trước, nếu không có thì thử theo email. */
    private User loadUserFromAuth(Authentication authentication) {
        String key = extractUsernameOrEmail(authentication);
        if (key == null)
            return null;

        User u = userService.findByUsername(key);
        if (u != null)
            return u;

        try {
            return userService.findByEmail(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    @GetMapping("/checkout")
    public String showCheckoutForm(Model model, HttpSession session) {
        // 1) Lấy giỏ & tổng
        List<CartItem> items = cartService.getItems();
        model.addAttribute("cartItems", items);

        BigDecimal totalPrice = items.stream()
                .map(i -> BigDecimal.valueOf(i.getBook().getPrice())
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        model.addAttribute("totalPrice", totalPrice);

        // 2) Pre-populate form
        CheckoutForm form = new CheckoutForm();
        model.addAttribute("checkoutForm", form);

        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(
            @ModelAttribute("checkoutForm") CheckoutForm form,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes ra) throws UnsupportedEncodingException {

        // 1) Lấy user an toàn cho cả local & Google
        User u = loadUserFromAuth(authentication);
        if (u == null) {
            ra.addFlashAttribute("message", "Vui lòng đăng nhập lại trước khi thanh toán.");
            return "redirect:/login";
        }

        // 2) Lấy giỏ hàng
        List<CartItem> items = cartService.getItems();

        // 3) Tạo order - KIỂM TRA TỒN KHO
        Order order;
        try {
            order = orderService.createOrder(u, items, form);
        } catch (IllegalStateException e) {
            // Hết hàng hoặc không đủ số lượng
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }

        // 3.5) Gửi mail sau khi tạo đơn
        log.info("[Checkout] Send initial confirmation email for order #{}", order.getId());
        gmailService.sendOrderConfirmationEmail(order);

        // 4) Thanh toán
        if ("VNPAY".equalsIgnoreCase(form.getPaymentMethod())) {
            String payUrl = vnpService.createPayment(order.getTotal(), form.getFullName(), order.getId());
            log.info("[Checkout] Created order #{} (VNPAY) -> redirect to VNPay", order.getId());

            // clear cart
            cartService.clear();
            return "redirect:" + payUrl;
        } else {
            // COD
            ra.addFlashAttribute("message", "Đặt hàng thành công!");
            cartService.clear();
            return "redirect:/confirmation/" + order.getId();
        }
    }

    @GetMapping("/payment")
    public String handleVNPayReturn(
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        // VNPay may send the user back without the expected params when they click
        // "Back" or cancel.
        // Defensively validate required parameters to avoid NPE /
        // NumberFormatException.
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");

        if (txnRef == null || txnRef.isBlank()) {
            log.warn("[VNPay] Missing vnp_TxnRef in return params: {}", params);
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("error",
                    "Không có thông tin đơn hàng trả về từ VNPay. Vui lòng kiểm tra lại lịch sử đơn hàng.");
            return "redirect:/profile"; // fallback to profile/orders page
        }

        Long orderId;
        try {
            orderId = Long.valueOf(txnRef);
        } catch (NumberFormatException ex) {
            log.error("[VNPay] Invalid vnp_TxnRef='{}'", txnRef, ex);
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("error",
                    "Mã đơn hàng trả về từ VNPay không hợp lệ.");
            return "redirect:/orders";
        }

        // If responseCode is null, treat as canceled/unknown and keep order in PENDING
        if ("00".equals(responseCode)) {
            orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
            log.info("[VNPay] Thanh toán thành công cho đơn #{} -> gửi mail xác nhận (CONFIRMED)", orderId);

            gmailService.sendOrderConfirmationEmail(orderService.getById(orderId));

            redirectAttributes.addFlashAttribute("paymentSuccess", true);
            redirectAttributes.addFlashAttribute("message", "Thanh toán VNPay thành công cho đơn #" + orderId);
            return "redirect:/confirmation/" + orderId;
        } else {
            // Thanh toán thất bại hoặc bị hủy -> giữ trạng thái PENDING
            try {
                orderService.updateStatus(orderId, OrderStatus.PENDING);
            } catch (Exception e) {
                log.warn("[VNPay] Failed to set order {} to PENDING: {}", orderId, e.getMessage());
            }
            log.warn("[VNPay] Thanh toán thất bại/hủy cho đơn #{}, responseCode: {}", orderId, responseCode);

            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("error",
                    "Thanh toán VNPay thất bại hoặc đã bị hủy. Đơn hàng #" + orderId
                            + " vẫn được lưu với trạng thái chờ xử lý.");
            // Redirect về trang chi tiết đơn hàng thay vì confirmation
            return "redirect:/profile/order/" + orderId;
        }
    }

    @GetMapping("/confirmation/{orderId}")
    public String showConfirmation(@PathVariable Long orderId, Model model) {
        model.addAttribute("order", orderService.getById(orderId));
        return "confirmation";
    }
}
