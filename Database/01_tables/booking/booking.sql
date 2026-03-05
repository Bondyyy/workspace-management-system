CREATE TABLE Bookings (
    booking_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER,
    voucher_id NUMBER,
    booking_code VARCHAR2(50) UNIQUE NOT NULL,
    qr_code VARCHAR2(255) UNIQUE,
    check_in_time TIMESTAMP,  -- Ghi nhận lúc khách quét mã nhận bàn
    check_out_time TIMESTAMP, -- Ghi nhận lúc khách thanh toán rời đi
    total_amount NUMBER(15, 2) DEFAULT 0, -- Tổng giá tiền trước khi áp dụng voucher
    discount_amount NUMBER(15, 2) DEFAULT 0, -- Số tiền giảm được từ voucher
    deposit_amount NUMBER(15, 2) DEFAULT 0, -- Tiền khách đã cọc online
    final_amount NUMBER(15, 2) DEFAULT 0, -- Số tiền khách thanh toán khi đặt online (final_amount = total_amount - discount_amount - deposit_amount)
    payment_status VARCHAR2(20) DEFAULT 'UNPAID',
    status VARCHAR2(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    booking_channel VARCHAR2(20) DEFAULT 'ONLINE',
    note VARCHAR2(255), -- Note khách vãng lai
    created_by_staff_id NUMBER
);
