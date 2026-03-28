THU MUC: view/
===============
Tang 1 trong kien truc MVC - chua cac class ho tro hien thi giao dien.

Giao dien chinh duoc dinh nghia trong FXML (resources/fxml/)
Thu muc view/ chua cac Java class bo tro cho FXML:

  BaseView.java         Abstract class - cac tinh nang chung cua tat ca man hinh
                        Phuong thuc: showAlert(), showConfirm(), showError()
                        Moi Controller co the extends hoac goi cac ham nay

  CleanerView.java      Man hinh danh rieng cho Tap vu
                        Hien danh sach ban co status=CLEANING
                        Nut 'Da don xong' -> SpaceDAO.updateStatus(AVAILABLE)

  CustomerPortalView.java  Man hinh danh rieng cho Khach hang
                           Xem so do chon cho, lich su dat cho, diem tich luy

LUU Y QUAN TRONG:
  Moi thay doi giao dien FXML: mo file .fxml bang Scene Builder (keo tha)
  KHONG sua FXML bang tay (XML kho doc, de loi)
  Sau khi sua FXML: chay F6 kiem tra ngay, tranh de den cuoi moi phat hien loi
