THU MUC: resources/css/
========================
Chua file CSS tuy chinh giao dien JavaFX.

JavaFX ho tro CSS tuong tu web nhung co mot so property khac:
  Web CSS:    color: #333;            background-color: #fff;
  JavaFX CSS: -fx-text-fill: #333;   -fx-background-color: #fff;

FILE TRONG THU MUC NAY
=======================
  style.css     CSS toan cuc - ap dung cho toan bo app
                Dinh nghia: mau nen chinh, font, nut, bang...
                Duoc load trong MainApp.java hoac tung FXML

HUONG DAN DUNG CSS TRONG JAVAFX
================================
  1. Dat class CSS cho component trong FXML: styleClass='btn-primary'
  2. Hoac dat truc tiep: style='-fx-background-color: #2196F3;'
  3. Load CSS vao Scene: scene.getStylesheets().add('.../css/style.css')
