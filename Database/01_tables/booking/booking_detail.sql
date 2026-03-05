CREATE TABLE BookingDetails (
    detail_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id NUMBER NOT NULL,
    space_id NUMBER NOT NULL,
    expected_start_time TIMESTAMP NOT NULL, -- Giờ bắt đầu dự kiến đặt trên web
    expected_end_time TIMESTAMP NOT NULL,   -- Giờ kết thúc dự kiến đặt trên web
    price_at_booking NUMBER(15, 2) NOT NULL, -- Giá tại thời điểm book
    note VARCHAR2(255),
    booking_channel VARCHAR2(20) DEFAULT 'ONLINE'
);