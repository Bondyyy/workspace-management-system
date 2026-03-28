THU MUC: src/
==============
Thu muc goc chua toan bo source code - quy dinh bat buoc cua Maven.

Ben trong co 2 thu muc con:
  main/   Code chay that su cua ung dung (duoc dong goi khi build .jar)
  test/   Code kiem thu JUnit (KHONG duoc dong goi vao .jar)

TAI SAO CAN PHAN BIET main/ VA test/?
  - Khi giao file .jar cho giang vien, chi co code trong main/ duoc dong goi
  - Test chi chay khi goi lenh: mvn test (hoac click phai -> Test trong NetBeans)
  - Tach biet giup code production sach, khong bi lan tap boi code test
