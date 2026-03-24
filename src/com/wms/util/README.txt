THU MUC: util/ (Utility - Tien ich)
=====================================
Chua cac class ham tien ich dung chung, khong thuoc ve tang cu the nao.

DANH SACH FILE
==============

DatabaseConnection.java  (xem them o config/README.txt)

SceneManager.java
  TAC DUNG: Quan ly viec chuyen doi giua cac man hinh JavaFX
  CACH DUNG:
    SceneManager.switchTo("SpaceMap.fxml");         // chuyen man hinh
    SceneManager.switchTo("Order.fxml", sessionId); // truyen them data
    SceneManager.getCurrentStage();                 // lay Stage hien tai
  TAI SAO CAN: Khong co no, moi Controller tu load FXML bang FXMLLoader
    -> code lap lai 10 lan, kho bao tri

QrCodeUtil.java
  TAC DUNG: Sinh anh QR PNG tu chuoi van ban
  THU VIEN: ZXing (com.google.zxing) - them jar vao Libraries NetBeans
  PHUONG THUC:
    generateQrImage(String content, int size) -> BufferedImage
    generateQrBytes(String content) -> byte[]
  NOI DUNG MA QR:
    "WMS_BOOKING_[bookingId]_[timestamp]_[hmacSignature]"
    Vi du: "WMS_BOOKING_1234_1711234567_a3f9c2..."
  MA QR 1 LAN: BookingDAO danh dau QR_USED=1 sau khi quet thanh cong

PasswordUtil.java
  TAC DUNG: Ma hoa mat khau truoc khi luu vao Oracle
  THUAT TOAN: SHA-256 (khong dung BCrypt vi Java Core)
  PHUONG THUC:
    hashPassword(String plainPassword) -> String hashedHex
    checkPassword(String plain, String hashed) -> boolean
  LUU Y: KHONG BAO GIO luu mat khau dang plain text vao DB

DateTimeUtil.java
  TAC DUNG: Xu ly ngay thoi gian cho cac tinh toan nghiep vu
  PHUONG THUC:
    calculateDurationMinutes(Timestamp from, Timestamp to) -> long
      -> Dung tinh gio ngoi: checkout_time - checkin_time
    calculateExtensionCost(int minutes, double pricePerHour) -> double
      -> extra_cost = minutes * (pricePerHour / 60.0)
    formatDuration(long minutes) -> String
      -> "2 gio 30 phut"
    formatCurrency(double amount) -> String
      -> "150,000 VND"
    formatDateTime(Timestamp ts) -> String
      -> "24/03/2026 14:30"

SessionState.java
  TAC DUNG: Luu trang thai nguoi dung dang dang nhap (thay the Session web)
  PHUONG THUC:
    setCurrentUser(User user)
    getCurrentUser() -> User
    getCurrentRole() -> UserRole
    isLoggedIn() -> boolean
    logout()
  TAI SAO CAN: JavaFX khong co session nhu web, can class static de luu
    user dang nhap de kiem tra quyen han o moi Controller
