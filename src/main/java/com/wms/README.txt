THU MUC: com/wms/ (Package goc)
=================================
Day la noi chua toan bo code Java cua he thong.
Ung dung la Desktop App chay bang JavaFX - KHONG PHAI web, KHONG PHAI Spring Boot.

KIEN TRUC MVC + 3 LOP
======================
  View (Giao dien)    <-> FXML + Controller
  Controller          <-> Xu ly su kien, dieu phoi
  DAO (Data Access)   <-> Truy van Oracle bang JDBC
  Model               <-> Du lieu (POJO thuan)

CACH CHAY UNG DUNG
==================
  Trong NetBeans: F6 -> Cua so app mo ra (KHONG can browser)
  Build .jar:     Shift+F11 -> ra file dist/WMS.jar
  Chay .jar:      Double-click WMS.jar hoac: java -jar WMS.jar
  Yeu cau:        Oracle dang chay + config.properties da dien dung

CAC THU MUC BEN TRONG
=====================
  config/     Ket noi Oracle (JDBC Singleton)
  model/      POJO - du lieu tuong ung bang Oracle
  dao/        Truy van Oracle (tu viet SQL bang JDBC)
  controller/ Xu ly su kien JavaFX (@FXML)
  view/       Cac class View ho tro (neu can)
  util/       Tien ich dung chung
