package com.v.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.v.dto.CheckoutForm;
import com.v.dto.RevenueDataDto;
import com.v.model.CartItem;
import com.v.model.Order;
import com.v.model.OrderItem;
import com.v.model.OrderStatus;
import com.v.model.PaymentMethod;
import com.v.model.User;
import com.v.repository.BookRepository;
import com.v.repository.OrderRepository;

import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private BookRepository bookRepo;

    /**
     * Tạo đơn hàng mới với customer và status = PENDING.
     */
    public Order createOrder(User customer) {
        Order o = new Order();
        o.setCustomer(customer);
        o.setStatus(OrderStatus.PENDING);
        o.setPaymentMethod(PaymentMethod.COD); // mặc định để tránh null
        o.setTotal(BigDecimal.ZERO);
        return orderRepo.save(o);
    }

    /**
     * Tạo đơn hàng kèm items và thông tin nhận hàng.
     */
    @Transactional
    public Order createOrder(User customer, List<CartItem> items, CheckoutForm form) {
        // KIỂM TRA SỐ LƯỢNG TỒN KHO TRƯỚC KHI TẠO ĐƠN
        for (CartItem ci : items) {
            Integer availableQty = ci.getBook().getQuantity();
            if (availableQty == null || availableQty < ci.getQuantity()) {
                throw new IllegalStateException(
                        String.format("Sản phẩm '%s' không đủ hàng! Còn lại: %d, yêu cầu: %d",
                                ci.getBook().getName(),
                                availableQty != null ? availableQty : 0,
                                ci.getQuantity()));
            }
        }

        Order o = new Order();
        o.setCustomer(customer);
        o.setStatus(OrderStatus.PENDING);

        // Thông tin người nhận
        o.setRecipientName(form.getFullName());
        o.setRecipientEmail(form.getEmail());
        o.setRecipientAddress(form.getAddress());
        o.setRecipientPhone(form.getPhone());

        // === Phương thức thanh toán (mặc định COD nếu form rỗng/không map được) ===
        PaymentMethod pm = PaymentMethod.COD;
        String pmStr = form.getPaymentMethod();
        if (pmStr != null && !pmStr.isBlank()) {
            try {
                pm = PaymentMethod.valueOf(pmStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // giữ COD
            }
        }
        o.setPaymentMethod(pm);

        // 1) Tính total
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setOrder(o);
            oi.setProduct(ci.getBook());
            oi.setQuantity(ci.getQuantity());

            BigDecimal unitPrice = BigDecimal.valueOf(ci.getBook().getPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            oi.setUnitPrice(unitPrice);

            total = total.add(unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity())));
            o.getItems().add(oi);
        }
        o.setTotal(total);

        // 2) TRỪ SỐ LƯỢNG TỒN KHO VÀ SAVE VÀO DATABASE
        for (CartItem ci : items) {
            Integer currentQty = ci.getBook().getQuantity();
            int newQty = currentQty - ci.getQuantity();
            ci.getBook().setQuantity(newQty);
            // PHẢI SAVE VÀO DATABASE
            bookRepo.save(ci.getBook());
        }

        return orderRepo.save(o);
    }

    /** Lấy danh sách đơn theo customer. */
    public List<Order> getByCustomer(User user) {
        return orderRepo.findByCustomer(user);
    }

    /**
     * NEW: Lấy danh sách đơn theo customer, kèm join fetch items + product để hiển
     * thị ảnh ở profile.
     */
    public List<Order> getByCustomerWithItems(User user) {
        return orderRepo.findByCustomerWithItems(user);
    }

    /** Lấy danh sách đơn theo trạng thái. */
    public List<Order> getByStatus(OrderStatus status) {
        return orderRepo.findByStatus(status);
    }

    /** Lấy chi tiết một đơn theo ID (Long). */
    public Order getById(Long orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn " + orderId));
    }

    /** Overload cho ID kiểu Integer. */
    public Order getById(Integer id) {
        return getById(id.longValue());
    }

    /** Cập nhật trạng thái đơn. */
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order o = getById(id);
        o.setStatus(newStatus);

        // Lưu mốc thời gian giao thành công
        if (newStatus == OrderStatus.DELIVERED) {
            o.setDeliveredAt(LocalDateTime.now());
        }
        return orderRepo.save(o);
    }

    /** Xóa đơn hàng theo ID. */
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy đơn để xóa: " + id);
        }
        orderRepo.deleteById(id);
    }

    /** Thống kê tổng doanh thu theo trạng thái. */
    public BigDecimal getTotalRevenue(OrderStatus status) {
        return orderRepo.sumTotalByStatus(status);
    }

    /** Thống kê tổng số lượng sản phẩm đã bán theo trạng thái. */
    public Long getTotalQuantity(OrderStatus status) {
        Long v = orderRepo.sumQuantityByStatus(status);
        return v != null ? v : 0L;
    }

    /** Doanh thu theo ngày. */
    public List<RevenueDataDto> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        // Lấy đơn có trạng thái: CONFIRMED, SHIPPED, DELIVERED (bỏ PENDING, CANCELED)
        List<OrderStatus> statuses = List.of(
                OrderStatus.CONFIRMED,
                OrderStatus.SHIPPED,
                OrderStatus.DELIVERED);
        List<String> names = statuses.stream().map(Enum::name).toList();
        return orderRepo.revenueDailyBetween(start, end, names).stream()
                .map(row -> new RevenueDataDto(row[0].toString(), (BigDecimal) row[1]))
                .collect(Collectors.toList());
    }

    /** Doanh thu theo tháng. */
    public List<RevenueDataDto> getMonthlyRevenue(LocalDate startMonth, LocalDate endMonth) {
        LocalDateTime start = startMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = endMonth.withDayOfMonth(endMonth.lengthOfMonth()).atTime(LocalTime.MAX);

        // Lấy đơn có trạng thái: CONFIRMED, SHIPPED, DELIVERED (bỏ PENDING, CANCELED)
        List<OrderStatus> statuses = List.of(
                OrderStatus.CONFIRMED,
                OrderStatus.SHIPPED,
                OrderStatus.DELIVERED);

        List<String> names = statuses.stream().map(Enum::name).toList();

        // Lấy doanh thu từng ngày rồi gộp theo tháng
        Map<String, BigDecimal> monthMap = new LinkedHashMap<>();
        orderRepo.revenueDailyBetween(start, end, names).forEach(row -> {
            String dateStr = row[0].toString(); // "2025-11-01"
            String[] parts = dateStr.split("-");
            String ym = parts[0] + "-" + parts[1]; // "2025-11"
            BigDecimal sum = (BigDecimal) row[1];
            monthMap.merge(ym, sum, BigDecimal::add);
        });

        return monthMap.entrySet().stream()
                .map(e -> new RevenueDataDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // ================== HỦY ĐƠN HÀNG ==================

    /**
     * Khách tự hủy đơn của chính mình (chỉ khi PENDING - chưa được admin xác nhận).
     * 
     * @param orderId id đơn
     * @param actor   user đang đăng nhập
     * @param reason  lý do (tùy chọn)
     */
    @Transactional
    public Order cancelOrder(Long orderId, User actor, String reason) {
        Order o = getById(orderId);

        // Chỉ chủ sở hữu mới được hủy
        if (o.getCustomer() == null || actor == null || !o.getCustomer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Bạn không có quyền hủy đơn này.");
        }

        // Đã hủy rồi -> idempotent
        if (o.getStatus() == OrderStatus.CANCELED) {
            return o;
        }

        // Chỉ cho hủy khi còn trạng thái cho phép
        if (!o.isCancelable()) {
            throw new IllegalStateException("Đơn đã chuyển sang trạng thái không thể hủy.");
        }

        o.setStatus(OrderStatus.CANCELED);
        o.setCanceledAt(LocalDateTime.now());
        o.setCancelReason((reason == null || reason.isBlank()) ? "Khách yêu cầu hủy" : reason.trim());
        o.setCanceledBy("CUSTOMER");

        // HOÀN KHO: Cộng lại số lượng đã trừ khi đặt hàng
        for (OrderItem item : o.getItems()) {
            Integer currentQty = item.getProduct().getQuantity();
            int restoredQty = currentQty + item.getQuantity();
            item.getProduct().setQuantity(restoredQty);
            bookRepo.save(item.getProduct());
        }

        // TODO: Tạo yêu cầu refund nếu PaymentMethod != COD
        return orderRepo.save(o);
    }

    /**
     * Admin hủy đơn (chỉ khi PENDING - chưa xác nhận).
     * Sau khi admin đã xác nhận (CONFIRMED trở đi), không cho phép hủy để đảm bảo
     * tính nhất quán.
     */
    @Transactional
    public Order cancelOrderByAdmin(Long orderId, String reason) {
        Order o = getById(orderId);

        if (o.getStatus() == OrderStatus.CANCELED) {
            return o;
        }
        if (!o.isCancelable()) {
            throw new IllegalStateException("Đơn đã chuyển sang trạng thái không thể hủy.");
        }

        o.setStatus(OrderStatus.CANCELED);
        o.setCanceledAt(LocalDateTime.now());
        o.setCancelReason((reason == null || reason.isBlank()) ? "Admin hủy đơn" : reason.trim());
        o.setCanceledBy("ADMIN");

        // HOÀN KHO: Cộng lại số lượng đã trừ khi đặt hàng
        for (OrderItem item : o.getItems()) {
            Integer currentQty = item.getProduct().getQuantity();
            int restoredQty = currentQty + item.getQuantity();
            item.getProduct().setQuantity(restoredQty);
            bookRepo.save(item.getProduct());
        }

        // TODO: Đánh dấu refund nếu cần
        return orderRepo.save(o);
    }

    public long countByStatus(OrderStatus pending) {
        // TODO Auto-generated method stub
        return 0;
    }
}
