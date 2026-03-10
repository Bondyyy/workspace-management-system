CREATE TABLE SessionDetails (
    detail_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id NUMBER NOT NULL,
    space_id NUMBER NOT NULL,
    checkin_time TIMESTAMP NOT NULL,
    checkout_time TIMESTAMP, -- Khách có thể trả 1 phòng trước, giữ lại phòng kia
    applied_hourly_rate NUMBER(15, 2) NOT NULL,
    status VARCHAR2(20) DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED
    note VARCHAR2(255)
);
