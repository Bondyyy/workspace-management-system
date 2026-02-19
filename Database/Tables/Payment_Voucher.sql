-- 1. Bảng Vouchers
CREATE TABLE Vouchers (
    voucher_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    voucher_code VARCHAR2(50) UNIQUE NOT NULL,
    discount_type VARCHAR2(20) NOT NULL, 
    discount_value NUMBER(15, 2) NOT NULL,
    min_order_value NUMBER(15, 2) DEFAULT 0,
    max_discount NUMBER(15, 2),
    usage_limit NUMBER,        
    used_count NUMBER DEFAULT 0, 
    expiry_date DATE NOT NULL,
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng Payments 
CREATE TABLE Payments (
    payment_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id NUMBER, -- nếu đây là giao dịch cọc online
    session_id NUMBER, -- nếu đây là giao dịch thanh toán lúc check-out
    order_id NUMBER,   -- Nếu khách vãng lai chỉ mua ly nước mang đi, không ngồi lại
    payment_method VARCHAR2(50) NOT NULL, -- CASH, CREDIT_CARD, VNPAY, MOMO
    amount NUMBER(15, 2) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, REFUNDED
    transaction_reference VARCHAR2(255),   -- Mã giao dịch trả về từ cổng thanh toán
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR2(255)
);