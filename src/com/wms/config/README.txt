THU MUC: config/
=================
Quan ly ket noi den Oracle Database.

FILE TRONG THU MUC NAY
=======================

DatabaseConnection.java
  - Singleton Pattern: chi co 1 instance duy nhat trong toan app
  - Phuong thuc chinh: getInstance(), getConnection(), closeConnection()
  - Doc thong tin tu resources/config.properties (url, user, password)
  - Su dung: Connection conn = DatabaseConnection.getInstance().getConnection()
  - TAI SAO SINGLETON: tranh tao qua nhieu ket noi den Oracle, tiet kiem tai nguyen

CACH HOAT DONG
==============
  1. Doc file config.properties: url=jdbc:oracle:thin:@localhost:1521:XE
  2. Goi DriverManager.getConnection(url, user, password)
  3. Tat ca DAO goi DatabaseConnection.getInstance().getConnection() de lay ket noi
  4. Khong DAO nao tu tao Connection rieng

LUU Y
=====
  - KHONG commit file config.properties len GitHub (co password Oracle)
  - Chi commit config.example.properties (khong co password that)
  - Neu ket noi that bai: kiem tra Oracle dang chay, kiem tra port 1521
