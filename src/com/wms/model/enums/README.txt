THU MUC: model/enums/
======================
Chua cac enum (tap gia tri co dinh) dung trong toan he thong.
Dung enum thay vi String de compiler phat hien loi gay chinh ta:
  SAI : "AVAILBLE" -> app chay nhung bug am tham
  DUNG: SpaceStatus.AVAILABLE -> khong compile duoc neu sai

DANH SACH ENUM
==============

SpaceStatus.java
  - AVAILABLE   : Ban/phong trong, co the dat
  - BOOKED      : Da co nguoi dat (deposit), chua den
  - OCCUPIED    : Dang co nguoi ngoi (da check-in)
  - CLEANING    : Dang don dep sau khi khach ra (chua cho dat lai)
  - MAINTENANCE : Bao tri, khong phuc vu
  -> Tuong ung cot STATUS trong bang SPACES cua Oracle

BookingStatus.java
  - PENDING     : Vua tao don, chua thanh toan coc (giu 10 phut)
  - BOOKED      : Da thanh toan coc, cho khach den
  - ACTIVE      : Khach da check-in, dang trong phien lam viec
  - COMPLETED   : Khach da checkout, dong don
  - CANCELLED   : Don bi huy (het 10 phut chua tra coc, hoac tu huy)

SessionStatus.java
  - ACTIVE      : Phien dang chay (khach dang ngoi)
  - COMPLETED   : Phien da ket thuc (checkout xong)

UserRole.java
  - ADMIN       : Quan tri toan quyen
  - RECEPTIONIST: Le tan - check-in, goi mon, xuat hoa don
  - CLEANER     : Tap vu - chi thay duoc ban can don dep
  - CUSTOMER    : Khach hang - dat cho online

PaymentMethod.java
  - CASH        : Tien mat tai quay
  - TRANSFER    : Chuyen khoan
  - VNPAY       : Thanh toan online qua VNPay (neu co tich hop)

InvoiceLineType.java
  - SPACE       : Tien thue khong gian (tinh theo gio)
  - FNB         : Tong tien do uong/an uong goi them
  - EXTENSION   : Phi gia han them gio
  - DEPOSIT     : Tru tien coc da nop (so am)
  - VOUCHER     : Giam gia voucher hoac hang thanh vien (so am)
