CREATE TABLE Sessions (
    session_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER,       -- Có thể NULL nếu khách vãng lai chưa kịp đăng ký
    booking_id NUMBER,        -- Có thể NULL nếu là khách vãng lai
    space_id NUMBER NOT NULL,
    checkin_time TIMESTAMP NOT NULL,
    checkout_time TIMESTAMP,  -- Sẽ NULL cho đến khi khách check-out
    applied_hourly_rate NUMBER(15, 2), -- Giá áp dụng tại thời điểm mở phiên
    check_in_staff_id NUMBER,
    check_out_staff_id NUMBER,
    status VARCHAR2(20) DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);