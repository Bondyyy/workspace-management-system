CREATE TABLE SessionOrderDetails (
    detail_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id NUMBER NOT NULL,
    product_id NUMBER NOT NULL,
    quantity NUMBER NOT NULL,
    unit_price NUMBER(15, 2) NOT NULL, -- Giá món ăn tại thời điểm order
    subtotal NUMBER(15, 2) NOT NULL,   -- quantity * unit_price
    note VARCHAR2(255)               
);