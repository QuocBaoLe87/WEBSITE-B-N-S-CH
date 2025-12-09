# Chi tiết dự án Website bán laptop

Tài liệu này giải thích toàn bộ tính năng của hệ thống một cách dễ hiểu, phù hợp với người không có nền tảng kỹ thuật.

## 1. Dự án này làm gì?

- Là một website bán laptop trực tuyến cho khách hàng cá nhân.
- Người dùng có thể xem thông tin sản phẩm, thêm vào giỏ, thanh toán online (VNPay) hoặc COD, theo dõi đơn hàng và tương tác với nhân viên.
- Ban quản trị có khu vực riêng để theo dõi doanh thu, quản lý đơn hàng, sản phẩm, chương trình giảm giá và chăm sóc khách hàng.

## 2. Trải nghiệm của khách hàng

### 2.1. Lướt và tìm sản phẩm (`home.html`, `product.html`)

- Trang chủ hiển thị danh sách laptop theo danh mục (văn phòng, gaming, học tập), hãng và các bộ lọc giá, RAM, CPU.
- Có thể sắp xếp theo giá, độ nổi bật; hệ thống tự tính điểm đánh giá trung bình và hiển thị số sao.
- Mỗi trang sản phẩm cung cấp mô tả chi tiết, tối đa 5 ảnh dạng carousel, thông số kỹ thuật và đánh giá thực tế.

### 2.2. Đánh giá sản phẩm (`ProductController`, `fragments/review-item.html`)

- Người mua sau khi đăng nhập có thể chấm điểm 1–5 sao và để lại bình luận.
- Đánh giá mới lập tức xuất hiện trên trang sản phẩm; dữ liệu này cũng dùng để tính điểm trung bình.

### 2.3. Giỏ hàng & mã giảm giá (`cart.html`)

- Thêm/bớt số lượng từng sản phẩm, xóa mục hoặc làm trống toàn bộ giỏ.
- Nhập mã khuyến mãi để được giảm theo phần trăm, hệ thống tự tính lại tổng tiền sau giảm và lưu vào phiên làm việc.

### 2.4. Thanh toán & xác nhận (`checkout.html`, `confirmation.html`)

- Điền thông tin giao hàng, chọn phương thức thanh toán COD hoặc VNPay.
- Khi thanh toán VNPay thành công, trạng thái đơn chuyển sang “Đã xác nhận” và email xác nhận được gửi tự động.
- Với thanh toán COD, khách vẫn nhận được trang xác nhận và email thông báo.

### 2.5. Tài khoản người dùng (`login.html`, `register.html`, `profile.html`)

- Đăng ký bằng email/số điện thoại hoặc đăng nhập qua Google OAuth.
- Có trang quên mật khẩu, đặt lại mật khẩu, đổi mật khẩu (trừ tài khoản Google).
- Trang hồ sơ cho phép cập nhật thông tin cá nhân, đổi ảnh đại diện, xem toàn bộ đơn hàng kèm trạng thái theo thời gian thực.
- Người dùng có thể xem chi tiết từng đơn, tải lại link thanh toán VNPay nếu đơn còn chờ xử lý.

### 2.6. Theo dõi đơn hàng & trả hàng (`profile.html`, `return-detail.html`)

- Danh sách đơn phân loại theo trạng thái: chờ duyệt, đã xác nhận, đang giao, đã giao, bị hủy, trả hàng.
- Người dùng có thể gửi yêu cầu trả hàng, kèm lý do, danh sách sản phẩm muốn trả và ảnh minh chứng; hệ thống lưu lại để admin duyệt.

### 2.7. Hỗ trợ trực tuyến và liên hệ (`chat.html`, `contact.html`)

- Tích hợp chat realtime giữa khách và admin. Tin nhắn được lưu lại để tra cứu, admin có thể xóa cuộc trò chuyện khi cần.
- Trang liên hệ cung cấp biểu mẫu gửi phản hồi, thông tin hotline, email.

### 2.8. Thông báo & banner (`AnnouncementService`, `templates/admin/ann_*`)

- Khối thông báo trên trang chủ giúp đẩy khuyến mãi, sự kiện; admin thiết lập thời gian hiệu lực và nội dung.
- Banner trang chủ được quản lý từ khu vực admin và lưu trữ ở `uploads/ann`.

## 3. Hậu mãi & dịch vụ đi kèm

- Hệ thống email (Gmail API) gửi các loại thư: xác nhận đơn, cập nhật trạng thái, đặt lại mật khẩu.
- Tích hợp VNPay để thanh toán online, có cơ chế quay lại trang web và cập nhật trạng thái đơn theo kết quả từ cổng thanh toán.
- Bộ lọc CSRF, đăng nhập bảo vệ bằng Spring Security, đảm bảo chỉ người có quyền mới thao tác được dữ liệu nhạy cảm.

## 4. Khu vực quản trị (`templates/admin/*.html`)

### 4.1. Bảng điều khiển (Dashboard)

- Thống kê doanh thu theo ngày/tháng, lượng đơn, số sản phẩm bán ra, tỉ lệ hủy, khách hàng mới.
- Đồ thị 7 ngày gần nhất giúp theo dõi xu hướng; danh sách đơn chờ duyệt nằm ngay trên dashboard để xử lý nhanh.

### 4.2. Quản lý sản phẩm và thương hiệu

- Thêm/sửa/xóa sản phẩm với tối đa 5 ảnh, chọn danh mục, hãng, cấu hình kỹ thuật.
- Quản lý brand riêng để dùng lại khi tạo sản phẩm; hỗ trợ tải ảnh trực tiếp lên máy chủ.

### 4.3. Đơn hàng & giao vận

- Lọc đơn theo trạng thái, xem chi tiết, đổi trạng thái (trừ “Hoàn tất”), hủy đơn và xóa đơn khi cần.
- Có API nội bộ để cập nhật trạng thái từ giao diện mà không cần tải lại trang.

### 4.4. Khách hàng & phân quyền

- Danh sách người dùng, tạo mới tài khoản admin hoặc người dùng thường, kích hoạt/vô hiệu hóa, đặt lại thông tin.
- Kiểm tra trùng lặp username/email ngay trên form.

### 4.5. Giảm giá, banner, thông báo & chat

- Tạo mã giảm giá với phần trăm ưu đãi, thời gian hiệu lực; hệ thống đếm số mã đang hoạt động/đã hết hạn.
- Quản lý banner, thông báo hiển thị trên trang chủ; xem lịch sử và thiết lập thời gian hiển thị.
- Quản trị viên có giao diện chat riêng để trả lời khách, xem ai đang trực tuyến và gỡ các cuộc hội thoại cũ.

### 4.6. Trả hàng & đánh giá

- Xem danh sách yêu cầu trả hàng, trạng thái xử lý, ảnh minh chứng và lý do.
- Theo dõi đánh giá của khách, kiểm soát bình luận tiêu cực nếu cần.

## 5. Thành phần kỹ thuật (chỉ để tham khảo)

- Backend sử dụng Spring Boot 3, Java 21, kết nối MySQL qua JPA/Hibernate.
- Giao diện render bằng Thymeleaf; static assets đặt tại `src/main/resources/static`.
- WebSocket dùng cho chat realtime; VNPay và Gmail được đóng gói sẵn trong `service/impl`.
- Cấu hình bảo mật, upload file, CSRF, WebSocket nằm trong thư mục `config`.

## 6. Đọc thêm nếu cần

- `src/main/java/com/v/controller/` chứa toàn bộ luồng nghiệp vụ chính (nhìn tên file sẽ biết trang hoặc chức năng thao tác).
- `src/main/resources/templates/` chứa giao diện tương ứng; mỗi file HTML là một màn hình cụ thể.
- `src/main/resources/db/migration/` liệt kê dữ liệu mẫu (danh mục, thương hiệu) khi khởi chạy dự án lần đầu.

> **Gợi ý cho người mới:** Hãy bắt đầu từ trang `home.html` và `ProductController` để hiểu luồng mua hàng, sau đó xem `AdminController` để hình dung phần quản trị. Nếu cần sửa câu chữ hoặc thêm hình ảnh, chỉ cần chỉnh các file trong `templates`.
