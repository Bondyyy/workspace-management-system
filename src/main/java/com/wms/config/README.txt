THU MUC: config/
=================
Quan ly ket noi den Oracle Database - chi co 1 file duy nhat.

FILE TRONG THU MUC NAY
=======================

DatabaseConnection.java
  - Pattern: SINGLETON - chi tao 1 ket noi duy nhat trong suot vong doi app
  - Cach dung: DatabaseConnection.getInstance().getConnection()
  - Doc thong tin tu: src/main/resources/config.properties
  - Dung JDBC thuan: Class.forName("oracle.jdbc.OracleDriver")
                     DriverManager.getConnection(url, user, pass)

  TAI SAO SINGLETON?
    Neu moi DAO tu tao Connection rieng -> Oracle bi qua tai, chay cham
    Singleton dam bao chi co 1 connection, tat ca DAO dung chung

  CACH HOAT DONG (thu tu code):
    1. Doc config.properties: db.url, db.username, db.password
    2. Load Oracle driver: Class.forName(...)
    3. Tao connection: DriverManager.getConnection(...)
    4. Cac DAO goi getInstance().getConnection() de lay connection
    5. KHONG goi connection.close() trong DAO - de Singleton quan ly

  LUU Y QUAN TRONG:
    - File config.properties co password Oracle -> KHONG commit len GitHub
    - Chi commit config.example.properties (placeholder, khong co password that)
    - Neu Oracle chua chay khi mo app -> hien dialog bao loi ket noi
