ALTER TABLE Sessions 
ADD CONSTRAINT chk_session_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'));
ALTER TABLE Sessions 
ADD CONSTRAINT chk_session_time CHECK (checkout_time >= checkin_time);
ALTER TABLE SessionExtensions 
ADD CONSTRAINT chk_ext_time CHECK (end_time > start_time);
ALTER TABLE SessionOrders 
ADD CONSTRAINT chk_order_status CHECK (order_status IN ('PENDING', 'PREPARING', 'SERVED', 'CANCELLED'));
ALTER TABLE SessionOrderDetails 
ADD CONSTRAINT chk_order_quantity CHECK (quantity > 0);
ALTER TABLE SessionDetails
ADD CONSTRAINT chk_sessiondetail_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'));
ALTER TABLE SessionDetails
ADD CONSTRAINT chk_sessiondetail_time CHECK (checkout_time >= checkin_time);


ALTER TABLE SessionDetails
ADD CONSTRAINT fk_sessiondetails_sessions
FOREIGN KEY (session_id) REFERENCES Sessions(session_id) ON DELETE CASCADE;
ALTER TABLE SessionDetails
ADD CONSTRAINT fk_sessiondetails_spaces
FOREIGN KEY (space_id) REFERENCES Spaces(space_id);
ALTER TABLE Sessions 
ADD CONSTRAINT fk_sessions_customers 
FOREIGN KEY (customer_id) REFERENCES Customers(customer_id);
ALTER TABLE Sessions 
ADD CONSTRAINT fk_sessions_bookings 
FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id);
ALTER TABLE Sessions 
ADD CONSTRAINT fk_sessions_staff_in 
FOREIGN KEY (check_in_staff_id) REFERENCES Employees(employee_id);
ALTER TABLE Sessions 
ADD CONSTRAINT fk_sessions_staff_out 
FOREIGN KEY (check_out_staff_id) REFERENCES Employees(employee_id);
ALTER TABLE SessionExtensions 
ADD CONSTRAINT fk_ext_sessions 
FOREIGN KEY (session_id) REFERENCES Sessions(session_id) ON DELETE CASCADE;
ALTER TABLE SessionExtensions 
ADD CONSTRAINT fk_ext_staff 
FOREIGN KEY (created_by) REFERENCES Employees(employee_id);
ALTER TABLE SessionOrders 
ADD CONSTRAINT fk_orders_sessions 
FOREIGN KEY (session_id) REFERENCES Sessions(session_id) ON DELETE CASCADE;
ALTER TABLE SessionOrders 
ADD CONSTRAINT fk_orders_staff 
FOREIGN KEY (staff_id) REFERENCES Employees(employee_id);
ALTER TABLE SessionOrderDetails 
ADD CONSTRAINT fk_orderdetails_orders 
FOREIGN KEY (order_id) REFERENCES SessionOrders(order_id) ON DELETE CASCADE;
ALTER TABLE SessionOrderDetails 
ADD CONSTRAINT fk_orderdetails_products 
FOREIGN KEY (product_id) REFERENCES Products(product_id);
