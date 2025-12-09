# Bản đồ chức năng & mã nguồn

Tài liệu này dành cho người cần hiểu rõ “chức năng nào nằm ở đâu trong code” cùng mô tả từng hàm chính. Liệt kê dưới đây bám theo kiến trúc Spring Boot của dự án.

> Cách đọc:  
> • **Chức năng** → mô tả nghiệp vụ.  
> • **File** → lớp/đường dẫn triển khai.  
> • **Hàm chính** → chữ in nghiêng, kèm giải thích nhiệm vụ.

## 1. Xác thực & bảo mật

**File `config/SecurityConfig.java`**

- _passwordEncoder()_: cấu hình BCrypt dùng chung để mã hóa mật khẩu.
- _userDetailsService(UserService)_: tải `User` từ DB và build `UserDetails` với role tương ứng.
- _filterChain(HttpSecurity,…)_: định nghĩa toàn bộ luật bảo mật (CSRF cookie, URL được phép, login form, OAuth2, logout, giới hạn session, trang 403).

**File `controller/AuthController.java`**

- _loginPage()_: render trang đăng nhập và ép Spring tạo CSRF token vào model.
- _registerPage()_: trả view đăng ký.
- _doRegister()_: nhận AJAX đăng ký, validate password, tạo `User`, bắt lỗi trùng username/email.
- _checkUniquePublic()_: API kiểm tra nhanh username/email đã tồn tại chưa (dùng ở form register).

## 2. Danh mục & tìm kiếm sản phẩm

**File `controller/HomeController.java`**

- _list(...)_: endpoint “/” và “/laptops”.
  1. Gọi `BookService.search` lấy toàn bộ laptop theo brand/sort.
  2. Tự lọc theo category, price range, RAM, CPU, hãng.
  3. Tự phân trang bằng `PageImpl`.
  4. Tính rating từng sản phẩm qua `ReviewService.ratingAgg`.
  5. Nạp dữ liệu hỗ trợ view (brands, badge giỏ, thông báo).

## 3. Trang chi tiết sản phẩm & đánh giá

**File `controller/ProductController.java`**

- _detail(Long id)_: tải `Book`, dựng carousel 5 ảnh `/product/{id}/image/{slot}`, gán reviews + rating trung bình lên model.
- _addToCart(Long id, int quantity, Authentication)_: dùng `CartService.addToCart`, kiểm tra số lượng hợp lệ, redirect giỏ hàng.
- _addReview(...)_: xử lý form/ AJAX gửi đánh giá. Kiểm tra đăng nhập, load `Book`/`User`, gọi `ReviewService.addReview`, trả fragment HTML nếu là AJAX.
- _avgRating(Long id)_: API text/plain trả điểm trung bình để JS cập nhật sao.

## 4. Giỏ hàng & mã giảm giá

**File `service/impl/CartServiceImpl.java`**

- _init()_: khởi tạo list `CartItem` cho mỗi session.
- _add(Long bookId)_: thêm 1 sản phẩm, kiểm tra tồn kho.
- _decrement(Long id)_: giảm số lượng, xóa khỏi giỏ nếu <=0.
- _remove(Long id)_: bỏ hẳn sản phẩm.
- _getItems() / getItemCount() / getTotalPrice()_: trả dữ liệu hiện tại.
- _clear()_: xóa giỏ.
- _addToCart(Long id, int quantity, Authentication)_: phục vụ nút “thêm X chiếc từ trang chi tiết”, có kiểm tra tồn kho và cộng dồn số lượng hiện có.

**File `controller/CartController.java`**

- _viewCart()_: render `cart.html`, tính tổng tiền và đọc thông tin giảm giá từ session.
- _addToCart(Long id)_: gọi `CartService.add`, đặt flash message.
- _decrement(Long id)_ / _remove(Long id)_ / _clearCart()_: thao tác tương ứng trên dịch vụ.
- _applyDiscount(...)_: nhận mã giảm giá, gọi `DiscountService.getDiscountPercent`, tính giảm trừ, lưu các biến (`discountCode`, `discountPercent`, `discountAmount`, `totalAfterDiscount`) vào session để bước checkout đọc lại.

## 5. Thanh toán & VNPay

**File `controller/CheckoutController.java`**

- _showCheckoutForm(...)_:
  - Lấy giỏ hàng, tính `totalPrice`.
  - Đọc mã giảm giá/percent từ session, tính lại `discountAmount` & `totalAfterDiscount`.
  - Khởi tạo `CheckoutForm` và trả view `checkout.html`.
- _processCheckout(CheckoutForm, Authentication,...)_:
  1. Lấy `User` đăng nhập (hỗ trợ cả local & Google).
  2. Rà lại discount trong session để chống sửa hidden field.
  3. Tính lại subtotal, discount, total.
  4. Gọi `OrderService.createOrder` -> trừ tồn kho.
  5. Gửi email xác nhận bằng `GmailService`.
  6. Nếu chọn VNPay: tạo URL qua `VNPayService.createPayment`, xóa giỏ + session discount, redirect sang cổng thanh toán.  
     Nếu COD: xóa giỏ + session discount, redirect `/confirmation/{orderId}`.
- _handleVNPayReturn(Map params,...)_: xử lý callback từ VNPay, cập nhật trạng thái đơn (`CONFIRMED` nếu mã “00”, ngược lại đưa về `PENDING`), gửi email và chuyển hướng phù hợp.
- _showConfirmation(Long orderId)_: tải đơn và render `confirmation.html`.

**File `service/OrderService.java`**

- _createOrder(User customer, List<CartItem> items, CheckoutForm form)_: lõi tạo đơn (validate tồn kho, tính subtotal, discount, total, tạo `OrderItem`, trừ tồn kho thực tế, lưu order).
- _updateStatus(Long id, OrderStatus newStatus)_: đổi trạng thái, ghi nhận `deliveredAt`.
- _cancelOrder(...) / cancelOrderByAdmin(...)_: hủy đơn, ghi lý do, hoàn kho.
- _getDailyRevenue(...), getMonthlyRevenue(...)_: gom dữ liệu phục vụ dashboard chart.
- _getByCustomerWithItems(...), getByStatus(...), getById(...), deleteOrder(...), countByStatus(...)_: tiện ích phục vụ profile/admin.

## 6. Hồ sơ khách hàng & đơn mua

**File `controller/ProfileController.java`**

- Helpers _extractUsernameOrEmail()_ và _loadUserFromAuth()_: chuẩn hóa key tìm `User` cho cả local/OAuth2.
- _showProfile()_: load `User`, danh sách `Order` (kèm item) và cờ `oauth2` để khóa tính năng đổi username/password nếu cần.
- _viewOrderDetail(Long id)_: bảo vệ quyền sở hữu đơn trước khi hiển thị.
- _updateProfile(...)_: cho phép đổi username (user thường), email, phone.
- _uploadAvatar(MultipartFile)_: kiểm tra định dạng/ kích thước, lưu file vào `static/uploads/avatars`, cập nhật `avatarUrl`.
- _showChangePassword()_ / _changePassword(...)_: chặn tài khoản OAuth2, verify mật khẩu cũ, gọi `UserService.updatePassword`.
- _retryPayment(Long orderId)_: cho phép khách tạo lại link VNPay nếu đơn còn `PENDING`/`CANCELED` và phương thức là VNPay.

## 7. Quản lý trả hàng

**File `controller/ReturnRequestController.java`**

- _createReturnRequest(...)_: nhận form (orderId, lý do, JSON danh sách item, ảnh), xác thực người dùng, gọi `ReturnService.createReturnRequest`.
- _getMyReturns()_: trả về toàn bộ yêu cầu trả hàng của khách đang đăng nhập.
- _getReturnRequest(Long id)_: tải chi tiết một yêu cầu, kiểm tra quyền sở hữu.
- _cancelReturn(Long id)_: khách hủy yêu cầu trả hàng, trả về trạng thái mới.

## 8. Chat realtime & hỗ trợ

**File `controller/ChatController.java`**

- _sendMessage(ChatMessage payload, Principal)_: endpoint STOMP `/app/chat.send`. Phân loại người gửi (admin/user), xác định người nhận, lưu `ChatMessageEntity`, đẩy message tới `/queue/messages` cả hai phía.
- _chatPage(...)_: chuẩn bị view `chat.html`, xác định đối tác chat (`with`) tùy vai trò.
- _deleteConversation(...)_: API dành cho admin để xóa toàn bộ tin nhắn với một user và phát sự kiện “CHAT_DELETED”.

## 9. Khu vực quản trị

**File `controller/AdminController.java`** (chỉ liệt kê các nhóm chính)

- _dashboard(...)_: tổng hợp dữ liệu sản phẩm, user, đơn pending, KPI ngày, top sản phẩm, biểu đồ doanh thu (ngày/tháng), thống kê discount.
- _metricsByDate(LocalDate)_: API JSON trả KPI của một ngày cụ thể.
- _viewOrderDetail(Long id)_, _listOrders(...)_, _changeOrderStatus(...)_, _cancelOrderByAdmin(...)_, _deleteOrder(...)_, _apiChangeOrderStatus(...)_, _apiCancelOrder(...)_: bộ chức năng quản lý đơn.
- _createProductForm()_, _editProductForm(Long id)_, _saveProduct(...)_, _deleteProduct(Long id)_ cùng Helpers _hasImageAt_, _firstAvailableImageIndex_: CRUD sản phẩm, xử lý tối đa 5 ảnh và chỉ định ảnh nổi bật.
- _listUsers()_, _createUserForm()_, _saveUser(...)_, _editUserForm(...)_
  , _deleteUser(...)_, _checkUnique(...)_: quản trị người dùng.

## 10. Email, thông báo & tiện ích khác

**File `service/impl/GmailService.java`** (không trích toàn bộ)

- Các hàm như _sendOrderConfirmationEmail(Order)_, _sendPasswordResetMail(...)_ tận dụng JavaMailSender để gửi thông báo.

**File `service/AnnouncementService` & `controller/BannerAdminController`**

- Quản lý thông báo/banner hiển thị trên trang chủ; mỗi hàm tương ứng CRUD thông báo, bật/tắt banner, upload hình (`uploads/ann/...`).

**File `web/AnnouncementOncePerLoginInterceptor.java` & `web/GlobalModelAdvice.java`**

- Đưa thông báo + số lượng giỏ hàng vào mọi view, đảm bảo mỗi lần đăng nhập chỉ hiện popup một lần.

---

## Cách mở rộng / lần theo mã

1. Chọn chức năng ở danh sách trên.
2. Mở file tương ứng trong `src/main/java/com/v/...`.
3. Tìm hàm đã ghi chú. Hầu hết đều có chú thích rõ ràng ngay trong code để dễ bảo trì.

Nếu cần thêm chức năng, hãy đặt controller/service mới cùng module hiện có để tận dụng các tiện ích (CSRF, bảo mật, template) đã cấu hình sẵn.
