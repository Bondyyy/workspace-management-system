-- 1. Bảng Bookings
CREATE TABLE Bookings (
    booking_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER,
    voucher_id NUMBER,  
    booking_code VARCHAR2(50) UNIQUE NOT NULL, 
    qr_code VARCHAR2(255) UNIQUE, 
    check_in_time TIMESTAMP,  -- Ghi nhận lúc khách quét mã nhận bàn
    check_out_time TIMESTAMP, -- Ghi nhận lúc khách thanh toán rời đi
    total_amount NUMBER(15, 2) DEFAULT 0,
    discount_amount NUMBER(15, 2) DEFAULT 0,
    deposit_amount NUMBER(15, 2) DEFAULT 0, -- Tiền khách đã cọc online
    final_amount NUMBER(15, 2) DEFAULT 0,
    payment_status VARCHAR2(20) DEFAULT 'UNPAID', 
    status VARCHAR2(20) DEFAULT 'PENDING',       
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng BookingDetails
CREATE TABLE BookingDetails (
    detail_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id NUMBER NOT NULL,
    space_id NUMBER NOT NULL,
    expected_start_time TIMESTAMP NOT NULL, -- Giờ bắt đầu dự kiến đặt trên web
    expected_end_time TIMESTAMP NOT NULL,   -- Giờ kết thúc dự kiến đặt trên web
    price_at_booking NUMBER(15, 2) NOT NULL, -- Giá tại thời điểm book
    note VARCHAR2(255) 
);

ALTER TABLE Bookings ADD CONSTRAINT chk_booking_status CHECK (status IN ('PENDING', 'BOOKED', 'ACTIVE', 'COMPLETED', 'CANCELLED'));
ALTER TABLE Bookings ADD CONSTRAINT chk_booking_payment_status CHECK (payment_status IN ('UNPAID', 'PARTIAL', 'PAID'));
ALTER TABLE BookingDetails ADD CONSTRAINT chk_bkdetail_expected_end_time CHECK (expected_end_time > expected_start_time);