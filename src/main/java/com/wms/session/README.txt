MODULE    : session
MUC DICH  : Quan ly vong doi phien lam viec: check-in -> gia han -> check-out
MAP ORACLE: SESSIONS, SESSION_EXTENSIONS

FILES CAN TAO
=============

entity/Session.java
  - Fields: id, booking_id, space_id, checkin_time, checkout_time
            status(ACTIVE/COMPLETED), total_duration_minutes

entity/SessionExtension.java
  - Fields: id, session_id, extended_minutes, extra_cost, extended_at

controller/SessionController.java
  - POST /api/sessions/checkin         : Le tan quet QR -> tao Session
  - POST /api/sessions/{id}/extend     : Gia han them X phut
  - POST /api/sessions/{id}/checkout   : Ket thuc phien
  - GET  /api/sessions/{id}            : Trang thai phien hien tai
  - GET  /api/sessions/active          : Danh sach phien dang chay (man hinh POS)

service/SessionService.java

  checkin(bookingId)
  - Tao Session moi voi status = ACTIVE, ghi checkin_time = NOW()
  - Chuyen Space: BOOKED -> OCCUPIED
  - Chuyen Booking: BOOKED -> ACTIVE, ghi check_in_time
  - Broadcast WebSocket cap nhat so do

  extend(sessionId, minutes)
  - Tao SessionExtension moi
  - extra_cost = minutes * (price_per_hour / 60.0)

  checkout(sessionId)
  - Ghi checkout_time = NOW()
  - Tinh total_duration_minutes = DIFF(checkout_time, checkin_time)
  - Chuyen Space: OCCUPIED -> CLEANING
  - Chuyen Session: ACTIVE -> COMPLETED
  - Goi InvoiceService.generateInvoice(sessionId) tu dong tao hoa don

  calculateDuration(sessionId)
  - Tra ve so phut tu checkin_time den NOW() (de hien thi thoi gian thuc)
