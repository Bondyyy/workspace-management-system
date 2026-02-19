# 🏢 Hệ thống Quản lý Không gian Làm việc & Học tập 

## 📖 Giới thiệu 
Dự án này cung cấp giải pháp phần mềm toàn diện cho việc quản lý và vận hành chuỗi không gian làm việc/học tập. Hệ thống giải quyết trọn vẹn bài toán vận hành Online to Offline, từ khâu khách hàng đặt chỗ trực tuyến, check-in tại quầy, quản lý thời gian sử dụng thực tế, gọi món F&B, cho đến thanh toán tổng hợp và dọn dẹp không gian.

---

Hệ thống xoay quanh 4 quy trình vận hành chính, được ánh xạ trực tiếp vào cơ sở dữ liệu:

### 1. Tìm kiếm và Đặt chỗ Trực tuyến 
* **Tìm kiếm:** Khách hàng (hoặc khách vãng lai) truy cập hệ thống, lọc các không gian (`Spaces`) dựa trên chi nhánh (`Branches`), loại bàn (`SpaceTypes`) và trạng thái `AVAILABLE`.
* **Tạo phiếu đặt:** Khi chọn được vị trí, hệ thống tạo một `Bookings` (Phiếu tổng) và các `BookingDetails` (Chi tiết). Giá trị thuê tại thời điểm này được snapshot vào `price_at_booking` để tránh rủi ro thay đổi giá trong tương lai.
* **Giữ chỗ & Đặt cọc:** Khách hàng tiến hành thanh toán cọc. Giao dịch được ghi nhận vào bảng `Payments` (gắn với `booking_id`). Trạng thái không gian chuyển sang `BOOKED`. Hệ thống phát sinh một `qr_code` duy nhất cho phiếu đặt này.

### 2. Check-in và Khởi tạo Phiên làm việc 
* **Đối soát:** Khách đến chi nhánh và xuất trình `qr_code`. Hệ thống giải mã và đối chiếu với bảng `Bookings`.
* **Mở phiên (Session):** Nếu hợp lệ (hoặc với khách vãng lai book trực tiếp tại quầy), hệ thống tạo một `Sessions` mới, ghi nhận `checkin_time` và nhân viên hỗ trợ (`check_in_staff_id`). Giá thuê thực tế được chốt vào `applied_hourly_rate`.
* **Cập nhật trạng thái:** Trạng thái bàn/phòng trong `Spaces` chính thức chuyển sang `OCCUPIED`. Mọi chi phí từ lúc này sẽ được cộng dồn vào Session ID.

### 3. Sử dụng Dịch vụ (F&B) và Gia hạn Giờ 
* **Gọi món (F&B):** Khách quét QR tại bàn hoặc gọi tại quầy. Hệ thống tra cứu thực đơn từ `Products` và `Categories` (chỉ hiển thị món có status `AVAILABLE`). Đơn hàng được ghi nhận vào `SessionOrders` và `SessionOrderDetails`. Trạng thái thanh toán của món ăn tạm để `UNPAID` để thanh toán sau.
* **Gia hạn giờ (Extension):** Nếu khách muốn ngồi lâu hơn dự kiến, nhân viên hoặc khách (qua App) gửi yêu cầu. Thông tin được ghi vào `SessionExtensions` với khoảng thời gian gia hạn, tính toán trước `cost_incurred` (phí phát sinh).

### 4. Check-out và Thanh toán Tổng hợp
* **Đóng phiên (Close Session):** Hệ thống ghi nhận `checkout_time` trong bảng `Sessions`.
* **Tổng hợp hóa đơn:** Hệ thống tự động tính toán tổng chi phí dựa trên: Tiền thuê gốc (`Sessions`) + Phí gia hạn (`SessionExtensions`) + Chi phí F&B (`SessionOrders`).
* **Áp dụng Khuyến mãi:** Nếu khách dùng mã giảm giá, hệ thống kiểm tra điều kiện trong bảng `Vouchers` (còn hạn, đạt giá trị tối thiểu, chưa vượt quá `usage_limit`) và trừ tiền.
* **Thanh toán & Thu hồi không gian:** Khách thanh toán phần còn lại (sau khi trừ `deposit_amount` nếu có cọc). Giao dịch được lưu vào `Payments` (gắn với `session_id`). Bàn làm việc trong bảng `Spaces` lập tức chuyển sang trạng thái `CLEANING` chờ nhân viên dọn dẹp trước khi quay về `AVAILABLE`.

---

## 🗄 Kiến trúc Cơ sở Dữ liệu 

### 1. Phân hệ Danh tính & Phân quyền 
* `Roles`, `Permissions`, `RolePermissions`: Cấu trúc phân quyền Role-Based Access Control tiêu chuẩn.
* `Users`: Thông tin tài khoản trung tâm.
* `Employees`, `Customers`: Mở rộng thông tin dựa trên quan hệ 1-1 với `Users` (Mô hình Table-Per-Type).

### 2. Phân hệ Quản lý Không gian
* `Branches`: Chi nhánh vật lý.
* `SpaceTypes`: Cấu hình danh mục hạng phòng/bàn và đơn giá.
* `Spaces`: Thực thể định danh từng vị trí ngồi, quản lý `current_status` và QR Token check-in.

### 3. Phân hệ Đặt chỗ
* `Bookings`: Quản lý giao dịch đặt trước, tiền cọc, và mã QR tổng.
* `BookingDetails`: Quản lý chi tiết từng vị trí không gian được đặt trong một Booking.

### 4. Phân hệ Phiên làm việc 
* `Sessions`: Theo dõi thời gian thực tế từ check-in đến check-out.
* `SessionExtensions`: Quản lý lịch sử gia hạn giờ.
* `SessionOrders` & `SessionOrderDetails`: Quản lý danh sách dịch vụ F&B phát sinh trong phiên.

### 5. Phân hệ Sản phẩm & Tiện ích
* `Categories`: Nhóm danh mục.
* `Products`: Danh mục món ăn, thức uống, thiết bị cho thuê.

### 6. Phân hệ Thanh toán & Khuyến mãi 
* `Vouchers`: Cấu hình các chiến dịch giảm giá (Theo % hoặc số tiền cố định).
* `Payments`: Bảng lưu vết lịch sử giao dịch. Hỗ trợ thanh toán linh hoạt cho cả Booking (Tiền cọc) và Session (Hóa đơn cuối cùng).

---
