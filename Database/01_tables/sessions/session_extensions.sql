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