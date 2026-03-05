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

-- 1. Bảng Invoices
CREATE TABLE Invoices (
    invoice_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER,             -- Có thể NULL nếu là khách vãng lai
    staff_id NUMBER,                -- Nhân viên xuất hóa đơn
    sub_total NUMBER(15, 2) DEFAULT 0,       -- Tổng tiền trước khi giảm giá
    discount_amount NUMBER(15, 2) DEFAULT 0, -- Tổng tiền được giảm (từ Membership/Voucher)
    total_amount NUMBER(15, 2) DEFAULT 0,    -- Tổng tiền cuối cùng phải khách trả
    status VARCHAR2(20) DEFAULT 'UNPAID',    -- UNPAID, PAID, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng InvoiceLines
CREATE TABLE InvoiceLines (
    line_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_id NUMBER NOT NULL,
    reference_type VARCHAR2(50) NOT NULL, -- 'BOOKING', 'SESSION', 'F&B_ORDER', 'EXTENSION'
    reference_id NUMBER NOT NULL,         -- Chứa ID tương ứng (booking_id, session_id, order_id...)
    description VARCHAR2(255) NOT NULL,
    unit_price NUMBER(15, 2) NOT NULL,
    quantity NUMBER NOT NULL,
    subtotal NUMBER(15, 2) NOT NULL,      -- = unit_price * quantity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE Invoices ADD CONSTRAINT chk_invoice_status CHECK (status IN ('UNPAID', 'PAID', 'CANCELLED'));
ALTER TABLE InvoiceLines ADD CONSTRAINT chk_reference_type CHECK (reference_type IN ('BOOKING', 'SESSION', 'F&B_ORDER', 'EXTENSION'));
-- Cái này là bổ sung để tránh nhọc nhằn logic cho bảng payment, tránh 1 bảng có 3 cái nullable
-- 2. Bảng Payments

CREATE TABLE Payments (
    invoice_id NUMBER NOT NULL ,
    payment_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_method VARCHAR2(50) NOT NULL, -- CASH, CREDIT_CARD, VNPAY, MOMO
    amount NUMBER(15, 2) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, REFUNDED
    transaction_reference VARCHAR2(255),   -- Mã giao dịch trả về từ cổng thanh toán
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR2(255)
);

-- Bảng MembershipTiers 
CREATE TABLE MembershipTiers (
    tier_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tier_name VARCHAR2(50) NOT NULL UNIQUE,  
    min_points NUMBER DEFAULT 0 NOT NULL,   
    discount_percent NUMBER(5, 2) DEFAULT 0, 
    description VARCHAR2(255),              
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE Payments ADD CONSTRAINT chk_payment_method CHECK (payment_method IN ('CASH', 'CREDIT_CARD', 'VNPAY', 'MOMO'));
ALTER TABLE Payments ADD CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'));
ALTER TABLE Payments ADD CONSTRAINT chk_payment_amount CHECK (amount > 0);
ALTER TABLE Vouchers ADD CONSTRAINT chk_voucher_type CHECK (discount_type IN ('PERCENT', 'FIXED'));
ALTER TABLE Vouchers ADD CONSTRAINT chk_voucher_value CHECK (discount_value > 0);