THU MUC: controller/
=====================
Chua cac Controller JavaFX - xu ly su kien nguoi dung va dieu phoi du lieu.
Moi Controller gan voi 1 file FXML trong resources/fxml/

QUY TAC VIET CONTROLLER
========================
  - Implement Initializable de chay code khi man hinh mo: initialize()
  - Cac component UI khai bao @FXML: @FXML private Button btnLogin
  - Xu ly su kien bang @FXML: @FXML private void onLoginClick()
  - Goi DAO de lay du lieu: UserDAO.findByUsername(username)
  - Cap nhat UI sau khi co du lieu: tableView.getItems().setAll(list)
  - Chuyen man hinh: SceneManager.switchTo("SpaceMap.fxml")
  - KHONG viet SQL o day - SQL chi o DAO

DANH SACH FILE
==============

LoginController.java  <-> Login.fxml
  - Xu ly su kien bam nut Dang nhap
  - Lay username + password tu TextField
  - Goi UserDAO.checkPassword() xac thuc
  - Hash password truoc khi so sanh: PasswordUtil.hash(password)
  - Neu dung: luu user vao Session, chuyen den man hinh chinh theo role
    ADMIN/RECEPTIONIST -> PosMain.fxml
    CUSTOMER -> CustomerPortal.fxml
  - Neu sai: hien Alert "Sai tai khoan hoac mat khau"

SpaceMapController.java  <-> SpaceMap.fxml
  - Hien thi so do mat bang bang cac nut/o mau sac theo trang thai:
    AVAILABLE=xanh, BOOKED=vang, OCCUPIED=do, CLEANING=xam
  - Click vao o trong -> mo dialog dat cho hoac check-in
  - Tu dong lam moi trang thai moi X giay (Timer/Timeline JavaFX)
  - Goi SpaceDAO.findAll(branchId) lay du lieu

BookingController.java  <-> Booking.fxml
  - Form dat cho: chon khach hang, chon khung gio, xem gia
  - Bam "Dat cho": goi BookingDAO.create() - co xu ly double-booking
  - Neu thanh cong: sinh ma QR qua QrCodeUtil, hien ma QR len man hinh
  - Neu that bai (da co nguoi dat): hien thong bao loi

CheckinController.java  <-> Checkin.fxml
  - Le tan nhap ma QR hoac quet QR camera
  - Goi BookingDAO.findByQrCode() kiem tra ma hop le
  - Kiem tra QR chua dung lan nao: BookingDAO.isQrUsed()
  - Neu hop le: goi SessionDAO.checkin() tao Session moi
  - Neu da quet roi: hien Alert "Ma QR nay da duoc su dung"

SessionController.java  <-> PosMain.fxml
  - Hien danh sach tat ca phien dang ACTIVE
  - Moi phien hien: ten khach, ban so may, thoi gian da ngoi, tong tien tam tinh
  - Nut "Gia han": mo dialog nhap so phut, goi SessionDAO.extend()
  - Nut "Checkout": goi SessionDAO.checkout() + InvoiceDAO.generate()
  - Dong ho dem gio chay theo thoi gian thuc (Timeline JavaFX)

OrderController.java  <-> Order.fxml
  - Hien menu theo danh muc (tabs hoac ListView)
  - Bam mon -> them vao gio hang (goi OrderDAO.addItem())
  - Tang/giam so luong -> goi OrderDAO.updateItem() - delta
  - Xoa mon -> goi OrderDAO.removeItem()
  - Hien tong tien cap nhat theo thoi gian thuc

InvoiceController.java  <-> Invoice.fxml
  - Hien hoa don tong hop: bang chi tiet tung InvoiceLine
  - Hien ro: tien ghe + FnB + gia han - coc - voucher = TONG PHAI TRA
  - O nhap ma voucher: goi VoucherDAO.validateVoucher() roi InvoiceDAO.applyVoucher()
  - Nut "In hoa don": in ra may in hoac xuat PDF

ReportController.java  <-> Report.fxml
  - Chon ngay/khung thoi gian
  - Hien bieu do doanh thu tach: cot SPACE vs cot FnB (dung JavaFX Chart)
  - Hien ty le lap day theo gio trong ngay
  - Nut "Xuat CSV": goi ReportDAO roi ghi ra file .csv

AdminController.java  <-> Admin.fxml
  - Tab quan ly Space: them/sua/xoa ban phong, cau hinh gia
  - Tab quan ly Menu: them/sua/xoa mon an do uong, bat/tat mon
  - Tab quan ly Users: them nhan vien, doi mat khau, phan quyen
  - Chi ADMIN moi vao duoc man hinh nay
