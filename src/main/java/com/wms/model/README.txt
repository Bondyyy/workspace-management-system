THU MUC: model/
================
Chua cac POJO (Plain Old Java Object) - dai dien cho bang du lieu Oracle.
Day la JAVA CORE THUAN TUY: chi co field, constructor, getter, setter.
KHONG CO bat ky annotation nao (@Entity, @Table, @Column...)

TAI SAO KHONG ANNOTATION?
  Vi day la Java Core + JDBC, KHONG dung JPA/Hibernate.
  Model chi la 'tui chua du lieu' - DAO tu viet SQL va map ResultSet vao Model.
  -> Dat su hoan toan kiem soat SQL, phu hop Java Core

QUY TAC VIET MODEL
==================
  - Co 2 constructor: khong tham so + day du tham so
  - Co getter/setter cho tung field (hoac dung Lombok neu cho phep)
  - Ten field: camelCase (userId, pricePerHour)
  - Ten bang Oracle tuong ung: VIET_HOA (USER_ID, PRICE_PER_HOUR)
  - Tat ca Model extends BaseModel (co createdAt, updatedAt)

DANH SACH FILE VA BANG ORACLE TUONG UNG
=========================================
  BaseModel.java          <- Abstract - co createdAt, updatedAt (dung chung)

  [IDENTITY - Dinh danh]
  User.java               <- Bang USERS       (userId, username, passwordHash, roleId)
  Employee.java           <- Bang EMPLOYEES   (employeeId, userId, fullName, phone)
  Customer.java           <- Bang CUSTOMERS   (customerId, loyaltyPoints, membershipTierId)
  MembershipTier.java     <- Bang MEMBERSHIP_TIERS (tierId, name, minPoints, discountPercent)
  Voucher.java            <- Bang VOUCHERS    (code, type, value, maxDiscount, usedCount)

  [SPACE - Khong gian]
  Branch.java             <- Bang BRANCHES    (branchId, name, address)
  Space.java              <- Bang SPACES      (spaceId, branchId, pricePerHour, status)

  [BOOKING - Dat cho]
  Booking.java            <- Bang BOOKINGS    (bookingId, status, channel, qrCode)
  BookingDetail.java      <- Bang BOOKING_DETAILS (priceAtBooking <- snapshot gia)

  [SESSION - Phien lam viec]
  Session.java            <- Bang SESSIONS    (checkinTime, checkoutTime, totalDuration)
  SessionExtension.java   <- Bang SESSION_EXTENSIONS (extendedMinutes, extraCost)

  [FNB - Do uong an uong]
  MenuCategory.java       <- Bang MENU_CATEGORIES
  MenuItem.java           <- Bang MENU_ITEMS  (price, isAvailable)
  SessionOrder.java       <- Bang SESSION_ORDERS (totalPrice)
  SessionOrderDetail.java <- Bang SESSION_ORDER_DETAILS (qty, unitPrice, subtotal)

  [INVOICE - Hoa don]
  Invoice.java            <- Bang INVOICES    (subtotal, discountAmount, totalAmount)
  InvoiceLine.java        <- Bang INVOICE_LINES (type, amount - co the la so AM)

  [PAYMENT - Thanh toan]
  Payment.java            <- Bang PAYMENTS    (method, status, transactionId)

  [ENUMS - Gia tri co dinh]
  enums/SpaceStatus.java        AVAILABLE | BOOKED | OCCUPIED | CLEANING | MAINTENANCE
  enums/BookingStatus.java      PENDING | BOOKED | ACTIVE | COMPLETED | CANCELLED
  enums/SessionStatus.java      ACTIVE | COMPLETED
  enums/UserRole.java           ADMIN | RECEPTIONIST | CLEANER | CUSTOMER
  enums/PaymentMethod.java      CASH | TRANSFER | VNPAY
  enums/InvoiceLineType.java    SPACE | FNB | EXTENSION | DEPOSIT | VOUCHER
