-- 1. Bảng Sessions 
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

-- 2. Bảng SessionExtensions 
CREATE TABLE SessionExtensions (
    extension_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id NUMBER NOT NULL,
    extended_duration NUMBER NOT NULL, -- Thời gian gia hạn
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time TIMESTAMP NOT NULL, -- Giờ bắt đầu tính gia hạn
    end_time TIMESTAMP NOT NULL,   -- Giờ kết thúc phần gia hạn
    cost_incurred NUMBER(15, 2) DEFAULT 0, -- Phí phát sinh 
    payment_status VARCHAR2(20) DEFAULT 'UNPAID', -- UNPAID, PAID 
    request_channel VARCHAR2(50), -- APP hoặc RECEPTION 
    created_by NUMBER,            -- Lưu staff_id nếu lễ tân là người thao tác
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bảng SessionOrders
CREATE TABLE SessionOrders (
    order_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id NUMBER NOT NULL,
    staff_id NUMBER,              -- Nhân viên order hoặc nhận order
    order_status VARCHAR2(20) DEFAULT 'PENDING', -- PENDING, PREPARING, SERVED, CANCELLED
    total_price NUMBER(15, 2) DEFAULT 0,
    payment_status VARCHAR2(20) DEFAULT 'UNPAID', 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng SessionOrderDetails 
CREATE TABLE SessionOrderDetails (
    detail_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id NUMBER NOT NULL,
    product_id NUMBER NOT NULL,
    quantity NUMBER NOT NULL,
    unit_price NUMBER(15, 2) NOT NULL, -- Giá món ăn tại thời điểm order
    subtotal NUMBER(15, 2) NOT NULL,   -- quantity * unit_price
    note VARCHAR2(255)               
);

ALTER TABLE Sessions ADD CONSTRAINT chk_session_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'));
ALTER TABLE Sessions ADD CONSTRAINT chk_session_time CHECK (checkout_time >= checkin_time);
ALTER TABLE SessionExtensions ADD CONSTRAINT chk_ext_time CHECK (end_time > start_time);
ALTER TABLE SessionOrders ADD CONSTRAINT chk_order_status CHECK (order_status IN ('PENDING', 'PREPARING', 'SERVED', 'CANCELLED'));
ALTER TABLE SessionOrderDetails ADD CONSTRAINT chk_order_quantity CHECK (quantity > 0);