THU MUC: src/main/resources/
==============================
Chua tat ca file KHONG PHAI Java ma ung dung can khi chay.
Maven tu dong sao chep thu muc nay vao file .jar khi build.

CAU TRUC
========
  fxml/               File thiet ke giao dien JavaFX
  css/                File CSS tuy chinh giao dien
  img/                Anh, icon, logo dung trong app
  config.properties   GITIGNORE - co Oracle password - moi nguoi tu tao
  config.example.properties  COMMIT len Git - chi co placeholder

CACH DOC FILE TRONG RESOURCES TU JAVA
======================================
  // Doc config.properties
  InputStream is = getClass().getResourceAsStream('/config.properties');
  Properties props = new Properties();
  props.load(is);

  // Load FXML
  FXMLLoader loader = new FXMLLoader(getClass().getResource('/fxml/Login.fxml'));

  // Doc CSS
  scene.getStylesheets().add(getClass().getResource('/css/style.css').toExternalForm());
