MODULE    : loyalty
MUC DICH  : Quan ly hang thanh vien va ma giam gia (voucher)
            Tinh chiet khau da tang ket hop hang thanh vien + voucher
MAP ORACLE: MEMBERSHIP_TIERS, VOUCHERS

FILES CAN TAO
=============

entity/MembershipTier.java
  - Fields: id, name(BRONZE/SILVER/GOLD), min_points, discount_percent, benefits_description

entity/Voucher.java
  - Fields: id, code, type(PERCENT/FIXED), value, max_discount
            usage_limit, used_count, valid_from, expired_at, is_active

service/DiscountService.java
  - calculateDiscount(customerId, subtotal, voucherCode)
      Goi Oracle function fn_CalculateDiscountAmount da co san trong DB
      (ham nay da duoc tao trong folder Database/03_functions)
      Ket hop: chiet khau tu hang thanh vien + voucher
      Ap dung tran max_discount neu co
      Tra ve so tien giam gia cuoi cung

  - validateVoucher(code)
      Kiem tra voucher: is_active=true, expired_at > NOW(), used_count < usage_limit
      Throw InvalidVoucherException neu khong hop le
