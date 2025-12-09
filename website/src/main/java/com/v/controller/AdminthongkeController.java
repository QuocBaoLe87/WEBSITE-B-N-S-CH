package com.v.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v.dto.report.DayDetailResponse;
import com.v.dto.report.ItemDetailDTO;
import com.v.dto.report.OrderDetailDTO;
import com.v.model.Order;
import com.v.model.OrderStatus;
import com.v.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/api/reports")
public class AdminthongkeController {

    private final OrderRepository orderRepo;

    public AdminthongkeController(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    /** Trạng thái tính vào doanh thu (điều chỉnh theo enum của anh). */
    private static final Set<OrderStatus> REVENUE_STATUSES = EnumSet.of(
            OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.COMPLETED);

    // ---------------------- Helpers dùng phản chiếu an toàn ----------------------

    private static String tryGetString(Object obj, String... getters) {
        if (obj == null)
            return null;
        for (String g : getters) {
            try {
                var m = obj.getClass().getMethod(g);
                Object v = m.invoke(obj);
                return (v == null) ? null : v.toString();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String joinParts(String... parts) {
        return java.util.Arrays.stream(parts)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    private static String buildAddressFromParts(Object obj) {
        if (obj == null)
            return null;
        String street = tryGetString(obj, "getStreet", "getAddressLine", "getAddressLine1", "getLine1");
        String ward = tryGetString(obj, "getWard", "getCommune");
        String district = tryGetString(obj, "getDistrict");
        String city = tryGetString(obj, "getCity", "getTown");
        String province = tryGetString(obj, "getProvince", "getState");
        String zip = tryGetString(obj, "getZip", "getPostcode", "getPostalCode");
        return joinParts(street, ward, district, city, province, zip);
    }

    /**
     * ƯU TIÊN tên ở ĐƠN (receiver/shipping/customer name) → rồi mới tới User → cuối
     * cùng rớt về "Khách/User#ID".
     */
    private static String customerName(Order o) {
        // 1) Tên lưu trong Order
        String name = tryGetString(o,
                "getReceiverName", "getRecipientName", "getShippingName",
                "getCustomerName", "getFullName", "getName");

        // 2) Nếu vẫn trống -> lấy từ User
        if ((name == null || name.isBlank()) && o.getCustomer() != null) {
            var u = o.getCustomer();
            name = tryGetString(u, "getFullName", "getName", "getUsername", "getEmail");
            if (name == null || name.isBlank()) {
                try {
                    name = "User#" + u.getId();
                } catch (Exception ignored) {
                    name = "Khách";
                }
            }
        }

        // 3) Fallback nữa nếu hệ thống lưu String customer trong Order
        if (name == null || name.isBlank()) {
            name = tryGetString(o, "getCustomer");
            if (name == null || name.isBlank())
                name = "Khách";
        }
        return name;
    }

    /** SĐT: ưu tiên ở User; nếu trống thì lấy phone lưu trong Order. */
    private static String customerPhone(Order o) {
        String phone = null;
        if (o.getCustomer() != null) {
            var u = o.getCustomer();
            phone = tryGetString(u, "getPhone", "getPhoneNumber", "getMobile", "getTel", "getTelephone");
        }
        if (phone == null || phone.isBlank()) {
            phone = tryGetString(o, "getPhone", "getShippingPhone", "getReceiverPhone", "getContactPhone");
        }
        return (phone != null && !phone.isBlank()) ? phone : null;
    }

    /**
     * Địa chỉ giao hàng: ưu tiên field trên Order; nếu trống thì ghép từ các phần
     * hoặc lấy từ User.
     */
    private static String shippingAddress(Order o) {
        // 1) Một field nguyên vẹn trên Order
        String addr = tryGetString(o, "getShippingAddress", "getAddress", "getReceiverAddress", "getDeliveryAddress",
                "getShipAddress");
        if (addr == null || addr.isBlank())
            addr = buildAddressFromParts(o);

        // 2) Fallback: lấy từ User
        if (addr == null || addr.isBlank()) {
            var u = o.getCustomer();
            if (u != null) {
                addr = tryGetString(u, "getAddress", "getFullAddress", "getLocation");
                if (addr == null || addr.isBlank())
                    addr = buildAddressFromParts(u);
            }
        }
        return (addr != null && !addr.isBlank()) ? addr : null;
    }

    // ---------------------------------------------------------------------------

    @GetMapping("/day")
    public DayDetailResponse dayDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        // Nên để findWithItemsInDay FETCH JOIN customer + items + product
        List<Order> orders = orderRepo.findWithItemsInDay(start, end, List.copyOf(REVENUE_STATUSES));

        BigDecimal revenue = orders.stream()
                .map(Order::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderDetailDTO> orderDtos = orders.stream().map(o -> {
            String customer = customerName(o);
            String phone = customerPhone(o);
            String address = shippingAddress(o);
            String code = "DH-" + o.getId();

            var items = o.getItems().stream().map(oi -> {
                BigDecimal unit = Optional.ofNullable(oi.getUnitPrice()).orElse(BigDecimal.ZERO);
                int qty = Optional.ofNullable(oi.getQuantity()).orElse(0);
                BigDecimal line = unit.multiply(BigDecimal.valueOf(qty));

                var p = oi.getProduct(); // Laptop/Product
                Long pid = (p != null) ? p.getId() : null;
                String pname = (p != null) ? p.getName() : "Sản phẩm";

                return new ItemDetailDTO(pid, pname, qty, unit, line);
            }).collect(Collectors.toList());

            return new OrderDetailDTO(
                    o.getId(),
                    code,
                    customer,
                    phone,
                    address,
                    Optional.ofNullable(o.getTotal()).orElse(BigDecimal.ZERO),
                    items);
        }).collect(Collectors.toList());

        return new DayDetailResponse(date, revenue, orderDtos);
    }
}
