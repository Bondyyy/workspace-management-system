THU MUC: model/enums/
======================
Chua cac Java Enum - tap gia tri co dinh khong thay doi.

TAI SAO DUNG ENUM THAY VI STRING?
  BAD:  space.setStatus("AVAILBLE")  -> sai chinh ta, compiler khong phat hien, bug am tham
  GOOD: space.setStatus(SpaceStatus.AVAILABLE) -> sai ten -> KHONG COMPILE, phat hien ngay

DANH SACH VA Y NGHIA

SpaceStatus
  AVAILABLE    Ban/phong trong, khach co the dat hoac vao thang
  BOOKED       Da co nguoi dat va tra coc, cho khach den
  OCCUPIED     Khach da check-in, dang ngoi lam viec
  CLEANING     Khach vua ra, tap vu dang don dep, chua cho dat lai
  MAINTENANCE  Bao tri, huy bo phuc vu tam thoi

BookingStatus
  PENDING      Vua tao don, dang cho thanh toan coc (giu 10 phut)
  BOOKED       Da thanh toan coc thanh cong, cho ngay khach den
  ACTIVE       Khach da check-in, phien lam viec dang chay
  COMPLETED    Khach da checkout, don ket thuc binh thuong
  CANCELLED    Don bi huy: het 10 phut khong tra coc, hoac tu huy

SessionStatus
  ACTIVE       Phien dang chay (khach dang ngoi)
  COMPLETED    Phien da ket thuc (checkout xong)

UserRole
  ADMIN        Toan quyen: quan ly space, menu, bao cao, phan quyen
  RECEPTIONIST Le tan: check-in, goi mon, xuat hoa don, quan ly session
  CLEANER      Tap vu: chi xem danh sach ban can don, danh dau da don xong
  CUSTOMER     Khach hang: dat cho online, xem lich su, xem diem tich luy

PaymentMethod
  CASH         Tien mat tai quay le tan
  TRANSFER     Chuyen khoan ngan hang
  VNPAY        Thanh toan online qua cong VNPay

InvoiceLineType (cac dong trong 1 hoa don)
  SPACE        Tien thue khong gian: duration * pricePerHour / 60
  FNB          Tong tien do uong/an uong goi them
  EXTENSION    Phi gia han them gio
  DEPOSIT      Tru tien coc da nop truoc (so AM - giam tong)
  VOUCHER      Giam gia voucher hoac hang thanh vien (so AM - giam tong)
