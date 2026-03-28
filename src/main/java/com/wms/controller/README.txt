THU MUC: controller/
=====================
Tang 2 trong kien truc MVC - xu ly su kien JavaFX va dieu phoi logic.
Moi Controller gan voi 1 file FXML trong resources/fxml/

QUY TAC VIET CONTROLLER
========================
  - Implement Initializable, override initialize() de chay khi man hinh mo
  - Khai bao component UI: @FXML private Button btnLogin;
  - Xu ly su kien: @FXML private void onLoginClick() { ... }
  - Goi DAO lay du lieu: userDAO.findByUsername(username)
  - Hien loi: InputValidator.validate() truoc khi goi DAO
  - Chuyen man hinh: SceneManager.switchTo('SpaceMap.fxml')
  - TUYET DOI KHONG viet SQL trong Controller - SQL chi o DAO

DANH SACH FILE
==============

LoginController.java        <-> fxml/Login.fxml
  - Xu ly nut Dang nhap: lay username/password, hash, goi UserDAO.checkLogin()
  - Neu dung: luu vao SessionState, phan luong theo role:
      ADMIN/RECEPTIONIST -> PosMain.fxml
      CUSTOMER -> CustomerPortal.fxml
      CLEANER -> CleanerView.fxml
  - Neu sai: hien Alert 'Sai tai khoan hoac mat khau'
  - Validate: khong de trong, username khong co ky tu dac biet

RegisterController.java     <-> fxml/Register.fxml
  - Dang ky tai khoan Khach hang moi
  - Validate: email dung dinh dang, so dien thoai hop le, mat khau du manh
  - Goi CustomerDAO.register(), hien thong bao thanh cong

SpaceMapController.java     <-> fxml/SpaceMap.fxml
  - Ve so do mat bang: moi ban la 1 nut mau sac theo trang thai
    AVAILABLE=xanh, BOOKED=vang, OCCUPIED=do, CLEANING=xam
  - Click ban trong -> mo dialog dat cho hoac check-in
  - Lam moi tu dong moi 30 giay (Timeline JavaFX) de cap nhat real-time

BookingController.java      <-> fxml/Booking.fxml
  - Form dat cho: chon khach, chon khung gio, xem gia uoc tinh
  - Bam 'Dat cho': goi BookingDAO.create()
  - Neu thanh cong: sinh QR qua QrCodeUtil, hien ma QR
  - Neu that bai (double-booking): hien Alert 'Ban nay vua co nguoi dat'

CheckinController.java      <-> fxml/Checkin.fxml
  - Le tan nhap hoac quet ma QR
  - Goi BookingDAO.findByQrCode() -> kiem tra BookingDAO.isQrUsed()
  - Neu QR hop le: goi SessionDAO.checkin() -> man hinh POS chinh
  - Neu QR da quet: Alert 'Ma QR nay da duoc su dung'

SessionController.java      <-> fxml/PosMain.fxml
  - Hien danh sach phien ACTIVE: ten khach, ban, thoi gian ngoi, tam tinh tien
  - Nut 'Goi mon': mo OrderController
  - Nut 'Gia han': dialog nhap phut, goi SessionDAO.extend()
  - Nut 'Checkout': SessionDAO.checkout() -> InvoiceDAO.generate() -> InvoiceController
  - Dong ho dem gio thoi gian thuc (Timeline update moi 1 phut)

OrderController.java        <-> fxml/Order.fxml
  - Hien menu theo tab danh muc (ListView/TableView)
  - Them/sua/xoa mon: goi OrderDAO.addItem/updateItem/removeItem (delta)
  - Hien tong tien cap nhat real-time sau moi thao tac

InvoiceController.java      <-> fxml/Invoice.fxml
  - Hien hoa don tong hop: bang chi tiet tung InvoiceLine
  - O nhap ma voucher: VoucherDAO.isValid() -> InvoiceDAO.applyVoucher()
  - Nut 'In hoa don': xuat PDF bang iTextPDF hoac in may in
  - Hien ro: Tien ghe + FnB + Gia han - Coc - Voucher = TONG PHAI TRA

ReportController.java       <-> fxml/Report.fxml
  - Chon ngay/khoang thoi gian
  - Bieu do JavaFX Chart: doanh thu SPACE vs FNB theo ngay
  - Bang ty le lap day theo khung gio (peak hours)
  - Nut 'Xuat CSV': goi ReportDAO.exportToCsv(), mo file dialog chon noi luu

AdminController.java        <-> fxml/Admin.fxml
  - Tab Space: them/sua/xoa ban phong, sua gia
  - Tab Menu: them/sua/xoa mon, bat/tat mon
  - Tab Users: them nhan vien, phan quyen, doi mat khau
  - Chi ADMIN moi vao duoc - kiem tra trong SessionState.getCurrentRole()
