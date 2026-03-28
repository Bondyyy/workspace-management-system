THU MUC: util/ (Utility)
=========================
Chua cac class tien ich dung chung trong toan du an.
Khong thuoc tang nao cu the, phuc vu cho tat ca cac tang.

DANH SACH FILE VA NHIEM VU

SceneManager.java
  TAC DUNG: Chuyen doi giua cac man hinh JavaFX tu 1 cho duy nhat
  PHUONG THUC:
    switchTo(String fxmlName)               Chuyen man hinh khong data
    switchTo(String fxmlName, Object data)  Chuyen man hinh kem theo du lieu
    getCurrentStage() -> Stage              Lay Stage hien tai
  VI DU: SceneManager.switchTo('SpaceMap.fxml')
  TAI SAO CAN: Neu khong co, moi Controller tu load FXML bang FXMLLoader
  -> Code lap 10 lan, kho bao tri, kho thay doi theme

QrCodeUtil.java
  TAC DUNG: Sinh anh QR PNG tu chuoi noi dung
  THU VIEN: ZXing (da khai bao trong pom.xml)
  PHUONG THUC:
    generateImage(String content, int size) -> BufferedImage
    generateBytes(String content) -> byte[]
  NOI DUNG MA QR: WMS_BOOKING_{bookingId}_{timestamp}_{hmacSignature}
  MA QR 1 LAN: Sau khi quet -> BookingDAO.markQrUsed() -> quet lan 2 bi tu choi

PasswordUtil.java
  TAC DUNG: Ma hoa mat khau truoc khi luu Oracle
  THUAT TOAN: SHA-256 (Java Core, khong can thu vien ngoai)
  PHUONG THUC:
    hash(String plainPassword) -> String hexHash
    verify(String plain, String hashed) -> boolean
  LUU Y: KHONG BAO GIO luu mat khau dang plain text vao Oracle
  CHONG: SQL Injection tu dong qua PreparedStatement trong DAO

DateTimeUtil.java
  TAC DUNG: Xu ly ngay gio cho cac tinh toan nghiep vu
  PHUONG THUC:
    calcDurationMinutes(Timestamp from, Timestamp to) -> long
    calcExtensionCost(int minutes, double pricePerHour) -> double
    formatDuration(long minutes) -> String  VD: '2 gio 30 phut'
    formatCurrency(double) -> String        VD: '150,000 VND'
    formatDateTime(Timestamp) -> String     VD: '24/03/2026 14:30'

SessionState.java
  TAC DUNG: Luu trang thai nguoi dung dang dang nhap (khong co session nhu web)
  PHUONG THUC:
    setCurrentUser(User user) / getCurrentUser() -> User
    getCurrentRole() -> UserRole
    isLoggedIn() -> boolean
    logout() -> void (xoa user, chuyen ve Login.fxml)
  TAI SAO CAN: Moi Controller can biet ai dang dang nhap de kiem tra quyen

InputValidator.java
  TAC DUNG: Kiem tra du lieu nhap vao truoc khi gui DAO
  PHUONG THUC:
    isNotEmpty(String) -> boolean           Khong de trong
    isValidEmail(String) -> boolean         Dung dinh dang email
    isValidPhone(String) -> boolean         So dien thoai Viet Nam hop le
    isPositiveNumber(String) -> boolean     La so duong
    isValidPassword(String) -> boolean      Toi thieu 6 ky tu, co chu + so
  TAI SAO CAN: Bat loi som o tang View, khong de loi loi vao DAO/Oracle
  LUU Y: PreparedStatement trong DAO da chong SQL Injection - InputValidator
         chi kiem tra format, khong phai bao mat
