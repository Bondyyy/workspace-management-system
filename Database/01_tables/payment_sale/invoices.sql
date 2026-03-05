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
