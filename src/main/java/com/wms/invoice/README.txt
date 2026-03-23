MODULE    : invoice
MUC DICH  : Tao hoa don tong hop cuoi phien - tinh chinh xac 100% (tieu chi SOW)
            Cong thuc: [Gio ngoi * Gia] + [F&B] + [Gia han] - [Coc] - [Voucher]
MAP ORACLE: INVOICES, INVOICE_LINES

FILES CAN TAO
=============

entity/Invoice.java
  - Fields: id, session_id, subtotal, discount_amount, total_amount, status, created_at

entity/InvoiceLine.java
  - Fields: id, invoice_id, type, description, amount
  - type: SPACE | FNB | EXTENSION | DEPOSIT | VOUCHER

controller/InvoiceController.java
  - GET  /api/invoices/session/{id}/preview  : Xem truoc hoa don (chua chot)
  - POST /api/invoices/session/{id}/finalize : Chot hoa don sau checkout
  - GET  /api/invoices/{id}                  : Chi tiet hoa don da tao

service/InvoiceService.java

  generateInvoice(sessionId) - TAO INVOICE_LINE THEO THU TU:
  ===========================================================
  1. LINE type=SPACE
     amount = total_duration_minutes * (price_at_booking / 60.0)
     description = "Thue khong gian X phut"

  2. LINE type=FNB
     amount = OrderService.getOrderTotal(sessionId)
     description = "Tong tien do uong & an uong"

  3. LINE type=EXTENSION
     amount = SUM(SessionExtension.extra_cost)
     description = "Phi gia han them"

  4. LINE type=DEPOSIT (so AM)
     amount = -deposit_amount (tru tien coc da nop)
     description = "Tru tien coc da thanh toan"

  5. LINE type=VOUCHER (so AM)
     amount = -DiscountService.calculateDiscount(...)
     description = "Giam gia voucher / hang thanh vien"

  total_amount = SUM tat ca InvoiceLine.amount

  LUU Y QUAN TRONG:
  - BAT BUOC viet Unit Test cho generateInvoice() - day la tieu chi cham diem SOW
  - Test case: khach ngoi qua 0h (sang ngay moi), test nhieu order FNB, test co/khong voucher
