THU MUC: model/
================
Chua cac class dai dien du lieu - tuong ung voi cac bang trong Oracle.
Day la cac POJO (Plain Old Java Object) thuan tuy - KHONG CO bat ky
annotation nao (@Entity, @Table, @Column...).

TAI SAO KHONG CO ANNOTATION
============================
  Vi day la Java Core + JDBC, khong dung JPA/Hibernate.
  Model chi la "tui chua du lieu" - DAO tu map ResultSet vao Model.

QUY TAC VIET MODEL
==================
  - Co day du constructor (co tham so + khong tham so)
  - Co getter va setter cho tung field
  - Dat ten field theo camelCase: userId, fullName, pricePerHour
  - Dat ten bang Oracle tuong ung (USERS, SPACES, BOOKINGS...)

DANH SACH FILE VA BANG ORACLE TUONG UNG
========================================

  User.java           <- bang USERS
    Fields: userId, username, passwordHash, roleId, isActive, createdAt

  Employee.java       <- bang EMPLOYEES (mo rong tu User)
    Fields: employeeId, userId, fullName, phone, position, hireDate

  Customer.java       <- bang CUSTOMERS (mo rong tu User)
    Fields: customerId, userId, fullName, phone, email,
            loyaltyPoints, membershipTierId, joinDate

  MembershipTier.java <- bang MEMBERSHIP_TIERS
    Fields: tierId, name (BRONZE/SILVER/GOLD), minPoints, discountPercent

  Voucher.java        <- bang VOUCHERS
    Fields: voucherId, code, type (PERCENT/FIXED), value,
            maxDiscount, usageLimit, usedCount, expiredAt, isActive

  Branch.java         <- bang BRANCHES
    Fields: branchId, name, address, phone, openingHours, status

  Space.java          <- bang SPACES
    Fields: spaceId, branchId, name, type (SINGLE/GROUP/ROOM),
            capacity, pricePerHour, status (SpaceStatus enum)

  Booking.java        <- bang BOOKINGS
    Fields: bookingId, customerId, status (BookingStatus enum),
            channel (ONLINE/OFFLINE), createdAt, checkinTime, qrCode

  BookingDetail.java  <- bang BOOKING_DETAILS
    Fields: detailId, bookingId, spaceId, priceAtBooking
    LUU Y: priceAtBooking la snapshot gia luc dat - khong doi du gia sau thay doi

  Session.java        <- bang SESSIONS
    Fields: sessionId, bookingId, spaceId, checkinTime, checkoutTime,
            status (SessionStatus enum), totalDurationMinutes

  SessionExtension.java <- bang SESSION_EXTENSIONS
    Fields: extensionId, sessionId, extendedMinutes, extraCost, extendedAt

  MenuCategory.java   <- bang MENU_CATEGORIES
    Fields: categoryId, name, displayOrder, isActive

  MenuItem.java       <- bang MENU_ITEMS
    Fields: itemId, categoryId, name, price, isAvailable, imageUrl

  SessionOrder.java   <- bang SESSION_ORDERS
    Fields: orderId, sessionId, totalPrice, status, createdAt

  SessionOrderDetail.java <- bang SESSION_ORDER_DETAILS
    Fields: detailId, orderId, itemId, quantity, unitPrice, subtotal
    LUU Y: subtotal = quantity * unitPrice (tinh bang thuat toan delta)

  Invoice.java        <- bang INVOICES
    Fields: invoiceId, sessionId, subtotal, discountAmount, totalAmount,
            status, createdAt

  InvoiceLine.java    <- bang INVOICE_LINES
    Fields: lineId, invoiceId, type (SPACE/FNB/EXTENSION/DEPOSIT/VOUCHER),
            description, amount
    LUU Y: type DEPOSIT va VOUCHER co amount AM (tru tien)

  Payment.java        <- bang PAYMENTS
    Fields: paymentId, invoiceId, amount, method (CASH/TRANSFER),
            status (PENDING/SUCCESS/FAILED), transactionId, paidAt
