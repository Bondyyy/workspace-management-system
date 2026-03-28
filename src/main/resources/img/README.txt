THU MUC: resources/img/
========================
Chua hinh anh, icon dung trong giao dien JavaFX.

CACH DUNG ANH TRONG JAVAFX
===========================
  // Trong FXML:
  <ImageView><image><Image url='@../img/logo.png'/></image></ImageView>

  // Trong Java:
  Image img = new Image(getClass().getResourceAsStream('/img/logo.png'));
  imageView.setImage(img);

NEN DE TRONG THU MUC NAY
=========================
  logo.png         Logo he thong hien o man hinh Login
  icon_available.png, icon_booked.png...  Icon trang thai ban
  avatar_default.png  Anh dai dien mac dinh cho user
  no_image.png     Anh mac dinh khi menu item chua co anh
