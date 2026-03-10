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

ALTER TABLE Vouchers
ADD effective_date DATE NOT NULL;