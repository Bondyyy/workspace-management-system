# ACTOR PERMISSION MATRIX

| Chức năng | Hội viên | Nhân viên/lễ tân | Quản lý | Admin |
|---|---:|---:|---:|---:|
| Portal hội viên | Có | Không | Không | Không |
| Phiên làm việc | Không | Có | Có | Có |
| Đặt chỗ trước | Có, qua portal/form hội viên | Có | Có | Có |
| Hóa đơn/thu ngân | Không | Có | Có | Có |
| Dịch vụ khách đặt | Không | Có | Có | Có |
| Hội viên/khách hàng | Xem/cập nhật tài khoản cá nhân | Có | Có | Có |
| Kho/dịch vụ | Không | Có nếu được phân công | Có | Có |
| Báo cáo/Tổng quan doanh thu | Không | Không | Có | Có |
| Chi nhánh/không gian | Xem để đặt chỗ | Không hoặc hạn chế | Có | Có |
| Nhân viên | Không | Không | Có | Có |
| Người dùng | Không | Không | Không mặc định | Có |
| Vai trò/chức năng | Không | Không | Không mặc định | Có |

Ghi chú: `TrangChuQuanLyForm` bản gốc đang dựa vào RBAC/menu visibility từ `NguoiDungDTO.hasChucNang/hasRole`. Cần patch tối thiểu để màn mặc định cũng tuân theo quyền, tránh nhân viên bị mở Báo cáo dù menu đã ẩn.
