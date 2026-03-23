MODULE    : payment
MUC DICH  : Tich hop VNPay xu ly tien coc dat cho online
            Cong diem loyalty sau thanh toan SUCCESS
MAP ORACLE: PAYMENTS

FILES CAN TAO
=============

entity/Payment.java
  - Fields: id, invoice_id, amount, method(VNPAY/CASH/TRANSFER)
            status(PENDING/SUCCESS/FAILED), transaction_id, paid_at

controller/PaymentController.java
  - POST /api/payments/initiate
    Tao Payment PENDING
    Goi VnPayService.createPaymentUrl()
    Tra ve { paymentUrl } de FE redirect khach hang sang VNPay

controller/PaymentCallbackController.java
  - GET /api/payments/callback
    VNPay goi ve sau khi khach thanh toan xong
    Kiem tra chu ky checksum de chong gia mao
    Neu vnp_ResponseCode = "00" (SUCCESS):
      -> Payment.status = SUCCESS
      -> Booking.status = BOOKED
      -> Goi LoyaltyService.addPoints()
    Neu FAILED:
      -> Payment.status = FAILED

service/VnPayService.java
  - createPaymentUrl(amount, orderId, returnUrl)
      Tao URL chua cac param: vnp_Amount, vnp_TxnRef, vnp_OrderInfo
      Ky HMAC-SHA512 bang vnp_HashSecret
      Tra ve URL de redirect

  - validateCallback(params)
      Lay tat ca params ngoai tru vnp_SecureHash
      Tinh lai HMAC-SHA512
      So sanh voi vnp_SecureHash trong params
      -> Neu khac: la gia mao, throw SecurityException

  - refund(transactionId, amount)
      Goi VNPay Refund API neu khach huy sau khi da thanh toan

service/LoyaltyService.java
  - addPoints(customerId, amount)
      points = floor(amount / 1000)
      UPDATE customers SET loyalty_points += points

  - checkAndUpgradeTier(customerId)
      SELECT * FROM membership_tiers ORDER BY min_points DESC
      Neu tong diem >= tier.min_points -> nang hang

  - redeemPoints(customerId, pointsToRedeem)
      Kiem tra du diem khong
      Tru diem, tra ve so tien giam gia tuong ung

LUU Y BAOMAT (SOW muc 7.4):
  Khong luu so the tin dung tren server
  Chi luu transaction_id do VNPay cap
