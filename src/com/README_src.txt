THU MUC: src/com/wms/
======================
Day la thu muc goc chua toan bo code Java cua du an.
Ung dung la Desktop App chay bang JavaFX (khong phai web browser).

CAU TRUC TONG QUAN
==================
  MainApp.java     Diem khoi dong - extends Application, mo cua so dau tien
  config/          Ket noi Oracle bang JDBC
  model/           Du lieu (POJO thuan - khong annotation)
  dao/             Truy van Oracle (tu viet SQL bang JDBC)
  controller/      Xu ly su kien JavaFX (@FXML)
  util/            Cac ham tien ich dung chung

LUONG DU LIEU
=============
  Nguoi dung bam nut (View/FXML)
    -> Controller nhan su kien (@FXML)
    -> Controller goi DAO lay/luu du lieu
    -> DAO ket noi Oracle qua DatabaseConnection
    -> DAO tra ve Model object
    -> Controller cap nhat lai View

LUU Y QUAN TRONG
================
  - Khong co Spring, khong co JPA, khong co annotation @Entity
  - Ket noi Oracle dung JDBC thuan: DriverManager.getConnection()
  - Tu viet SQL: SELECT * FROM SPACES WHERE STATUS = ?
  - Tu doc ResultSet: rs.getString("SPACE_NAME")
  - Giao dien viet bang FXML + Scene Builder (khong phai HTML)
