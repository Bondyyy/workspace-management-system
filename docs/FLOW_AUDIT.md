# FLOW AUDIT

## Luồng Đã Rà Và Sửa

| Luồng | Kết quả |
|---|---|
| Nhân viên đăng nhập vào màn quản lý | Còn cần sửa tối thiểu trên `TrangChuQuanLyForm` bản gốc: không hardcode mở `TongQuanForm` mặc định nếu user không có quyền báo cáo. |
| Menu Báo cáo | Bản gốc đang ẩn/hiện theo quyền DB ở mức menu; chưa có guard action tập trung. |
| Tổng quan doanh thu | Đã bố trí lại filter/export để nút `Xuất CSV`, `Xuất PDF`, `Xuất PDF Jasper` không bị cụt chữ. |
| Web `/portal/history` | Đã trả đúng template `web/lich-su`; chưa login redirect `/dangNhap`; staff redirect `/staff/bookings`. |
| Web `/portal/benefits` | Đã trả đúng template `web/uu-dai`; xử lý chặn staff khỏi portal hội viên. |
| Web `/portal/account` | Đã trả đúng template `web/tai-khoan`; xử lý chặn staff khỏi portal hội viên. |
| Sinh mã web/quản lý | Đã dùng `MaTuDongUtil` cho đăng ký hội viên web, mã đặt chỗ/phiên/hóa đơn web và các màn quản lý chính: chi nhánh, phiếu giảm giá, không gian, loại không gian, dịch vụ, loại dịch vụ, người dùng, hội viên, nhân viên. |
| Hóa đơn PDF và QR | `HoaDonPDFExporter` không in QR chuyển khoản trên hóa đơn đã thanh toán. QR hiện thuộc luồng đặt chỗ/check-in/thanh toán trước. |

## Luồng Bị Chặn Theo Quyền

- Hội viên không vào màn quản lý Swing.
- Nhân viên/lễ tân không nên xem Tổng quan/Báo cáo doanh thu; cần test và patch tối thiểu trên bản gốc.
- Nhân viên/lễ tân không vào portal hội viên; web chuyển về `/staff/bookings`.
- Quản lý được xem báo cáo và vận hành nhưng không mặc định có quản trị người dùng/vai trò nếu không có quyền.
- Admin có toàn bộ menu quản trị.

## Checklist Test Tay Trước Khi Demo

1. Admin đăng nhập: thấy Báo cáo, Người dùng, Vai trò.
2. Quản lý đăng nhập: thấy Báo cáo/Tổng quan và các màn vận hành.
3. Nhân viên đăng nhập: không thấy Báo cáo, màn đầu tiên không phải Tổng quan sau khi áp dụng patch tối thiểu.
4. Hội viên đăng nhập web: mở `/portal/history`, `/portal/benefits`, `/portal/account`.
5. Staff web mở `/portal/history`: được chuyển sang `/staff/bookings`.
6. Xuất doanh thu CSV/PDF/PDF Jasper.
7. Xuất hóa đơn PDF.
8. Chạy script Oracle và kiểm tra dữ liệu dashboard doanh thu.

## Chưa Xác Thực Trong Lượt Sửa Này

- Chưa chạy runtime với Oracle thật trong môi trường hiện tại.
- Chưa có UI automation Swing/web.
- Chưa đồng bộ toàn bộ trigger/procedure DB sinh mã phụ như chứng từ kho; các màn quản lý chính đã dùng `MaTuDongUtil`.
- Chưa áp dụng lại guard phân quyền tập trung cho `TrangChuQuanLyForm` vì UI đã được đưa về bản gốc.
