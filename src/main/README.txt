THU MUC: src/main/
===================
Chua code san pham chinh cua ung dung.

Ben trong co 2 thu muc:
  java/       Toan bo file .java (Maven tu compile thu muc nay)
  resources/  File khong phai Java: FXML, CSS, anh, config (Maven tu copy vao .jar)

TAI SAO TACH java/ VA resources/?
  - Maven xu ly chung khac nhau:
    + java/      -> Duoc BIEN DICH (compile) thanh bytecode .class
    + resources/ -> Duoc SAO CHEP NGUYEN XI vao trong file .jar
  - Khi chay app, Java tim FXML qua classpath trong resources/ - khong phai o ngoai
