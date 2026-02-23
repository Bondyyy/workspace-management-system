# 🏢 Hệ thống Quản lý Không gian Làm việc & Học tập 

## 📖 Giới thiệu 
Dự án này cung cấp giải pháp phần mềm toàn diện cho việc quản lý và vận hành chuỗi không gian làm việc/học tập. Hệ thống giải quyết trọn vẹn bài toán vận hành Online to Offline, từ khâu khách hàng đặt chỗ trực tuyến/tại quầy, quản lý thời gian sử dụng thực tế, gọi món F&B, cho đến thanh toán tổng hợp, áp dụng khuyến mãi thành viên và dọn dẹp không gian.

---

## ⚙️ Quy trình Vận hành Cốt lõi
Hệ thống xoay quanh 4 quy trình vận hành chính, được ánh xạ trực tiếp vào cơ sở dữ liệu:

### 1. Quản lý Đặt chỗ (Online & Offline)
* **Tìm kiếm:** Khách hàng truy cập nền tảng Online hoặc Lễ tân tra cứu trên phần mềm nội bộ để lọc các không gian (`Spaces`) dựa trên chi nhánh (`Branches`), loại bàn (`SpaceTypes`) và trạng thái `AVAILABLE`.
* **Tạo phiếu đặt:** Hệ thống hỗ trợ 2 luồng đặt chỗ được phân loại qua `booking_channel`:
  * **ONLINE:** Khách thành viên tự đặt qua App/Web. Giao dịch cọc được ghi nhận, trạng thái không gian chuyển sang `BOOKED` và hệ thống cấp một `qr_code` duy nhất.
  * **OFFLINE:** Lễ tân thao tác đặt chỗ trước cho khách ngay tại quầy. Hệ thống hỗ trợ lưu thông tin cho cả Thành viên (`customer_id`) lẫn Khách vãng lai (`guest_name`, `guest_phone`), đồng thời lưu vết nhân viên tạo phiếu (`created_by_staff_id`).
* **Snapshot Giá:** Giá trị thuê tại thời điểm đặt luôn được snapshot vào `price_at_booking` trong `BookingDetails` để tránh rủi ro thay đổi giá trong tương lai.

### 2. Check-in và Khởi tạo Phiên làm việc 
* **Đối soát (Có đặt trước):** Khách xuất trình `qr_code` (Online) hoặc thông tin cá nhân (Offline). Hệ thống giải mã và đối chiếu với bảng `Bookings` để xác nhận.
* **Khách Walk-in (Vào ngồi ngay):** Lễ tân trực tiếp xếp bàn cho khách đến nhánh mà không cần qua bước đặt trước.
* **Mở phiên (Session):** Hệ thống tạo một `Sessions` mới, ghi nhận `checkin_time` và nhân viên hỗ trợ (`check_in_staff_id`). Với khách vãng lai, thông tin có thể được linh hoạt ẩn danh hoặc lưu dưới dạng `guest_name`.
* **Cập nhật trạng thái:** Trạng thái bàn/phòng trong `Spaces` chính thức chuyển sang `OCCUPIED`. Mọi chi phí từ lúc này sẽ được cộng dồn vào Session ID.

### 3. Sử dụng Dịch vụ (F&B) và Gia hạn Giờ 
* **Gọi món (F&B):** Khách quét QR tại bàn hoặc gọi tại quầy. Hệ thống tra cứu thực đơn từ `Products` và `Categories` (chỉ hiển thị món có status `AVAILABLE`). Đơn hàng được ghi nhận vào `SessionOrders` và `SessionOrderDetails` với trạng thái thanh toán tạm để `UNPAID`.
* **Gia hạn giờ (Extension):** Nếu khách muốn ngồi lâu hơn dự kiến, thông tin gia hạn được ghi vào `SessionExtensions` với khoảng thời gian gia hạn và tính toán trước `cost_incurred` (phí phát sinh).

### 4. Check-out và Thanh toán Tổng hợp
* **Đóng phiên (Close Session):** Hệ thống ghi nhận `checkout_time` trong bảng `Sessions`.
* **Tổng hợp hóa đơn:** Hệ thống tự động tính toán tổng chi phí dựa trên: Tiền thuê gốc (`Sessions`) + Phí gia hạn (`SessionExtensions`) + Chi phí F&B (`SessionOrders`).
* **Áp dụng Khuyến mãi & Hạng thành viên:** * Kiểm tra và trừ tiền cọc (`deposit_amount`) nếu có.
  * Nếu khách dùng Voucher, hệ thống kiểm tra điều kiện trong bảng `Vouchers`.
  * Nếu khách là Thành viên, hệ thống kiểm tra `MembershipTiers` để áp dụng phần trăm giảm giá (`discount_percent`) tự động dựa trên cấp bậc.
* **Thanh toán & Thu hồi không gian:** Giao dịch thanh toán (Tiền mặt, VNPay, MoMo...) được lưu vào `Payments`. Bàn làm việc trong bảng `Spaces` lập tức chuyển sang trạng thái `CLEANING` chờ nhân viên dọn dẹp trước khi quay về `AVAILABLE`.

---

## 🗄 Kiến trúc Cơ sở Dữ liệu 

### 1. Phân hệ Danh tính & Phân quyền (Identities)
* `Roles`, `Permissions`, `RolePermissions`: Cấu trúc phân quyền Role-Based Access Control tiêu chuẩn.
* `Users`: Thông tin tài khoản trung tâm.
* `Employees`, `Customers`: Mở rộng thông tin dựa trên quan hệ 1-1 với `Users` (Mô hình Table-Per-Type). Bảng `Customers` tích hợp quản lý điểm thưởng (`loyalty_points`) và liên kết Hạng thành viên.

### 2. Phân hệ Quản lý Không gian (Space Management)
* `Branches`: Quản lý các chi nhánh vật lý.
* `SpaceTypes`: Cấu hình danh mục hạng phòng/bàn, sức chứa và đơn giá.
* `Spaces`: Thực thể định danh từng vị trí ngồi, quản lý `current_status` (AVAILABLE, BOOKED, OCCUPIED, CLEANING) và QR Token check-in.

### 3. Phân hệ Đặt chỗ (Booking)
* `Bookings`: Quản lý giao dịch đặt trước. Đã được nâng cấp để hỗ trợ đa kênh (`booking_channel`: ONLINE/OFFLINE) và quản lý linh hoạt thông tin Khách vãng lai (Guest).
* `BookingDetails`: Quản lý chi tiết từng vị trí không gian được đặt trong một Booking (Hỗ trợ đặt nhiều không gian cùng lúc).

### 4. Phân hệ Phiên làm việc (Sessions)
* `Sessions`: Theo dõi dòng thời gian thực tế của khách từ lúc check-in đến check-out tại không gian. Hỗ trợ khách vãng lai (Walk-in Guests).
* `SessionExtensions`: Quản lý lịch sử yêu cầu gia hạn giờ.
* `SessionOrders` & `SessionOrderDetails`: Quản lý danh sách dịch vụ F&B, thiết bị thuê phát sinh trong phiên.

### 5. Phân hệ Sản phẩm & Tiện ích (Products)
* `Categories`: Nhóm danh mục sản phẩm.
* `Products`: Quản lý danh mục món ăn, thức uống, thiết bị cho thuê kèm mức giá và trạng thái khả dụng.

### 6. Phân hệ Thanh toán & Khuyến mãi (Payment & Sale)
* `MembershipTiers`: Cấu hình các hạng thành viên (Đồng, Bạc, Vàng...) và mức ưu đãi phần trăm tương ứng.
* `Vouchers`: Cấu hình các chiến dịch mã giảm giá (Theo % hoặc số tiền cố định, giới hạn lượt dùng).
* `Payments`: Bảng lưu vết lịch sử giao dịch. Hỗ trợ thanh toán linh hoạt cho cả Booking (Tiền cọc Online/Offline) và Session (Hóa đơn Check-out cuối cùng).

---