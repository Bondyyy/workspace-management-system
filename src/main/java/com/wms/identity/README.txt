MODULE    : identity
MUC DICH  : Quan ly Users, Roles, Employees, Customers
MAP ORACLE: USERS, ROLES, PERMISSIONS, EMPLOYEES, CUSTOMERS

FILES CAN TAO
=============

entity/User.java
  - Map bang USERS
  - Fields: id, username, password_hash, role_id, is_active, created_at

entity/Role.java
  - Map bang ROLES
  - Values: ADMIN, RECEPTIONIST, CLEANER, CUSTOMER

entity/Employee.java
  - Mo rong tu User (Table-Per-Type)
  - Fields: ho_ten, sdt, dia_chi, ca_lam_viec

entity/Customer.java
  - Mo rong tu User
  - Fields: ho_ten, sdt, loyalty_points, membership_tier_id, join_date

controller/UserController.java
  - GET    /api/users         : Danh sach users (chi ADMIN)
  - GET    /api/users/{id}    : Chi tiet 1 user
  - PUT    /api/users/{id}    : Cap nhat thong tin
  - DELETE /api/users/{id}    : Xoa user (chi ADMIN)

controller/CustomerController.java
  - GET /api/customers/{id}/points   : Xem diem tich luy + hang thanh vien
  - GET /api/customers/{id}/bookings : Lich su dat cho

LUU Y
=====
- Tat ca Entity phai extends BaseEntity (co created_at, updated_at)
- Mat khau dung BCryptPasswordEncoder, KHONG luu plaintext
- Customer.loyalty_points duoc cap nhat boi LoyaltyService sau thanh toan SUCCESS
