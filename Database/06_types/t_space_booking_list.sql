-- 1. Tạo đối tượng lưu trữ thông tin của 1 không gian khi book
CREATE OR REPLACE TYPE t_space_booking_rec AS OBJECT (
    space_id NUMBER,
    expected_start_time TIMESTAMP,
    expected_end_time TIMESTAMP,
    price_at_booking NUMBER,
    note VARCHAR2(255)
);
/

-- 2. Tạo một bảng (danh sách) chứa các đối tượng trên
CREATE OR REPLACE TYPE t_space_booking_list AS TABLE OF t_space_booking_rec;
/