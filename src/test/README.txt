THU MUC: src/test/
===================
Chua toan bo code kiem thu bang JUnit 5.

Cau truc GIONG HET voi src/main/ (cung package com/wms/...):
  main/java/com/wms/dao/InvoiceDAO.java      <- code that
  test/java/com/wms/dao/InvoiceDAOTest.java  <- test cho no

TAI SAO CAN VIET TEST?
  - Yeu cau do an: "Viet Unit Test co ban bang JUnit"
  - Tranh bug tinh toan sai: 1 loi nho trong InvoiceDAO -> khach bi tinh sai tien
  - Chay: mvn test hoac F6 tren file test trong NetBeans

3 FILE TEST QUAN TRONG NHAT (yeu cau do an):
  dao/InvoiceDAOTest.java    Test tinh toan hoa don (SPACE + FnB + Extension - Deposit - Voucher)
  dao/BookingDAOTest.java    Test chong double-booking (2 nguoi dat 1 ban cung luc)
  util/InputValidatorTest.java  Test kiem tra du lieu nhap vao tu form
