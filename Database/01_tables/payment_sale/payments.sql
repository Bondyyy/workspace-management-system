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
