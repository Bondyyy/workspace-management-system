MODULE    : booking
MUC DICH  : Xu ly dat cho ONLINE (qua web) va OFFLINE (Le tan tai quay)
            Day la module quan trong nhat - chong double-booking bang SELECT FOR UPDATE
MAP ORACLE: BOOKINGS, BOOKING_DETAILS

FILES CAN TAO
=============

entity/Booking.java
  - Fields: id, customer_id, status, channel(ONLINE/OFFLINE)
  - Status: PENDING -> BOOKED -> ACTIVE -> COMPLETED | CANCELLED

entity/BookingDetail.java
  - Fields: id, booking_id, space_id, price_at_booking
  - price_at_booking: snapshot gia tai thoi diem dat (khong doi du gia sau nay thay doi)

controller/BookingController.java
  - POST   /api/bookings              : Tao don dat cho moi
  - GET    /api/bookings/{id}         : Chi tiet don dat cho
  - PATCH  /api/bookings/{id}/cancel  : Huy don
  - GET    /api/bookings/my           : Lich su dat cho cua customer hien tai

service/BookingService.java

  createBooking() - PHAI THUC HIEN DUNG 5 BUOC NAY:
  ===================================================
  B1: @Transactional + SELECT FOR UPDATE space_id
      -> Khoa dong trong Oracle, khong process nao khac chen vao duoc
  B2: Kiem tra space.status == AVAILABLE
      -> Neu khong -> throw BookingConflictException (HTTP 409)
  B3: Snapshot price_at_booking = space.price_per_hour hien tai
  B4: Tao Booking(PENDING), BookingDetail, chuyen Space -> BOOKED
      Goi QrCodeService.generateQr(bookingId) -> luu qr_code
  B5: Schedule auto-cancel sau 10 phut neu chua thanh toan coc

  cancelBooking(bookingId)
  - Chuyen Booking.status -> CANCELLED
  - Chuyen Space.status -> AVAILABLE
  - Goi SpaceService.broadcastStatusChange() de cap nhat so do real-time

  expireUnpaidBookings()
  - Annotation: @Scheduled(fixedRate = 60000) chay moi 1 phut
  - Tim tat ca Booking PENDING qua 10 phut, goi cancelBooking()
