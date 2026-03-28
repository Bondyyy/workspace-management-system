THU MUC: src/test/java/com/wms/
================================
Chua toan bo Unit Test viet bang JUnit 5.
Yeu cau do an: 'Viet Unit Test co ban bang JUnit'

QUY TAC VIET TEST
=================
  - Ten file: [TenClass]Test.java  VD: InvoiceDAOTest.java
  - Moi method test: @Test annotation, bat dau bang 'test' hoac mo ta hanh dong
  - Dung Assertions: assertEquals(), assertTrue(), assertThrows()...
  - Moi test doc lap: chay 1 test khong anh huong test khac
  - Test ca truong hop thanh cong VA truong hop that bai

3 FILE TEST BAT BUOC
====================

dao/InvoiceDAOTest.java
  Test tinh toan hoa don - quan trong nhat, sai la mat tien khach hang:
    testGenerateInvoice_SpaceOnly()       Chi thue cho, khong goi mon
    testGenerateInvoice_WithFnB()         Co goi mon FnB
    testGenerateInvoice_WithExtension()   Co gia han them gio
    testGenerateInvoice_WithDeposit()     Tru tien coc da dat truoc
    testGenerateInvoice_WithVoucher()     Ap dung ma giam gia
    testGenerateInvoice_Full()            Tat ca cong lai: dung cong thuc

dao/BookingDAOTest.java
  Test chong double-booking:
    testCreate_Success()                  Dat cho binh thuong thanh cong
    testCreate_ThrowException_WhenOccupied() 2 nguoi dat 1 ban -> loi
    testCancelExpired_CancelsAfter10Min() Don qua 10 phut tu huy

util/InputValidatorTest.java
  Test kiem tra du lieu nhap:
    testEmail_Valid()    testEmail_Invalid()
    testPhone_Valid()    testPhone_Invalid()
    testPassword_TooShort()  testPassword_Valid()

CACH CHAY TEST
==============
  NetBeans: Click phai vao file Test -> Test File (hoac Ctrl+F6)
  Terminal: mvn test
  Xem ket qua: NetBeans hien panel Test Results (xanh=pass, do=fail)
