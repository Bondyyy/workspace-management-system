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