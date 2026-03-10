CREATE TABLE Sessions (
    session_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER,       -- Có thể NULL nếu khách vãng lai chưa kịp đăng ký
    booking_id NUMBER,        -- Có thể NULL nếu chưa đặt trước
    checkin_time TIMESTAMP NOT NULL,
    checkout_time TIMESTAMP,  -- Sẽ NULL cho đến khi khách check-out
    check_in_staff_id NUMBER,
    check_out_staff_id NUMBER,
    status VARCHAR2(20) DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


