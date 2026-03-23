MODULE    : qr
MUC DICH  : Sinh ma QR PNG cho Booking, xac thuc khi Le tan quet
            Moi ma QR chi dung duoc 1 LAN (yeu cau SOW muc 1.2)
THU VIEN  : ZXing - them vao pom.xml: com.google.zxing:core:3.5.2

FILES CAN TAO
=============

QrCodeService.java
  - generateQr(bookingId)
      Tao anh QR PNG ma hoa bookingId + timestamp + chu ky HMAC-SHA256
      Tra ve byte[] de luu vao DB hoac tra ve FE hien thi

  - validateQr(qrPayload)
      Giai ma payload, kiem tra chu ky HMAC
      Kiem tra DB/cache xem ma nay da duoc quet chua
      Neu da quet -> throw QrAlreadyUsedException

  - markAsUsed(bookingId)
      Ghi vao DB rang QR cua booking nay da su dung
      Bat ky lan quet thu 2 se bi tu choi voi HTTP 400

QrScanController.java
  - POST /api/qr/scan          : Le tan quet QR -> validateQr -> tao Session moi
  - GET  /api/qr/{bookingId}   : Lay anh QR PNG de hien thi cho khach (base64 hoac file)
