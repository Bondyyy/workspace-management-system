# Procedure Deprecation Plan

Ngày lập: 2026-05-25

Không xóa file SQL, không `DROP PROCEDURE`, không `ALTER` procedure chỉ để deprecate trong lượt này. Mọi xóa thật cần xác nhận PO/Tech lead/DBA, kiểm tra caller ngoài repo và rollback rõ ràng.

| Procedure | File | Lý do deprecate | Code hiện tại thay thế | Rủi ro nếu xóa | Điều kiện để xóa thật | Rollback |
|----------|------|-----------------|------------------------|----------------|------------------------|----------|
| `pro_SinhMaQR` | `Database/04_procedures/Pro_PhatSinhMaQR.sql` | QR format không khớp Java, không tạo ảnh/token nhận chỗ | `MaQRUtil`, `CongThongTinService`, `QuanLyDatChoTruocDAO` | App/job ngoài repo có thể còn gọi | Xác nhận không có caller DB/app ngoài repo và có migration được DBA duyệt | Restore file SQL và chạy lại `CREATE OR REPLACE PROCEDURE pro_SinhMaQR` từ source |
| `SP_DangKy` | `Database/04_procedures/QuanLyNguoiDung/SP_DangKy.sql` | Không bao phủ flow đăng ký hiện tại: user + khách hàng + vai trò | `CongThongTinWebRepository.taoHoiVien`, `KhachHangDAO.insert` | App cũ có thể còn gọi đăng ký trực tiếp | PO/Tech lead xác nhận không còn luồng dùng SP này | Restore file SQL và chạy lại create procedure |
| `SP_XoaPhieuGiamGia` | `Database/04_procedures/QuanLyPGG/SP_XoaPhieuGiamGia.sql` | Hard delete trái với Java soft-disable | `PhieuGiamGiaDAO.xoa` cập nhật `TrangThai` | Mất chức năng xóa thật nếu có tool admin cũ | Xác nhận nghiệp vụ chỉ dùng soft-disable và không còn caller ngoài repo | Restore file SQL và chạy lại create procedure |
| `SP_CapNhatTrangThaiDichVu` | `Database/04_procedures/QuanLyLoaiDV/SP_CapNhatTrangThaiDichVu.sql` | Trùng một phần chức năng với cập nhật dịch vụ, đặt sai module, chưa có caller | `sp_CapNhatDichVu` qua `DichVuDAO` | Nếu có màn status-only ẩn sẽ bị vỡ | Search caller ngoài repo, xác nhận không có màn status-only | Restore file SQL và chạy lại create procedure |
| `SP_TraCuuDichVu` non-canonical | `Database/04_procedures/QuanLyLoaiDV/SP_TraCuuDichVu.sql` trước khi rename nguồn | Trùng tên schema-wide với procedure canonical, có nguy cơ ghi đè deploy | `Database/04_procedures/QuanLyDichVu/SP_TraCuuDichVu.sql`; nguồn non-canonical đã rename thành `SP_TraCuuDichVuTheoLoai` | Nếu DB hiện tại đang bị deploy nhầm bản non-canonical, đổi source chưa tự cleanup object cũ | DBA xác nhận object `SP_TraCuuDichVu` trong DB là bản canonical và không còn phụ thuộc vào bản cũ | Chạy lại file canonical `QuanLyDichVu/SP_TraCuuDichVu.sql`; nếu cần bản theo loại, chạy `SP_TraCuuDichVuTheoLoai.sql` |
| `sp_TraCuuDichVuDaDat` | `Database/04_procedures/QuanLyDichVu/SP_TraCuuDichVuDaDat.sql` | Chưa có caller Java; trước đây có lỗi alias `ctddv` | Query trực tiếp theo từng màn quản lý dịch vụ đặt | App/tool ngoài repo có thể gọi | Sau khi alias đã sửa, chỉ deprecate nếu DBA xác nhận không còn caller | Restore file SQL và chạy lại create procedure |

