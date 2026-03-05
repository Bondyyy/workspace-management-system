-- Booking Constraints 
ALTER TABLE Bookings 
ADD CONSTRAINT chk_booking_status CHECK (status IN ('PENDING', 'BOOKED', 'ACTIVE', 'COMPLETED', 'CANCELLED'));
-- PENDING (Đang chờ): Đơn đặt chỗ vừa được tạo ra nhưng chưa hoàn tất thủ tục
-- BOOKED (Đã đặt trước): Giao dịch đặt chỗ và đặt cọc đã thành công, hệ thống cấp mã qr_code
-- ACTIVE (Đang hoạt động): Khách đã mang mã QR đến quầy để Check-in
-- COMPLETED (Đã hoàn tất): Khách hàng đã Check-out
-- CANCELLED (Đã hủy): Đơn đặt chỗ bị hủy do khách hàng không đến

ALTER TABLE Bookings 
ADD CONSTRAINT chk_booking_payment_status CHECK (payment_status IN ('UNPAID', 'PARTIAL', 'PAID'));
ALTER TABLE Bookings 
ADD CONSTRAINT chk_booking_channel CHECK (booking_channel IN ('ONLINE', 'OFFLINE'));
--BookingDetails Constraints
ALTER TABLE BookingDetails 
ADD CONSTRAINT chk_bkdetail_expected_end_time CHECK (expected_end_time > expected_start_time);

-- Booking Foreign Keys
ALTER TABLE Bookings 
ADD CONSTRAINT fk_bookings_customers 
FOREIGN KEY (customer_id) REFERENCES Customers(customer_id);
ALTER TABLE Bookings 
ADD CONSTRAINT fk_bookings_vouchers 
FOREIGN KEY (voucher_id) REFERENCES Vouchers(voucher_id);
ALTER TABLE Bookings 
ADD CONSTRAINT fk_bookings_staff 
FOREIGN KEY (created_by_staff_id) REFERENCES Employees(employee_id);
--BookingDetails Foreign Keys
ALTER TABLE BookingDetails 
ADD CONSTRAINT fk_bkdetail_bookings 
FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE;
ALTER TABLE BookingDetails 
ADD CONSTRAINT fk_bkdetail_spaces 
FOREIGN KEY (space_id) REFERENCES Spaces(space_id);

