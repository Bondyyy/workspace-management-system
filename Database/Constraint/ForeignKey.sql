-- Identities group
ALTER TABLE RolePermissions ADD CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE CASCADE;
ALTER TABLE RolePermissions ADD CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES Permissions(permission_id) ON DELETE CASCADE;
ALTER TABLE Users ADD CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES Roles(role_id);
ALTER TABLE Customers ADD CONSTRAINT fk_customers_users FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;
ALTER TABLE Employees ADD CONSTRAINT fk_employees_users FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE;


-- Space Management
ALTER TABLE Branches ADD CONSTRAINT fk_branches_manager FOREIGN KEY (manager_id) REFERENCES Employees(employee_id) ON DELETE SET NULL;
ALTER TABLE Spaces ADD CONSTRAINT fk_spaces_branches FOREIGN KEY (branch_id) REFERENCES Branches(branch_id);
ALTER TABLE Spaces ADD CONSTRAINT fk_spaces_types FOREIGN KEY (type_id) REFERENCES SpaceTypes(type_id);

-- Booking Online
ALTER TABLE Bookings ADD CONSTRAINT fk_bookings_customers FOREIGN KEY (customer_id) REFERENCES Customers(customer_id);
ALTER TABLE Bookings ADD CONSTRAINT fk_bookings_vouchers FOREIGN KEY (voucher_id) REFERENCES Vouchers(voucher_id);
ALTER TABLE BookingDetails ADD CONSTRAINT fk_bkdetail_bookings FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE;
ALTER TABLE BookingDetails ADD CONSTRAINT fk_bkdetail_spaces FOREIGN KEY (space_id) REFERENCES Spaces(space_id);

-- Sessions
ALTER TABLE Sessions ADD CONSTRAINT fk_sessions_customers FOREIGN KEY (customer_id) REFERENCES Customers(customer_id);
ALTER TABLE Sessions ADD CONSTRAINT fk_sessions_bookings FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id);
ALTER TABLE Sessions ADD CONSTRAINT fk_sessions_spaces FOREIGN KEY (space_id) REFERENCES Spaces(space_id);
ALTER TABLE Sessions ADD CONSTRAINT fk_sessions_staff_in FOREIGN KEY (check_in_staff_id) REFERENCES Employees(employee_id);
ALTER TABLE Sessions ADD CONSTRAINT fk_sessions_staff_out FOREIGN KEY (check_out_staff_id) REFERENCES Employees(employee_id);

ALTER TABLE SessionExtensions ADD CONSTRAINT fk_ext_sessions FOREIGN KEY (session_id) REFERENCES Sessions(session_id) ON DELETE CASCADE;
ALTER TABLE SessionExtensions ADD CONSTRAINT fk_ext_staff FOREIGN KEY (created_by) REFERENCES Employees(employee_id);

ALTER TABLE SessionOrders ADD CONSTRAINT fk_orders_sessions FOREIGN KEY (session_id) REFERENCES Sessions(session_id) ON DELETE CASCADE;
ALTER TABLE SessionOrders ADD CONSTRAINT fk_orders_staff FOREIGN KEY (staff_id) REFERENCES Employees(employee_id);

ALTER TABLE SessionOrderDetails ADD CONSTRAINT fk_orderdetails_orders FOREIGN KEY (order_id) REFERENCES SessionOrders(order_id) ON DELETE CASCADE;
ALTER TABLE SessionOrderDetails ADD CONSTRAINT fk_orderdetails_products FOREIGN KEY (product_id) REFERENCES Products(product_id);


-- Payments & Vouchers
ALTER TABLE Payments ADD CONSTRAINT fk_payments_bookings FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id);
ALTER TABLE Payments ADD CONSTRAINT fk_payments_sessions FOREIGN KEY (session_id) REFERENCES Sessions(session_id);
ALTER TABLE Payments ADD CONSTRAINT fk_payments_orders FOREIGN KEY (order_id) REFERENCES SessionOrders(order_id);

-- Product
ALTER TABLE Products ADD CONSTRAINT fk_products_categories FOREIGN KEY (category_id) REFERENCES Categories(category_id);