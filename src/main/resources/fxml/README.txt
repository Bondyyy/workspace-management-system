THU MUC: resources/fxml/
=========================
Chua cac file FXML - mo ta giao dien JavaFX bang XML.
Moi file FXML tuong ung voi 1 Controller trong controller/

CACH LIEN KET FXML VOI CONTROLLER
===================================
  Trong file FXML: fx:controller='com.wms.controller.LoginController'
  Trong Controller: cac field @FXML phai trung ten voi fx:id trong FXML

CACH CHINH SUA GIAO DIEN
=========================
  1. Mo Scene Builder (cai tai gluonhq.com - mien phi)
  2. File -> Open -> chon file .fxml can sua
  3. Keo tha component tu panel trai vao canvas
  4. Dat fx:id cho component (de Controller truy cap)
  5. Luu lai -> F6 trong NetBeans kiem tra ngay

DANH SACH FILE
==============
  Login.fxml          Man hinh dang nhap
  Register.fxml       Man hinh dang ky (Khach hang)
  PosMain.fxml        Man hinh chinh POS (Le tan - so do + session list)
  SpaceMap.fxml       So do mat bang tuong tac
  Booking.fxml        Form dat cho
  Checkin.fxml        Quet/nhap ma QR
  Order.fxml          Goi mon FnB
  Invoice.fxml        Hoa don tong hop
  Report.fxml         Bao cao doanh thu (co bieu do)
  Admin.fxml          Quan tri he thong (chi ADMIN)
  CustomerPortal.fxml  Portal khach hang
  CleanerView.fxml     Man hinh tap vu
