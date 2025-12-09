package com.v.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.v.model.PaymentMethod;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // Phương thức thanh toán
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    private String recipientName;
    private String recipientEmail;
    private String recipientAddress;
    private String recipientPhone;

    private LocalDateTime createdAt;

    // Thời điểm giao thành công
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** Tổng tiền thanh toán */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // ========== HỖ TRỢ HỦY ĐƠN ==========
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    /** "CUSTOMER" hoặc "ADMIN" */
    @Column(name = "canceled_by", length = 32)
    private String canceledBy;

    /** Có thể hủy chỉ khi còn PENDING (chưa xác nhận) */
    @Transient
    public boolean isCancelable() {
        return this.status == OrderStatus.PENDING;
    }
    // =====================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (total == null)
            total = BigDecimal.ZERO;
        if (status == null)
            status = OrderStatus.PENDING; // mặc định đơn mới
    }

    /**
     * Tính tổng tạm (chưa trừ giảm giá)
     */
    @Transient
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== Getters & Setters ===========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCanceledBy() {
        return canceledBy;
    }

    public void setCanceledBy(String canceledBy) {
        this.canceledBy = canceledBy;
    }

    public Object getUser() {
        // TODO Auto-generated method stub
        return null;
    }
}
