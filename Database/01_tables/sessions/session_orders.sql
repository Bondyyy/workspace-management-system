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