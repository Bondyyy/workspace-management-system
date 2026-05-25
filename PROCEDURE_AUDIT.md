# Báo Cáo Rà Soát Stored Procedure

Ngày rà soát: 2026-05-25
Branch hiện tại: `Dung_Fix`
Phạm vi: `Database/04_procedures` đối chiếu với Java DAO/service/controller hiện tại.

## Nguyên Tắc

- Không xóa procedure.
- Không sửa procedure.
- Không chuyển Java sang procedure nếu chưa chứng minh output và trạng thái giống 100%.
- Không đổi công thức tiền, trạng thái đặt chỗ, thanh toán, QR, hóa đơn, phiên làm việc.
- Các procedure có commit/rollback nội bộ được đánh dấu để cân nhắc transaction boundary trước khi dùng lại ở flow khác.

## Tổng Quan

- Tổng số file procedure rà soát: 35.
- Procedure đang được Java gọi bằng named `CallableStatement`: 12 procedure name.
- `CallableStatement` dạng anonymous PL/SQL có tồn tại ở Java, nhưng không phải named stored procedure: `DatChoDAO`, `CongThongTinWebRepository`, `KhachHangDAO`, `NhanVienDAO`, `NguoiDungDAO`, `VaiTroDAO`.
- Không có chuyển đổi Java sang procedure trong lượt này vì chưa có procedure chưa dùng nào đạt điều kiện tương đương 100% với flow hiện tại.
- Phát hiện quan trọng đã xử lý ở lượt 2026-05-25: nguồn canonical `SP_TraCuuDichVu` nằm ở `QuanLyDichVu`; nguồn không canonical trong `QuanLyLoaiDV` đã rename thành `SP_TraCuuDichVuTheoLoai` để tránh deploy ghi đè schema-wide.

## Bảng Phân Loại

| Procedure | File | Đang được Java gọi ở đâu | DAO/Service liên quan | Phân loại A/B/C/D/E | Hành động đề xuất | Có sửa code không | Lý do |
|---|---|---|---|---|---|---|---|
| `SP_BaoCaoDoanhThu` | `Database/04_procedures/SP_BaoCaoDoanhThu.sql` | Chưa gọi | `ThongKeDAO`, `TongQuanService` đang query trực tiếp | C | Chỉ dùng sau khi đồng bộ công thức và bộ lọc với dashboard/report hiện tại | Không | Procedure trả tổng hợp, còn Java cần nhiều dataset chi tiết, filter linh hoạt; công thức procedure cộng `DaTraTruoc` khác một số query hiện tại. |
| `SP_ThanhToanVoiPhieuGiamGia` | `Database/04_procedures/sp_ThanhToanVoiPGG.sql` | `HoaDonDAO.thanhToanVoiPhieuGiamGia`, `HoaDonDAO.thanhToanVoiPhieuGiamGiaMoi` | `HoaDonDAO`, `ThanhToanService`, tests thanh toán | A | Giữ nguyên; không tái dùng ở flow khác nếu chưa test transaction | Không | Đang xử lý atomic thanh toán, khóa `PHIENLAMVIEC/HOADON/PHIEUGIAMGIA`, cập nhật hóa đơn, voucher, trạng thái không gian. |
| `pro_SinhMaQR` | `Database/04_procedures/Pro_PhatSinhMaQR.sql` | Chưa gọi | `MaQRUtil`, `CongThongTinService`, `QuanLyDatChoTruocDAO`, `DatChoDAO` | E | Đề xuất deprecate sau khi xác nhận không có job/app ngoài Java dùng | Không | Java đang tạo QR có token/format riêng và ảnh PNG; procedure chỉ ghép `MaDatCho_timestamp`, không tương đương. |
| `SP_KetThucPhien` | `Database/04_procedures/HoaDon/SP_KetThucPhien.sql` | `PhienLamViecDAO.tuDongKetThucPhienQuaHanDatCho`; tests compile SP | `PhienLamViecDAO`, `HoaDonDAO` | A | Giữ cho scheduler auto-end; không thay manual `ketThucPhien` sang SP nếu chưa regression tiền/phụ thu | Không | Procedure đúng cho auto-end đặt chỗ, có lock và chốt tiền; manual Java path hiện có cách tính/cập nhật khác nên chuyển ngay có rủi ro đổi kết quả. |
| `SP_XemChiTietHoaDon` | `Database/04_procedures/HoaDon/SP_XemChiTietHoaDon.sql` | Chưa gọi | `HoaDonDAO.layThongTinChiTietHoaDon`, `ThanhToanService` | C | Đề xuất thêm wrapper theo `MaHoaDon` hoặc chỉnh output sau khi test hóa đơn preview/export | Không | Procedure nhận `MaPhien`, yêu cầu phiên đã kết thúc và trả 3 cursor; Java hiện nhận `MaHoaDon`, hỗ trợ DTO/preview riêng. |
| `SP_ThemChiNhanh` | `Database/04_procedures/QuanLyChiNhanh/SP_ThemChiNhanh.sql` | `ChiNhanhDAO.themChiNhanh` | `ChiNhanhDAO` | A | Giữ nguyên | Không | Java đang gọi đúng tham số, procedure validate trùng mã/tên và insert chi nhánh. |
| `SP_CapNhatChiNhanh` | `Database/04_procedures/QuanLyChiNhanh/SP_CapNhatChiNhanh.sql` | `ChiNhanhDAO.capNhatChiNhanh` | `ChiNhanhDAO` | A | Giữ nguyên | Không | Java đang gọi procedure; logic cập nhật không thấy lệch flow. |
| `SP_TraCuuChiNhanh` | `Database/04_procedures/QuanLyChiNhanh/SP_TraCuuChiNhanh.sql` | `ChiNhanhDAO.timKiemChiNhanh` | `ChiNhanhDAO` | A | Giữ nguyên | Không | Procedure trả cursor phù hợp danh sách/tìm kiếm chi nhánh. |
| `SP_VoHieuHoaChiNhanh` | `Database/04_procedures/QuanLyChiNhanh/SP_VoHieuHoaChiNhanh.sql` | `ChiNhanhDAO.voHieuHoaChiNhanh` | `ChiNhanhDAO` | C | Sửa message hoặc bổ sung logic vô hiệu hóa không gian sau khi xác nhận nghiệp vụ | Không | Procedure chỉ update `CHINHANH.TrangThai`, nhưng message nói toàn bộ không gian thuộc chi nhánh đã bị vô hiệu hóa. |
| `sp_ThemDichVu` | `Database/04_procedures/QuanLyDichVu/SP_ThemDichVu.sql` | `DichVuDAO.themDichVu` | `DichVuDAO` | A | Giữ nguyên; nên map `outMessage` thân thiện ở service/UI khi có thời gian | Không | Java đang gọi đúng tham số; procedure insert dịch vụ và validate loại dịch vụ. |
| `sp_CapNhatDichVu` | `Database/04_procedures/QuanLyDichVu/SP_CapNhatDichVu.sql` | `DichVuDAO.capNhatDichVu` | `DichVuDAO` | A | Giữ nguyên; cần test case đơn giá bằng 0 nếu UI đang cho phép | Không | Procedure đang được dùng; cập nhật đủ field dịch vụ đang có trong DTO. |
| `SP_TraCuuDichVu` | `Database/04_procedures/QuanLyDichVu/SP_TraCuuDichVu.sql` | `DichVuDAO.layDanhSachDichVu` | `DichVuDAO` | A | Giữ làm canonical cho danh sách dịch vụ | Không | Java đang gọi tên này và map output hiện tại; nguồn trùng tên ở `QuanLyLoaiDV` đã rename thành `SP_TraCuuDichVuTheoLoai`. |
| `sp_ThemChiTietDichVu` | `Database/04_procedures/QuanLyDichVu/SP_ThemChiTietDichVu.sql` | Chưa gọi | DAO thêm dịch vụ vào phiên hiện dùng logic Java/direct SQL | C | Chỉ dùng sau khi bổ sung logic đặc biệt cho dịch vụ gia hạn giờ | Không | Procedure chỉ upsert `CHITIETDICHVU`; flow Java có xử lý riêng cho dịch vụ gia hạn và cập nhật thời gian dự kiến kết thúc. |
| `sp_TraCuuDichVuDaDat` | `Database/04_procedures/QuanLyDichVu/SP_TraCuuDichVuDaDat.sql` | Chưa gọi | Tra cứu dịch vụ đã dùng hiện không dùng procedure này | D | Đã sửa alias compile; vẫn chỉ dùng/deprecate sau xác nhận caller ngoài repo | Không | Alias `ctddv.SoLuong` đã sửa thành `ctdv.SoLuong`; chưa có caller Java nên chưa đổi nghiệp vụ. |
| `SP_LayDanhSachKho` | `Database/04_procedures/QuanLyKho/SP_LayDanhSachKho.sql` | `QuanLyKhoDao.layDanhSachKho` | `QuanLyKhoDao` | A | Giữ nguyên | Không | Procedure trả cursor đúng dữ liệu kho/dịch vụ hiện DAO map. |
| `SP_NhapKhoDichVu` | `Database/04_procedures/QuanLyKho/SP_NhapKhoDichVu.sql` | `QuanLyKhoDao.nhapKhoDichVu` | `QuanLyKhoDao`, report nhập kho | C | Giữ tạm; đề xuất refactor sau sang mã nhân viên/mã loại/mã dịch vụ ổn định | Không | Procedure đang dùng và atomic, nhưng dùng `ROWNUM = 1` theo tên nhân viên/loại/dịch vụ, có thể che dữ liệu trùng. |
| `SP_ThemKhongGian` | `Database/04_procedures/QuanLyKhongGian/SP_ThemKhongGian.sql` | Chưa gọi | `KhongGianDAO.them` đang insert trực tiếp | C | Không chuyển; nếu muốn dùng cần thêm tham số trạng thái hoặc xác nhận luôn tạo `Trống` | Không | Procedure hardcode `TrangThaiKG = 'Trống'` và trả QR text không được lưu; Java hiện nhận trạng thái từ DTO. |
| `SP_CapNhatKhongGian` | `Database/04_procedures/QuanLyKhongGian/SP_CapNhatKhongGian.sql` | Chưa gọi | `KhongGianDAO.capNhat` đang update trực tiếp | B | Ứng viên chuyển sau test CRUD và mapping lỗi UI | Không | Tham số gần khớp Java update và có validate trùng tên/loại, nhưng đổi cơ chế lỗi và commit nội bộ nên chưa chuyển nóng. |
| `SP_TraCuuKhongGian` | `Database/04_procedures/QuanLyKhongGian/SP_TraCuuKhongGian.sql` | Chưa gọi | `KhongGianDAO.timKiem`, `XemSoDoKhongGianForm` | C | Bổ sung `TrangThaiLoaiKG`/order tương đương nếu muốn thay query Java | Không | Procedure thiếu field Java đang map (`TrangThaiLoaiKG`) và order khác `KhongGianDAO`. |
| `SP_VoHieuHoaKhongGian` | `Database/04_procedures/QuanLyKhongGian/SP_VoHieuHoaKhongGian.sql` | Chưa gọi | `KhongGianDAO.xoa` đang delete trực tiếp | C | Không chuyển khi chưa đổi nghiệp vụ delete sang bảo trì | Không | Procedure là soft-disable sang `Bảo trì`; Java hiện xóa hard delete. Chuyển sẽ đổi behavior. |
| `SP_ThemLoaiDichVu` | `Database/04_procedures/QuanLyLoaiDV/SP_ThemLoaiDichVu.sql` | Chưa gọi | `LoaiDichVuDAO.them` đang insert trực tiếp | B | Ứng viên chuyển sau test validation/message | Không | Procedure có validation rõ hơn và tham số khớp; cần đảm bảo UI đang không phụ thuộc lỗi DB trực tiếp. |
| `SP_CapNhatLoaiDichVu` | `Database/04_procedures/QuanLyLoaiDV/SP_CapNhatLoaiDichVu.sql` | Chưa gọi | `LoaiDichVuDAO.capNhat` đang update trực tiếp | C | Không chuyển tự động | Không | Procedure chặn ngưng loại nếu còn dịch vụ hoạt động; Java hiện không chặn. Chuyển sẽ đổi nghiệp vụ/trạng thái. |
| `SP_CapNhatTrangThaiDichVu` | `Database/04_procedures/QuanLyLoaiDV/SP_CapNhatTrangThaiDichVu.sql` | Chưa gọi | `DichVuDAO.capNhatDichVu` đã cập nhật full thông tin | E | Deprecate nếu không có màn status-only; hoặc đổi tên/đưa về module `QuanLyDichVu` | Không | Chức năng trùng một phần với `sp_CapNhatDichVu`, đặt sai thư mục module, chưa có caller. |
| `SP_TraCuuDichVuTheoLoai` | `Database/04_procedures/QuanLyLoaiDV/SP_TraCuuDichVuTheoLoai.sql` | Chưa gọi | Chưa có DAO/service Java phụ thuộc | E | Chỉ dùng sau khi có caller riêng và test output; có trong deprecation plan cho bản non-canonical cũ | Không | Đã rename từ nguồn trùng `SP_TraCuuDichVu` để tránh overwrite; output/order khác procedure canonical. |
| `SP_DangKy` | `Database/04_procedures/QuanLyNguoiDung/SP_DangKy.sql` | Chưa gọi | `CongThongTinWebRepository.taoHoiVien`, `KhachHangDAO.insert`, service đăng ký | D | Deprecate hoặc viết lại theo flow đăng ký hiện tại | Không | Procedure chỉ tạo `NGUOIDUNG`, không tạo `KHACHHANG`, vai trò, họ tên; không khớp flow đăng ký hiện tại. |
| `SP_ThemKhachHang` | `Database/04_procedures/QuanLyNguoiDung/SP_ThemKhachHang.sql` | Chưa gọi | `KhachHangDAO.insert` dùng transaction tạo user + khách hàng | C | Chỉ dùng nếu bỏ commit nội bộ hoặc bọc lại trong procedure đầy đủ | Không | Procedure yêu cầu `MaND` có sẵn và commit riêng; không atomic với tạo `NGUOIDUNG`/vai trò như Java. |
| `SP_CapNhatKhachHang` | `Database/04_procedures/QuanLyNguoiDung/SP_CapNhatKhachHang.sql` | Chưa gọi | `KhachHangDAO.update` | C | Bổ sung đủ field nếu muốn dùng | Không | Procedure chỉ cập nhật họ tên và hạng; Java cập nhật SDT, email, ngày sinh, giới tính, ảnh, trạng thái. |
| `SP_TraCuuHoiVien` | `Database/04_procedures/QuanLyNguoiDung/SP_TraCuuHoiVien.sql` | Chưa gọi | `KhachHangDAO.getAll/search/timTheoSdt` | C | Không chuyển; cần bổ sung field/filter loại trừ admin/nhân viên | Không | Procedure thiếu `MaND`, ngày sinh, giới tính, ảnh và filter loại trừ account admin/nhân viên như Java. |
| `SP_XoaKhachHang` | `Database/04_procedures/QuanLyNguoiDung/SP_XoaKhachHang.sql` | Chưa gọi | `KhachHangDAO.delete` | C | Không chuyển; cần thống nhất xóa hard/soft và role/user cleanup | Không | Procedure chỉ xóa `KHACHHANG`, giữ `NGUOIDUNG`; Java xóa role, khách hàng, người dùng trong transaction. |
| `SP_ThemPhieuGiamGia` | `Database/04_procedures/QuanLyPGG/SP_ThemPhieuGiamGia.sql` | Chưa gọi | `PhieuGiamGiaDAO.themMoi` | C | Không chuyển; bổ sung `TrangThai` và mapping message trước | Không | Java insert có `TrangThai`; procedure không có tham số trạng thái và commit nội bộ. |
| `SP_CapNhatPhieuGiamGia` | `Database/04_procedures/QuanLyPGG/SP_CapNhatPhieuGiamGia.sql` | Chưa gọi | `PhieuGiamGiaDAO.capNhat` | C | Không chuyển; cần bổ sung cập nhật `MaChuSoPGG` và `TrangThai` nếu đúng nghiệp vụ | Không | Java cập nhật mã chữ số và trạng thái; procedure không cập nhật hai field này. |
| `SP_XoaPhieuGiamGia` | `Database/04_procedures/QuanLyPGG/SP_XoaPhieuGiamGia.sql` | Chưa gọi | `PhieuGiamGiaDAO.xoa` | D | Deprecate hoặc đổi procedure thành soft-disable sau xác nhận | Không | Procedure `DELETE` thật; Java hiện chỉ set `TrangThai = 'Đã vô hiệu hoá'`. Dùng procedure sẽ đổi retention/history behavior. |
| `sp_MoPhienLamViecTrucTiep` | `Database/04_procedures/QuanLyPhien/SP_MoPhienLamViecTrucTiep.sql` | `PhienLamViecDAO.taoPhienLamViecMoi`; test business hours | `PhienLamViecDAO`, `PhienLamViecService` | A | Giữ nguyên cho mở phiên trực tiếp | Không | Procedure đang dùng cho mở phiên trực tiếp, có lock không gian và kiểm tra giờ hoạt động. |
| `sp_TraCuuPhienLamViec` | `Database/04_procedures/QuanLyPhien/SP_TraCuuPhienLamViec.sql` | Chưa gọi | `PhienLamViecDAO.layDanhSachPhien` query trực tiếp | C | Không chuyển; mở rộng filter nếu muốn dùng | Không | Procedure tra cứu đúng một `MaPhien`; Java cần list theo keyword/chi nhánh và thêm trạng thái thanh toán/đặt chỗ/đơn giá. |
| `sp_XemChiTietPhienLamViec` | `Database/04_procedures/QuanLyPhien/SP_XemChiTietPhienLamViec.sql` | Chưa gọi | `PhienLamViecDAO.layDichVuCuaPhien`, detail/confirm DTO | C | Có thể dùng cho màn detail sau khi map đủ DTO và message | Không | Procedure trả 2 cursor, nhưng Java hiện dùng nhiều DTO/query riêng cho xác nhận đặt chỗ, hóa đơn và dịch vụ. |

## Procedure Đang Được Gọi Tốt

- `SP_ThanhToanVoiPhieuGiamGia`
- `SP_KetThucPhien` trong scheduler auto-end
- `SP_ThemChiNhanh`
- `SP_CapNhatChiNhanh`
- `SP_TraCuuChiNhanh`
- `sp_ThemDichVu`
- `sp_CapNhatDichVu`
- `SP_LayDanhSachKho`
- `sp_MoPhienLamViecTrucTiep`

Các procedure này vẫn cần hardening thông báo lỗi ở tầng UI/service nếu đang đưa `SQLERRM` ra ngoài, nhưng không cần đổi nghiệp vụ.

## Procedure Có Thể Cân Nhắc Chuyển Sau Test

- `SP_CapNhatKhongGian`: gần khớp `KhongGianDAO.capNhat`, nhưng cần test duplicate/type validation và message mapping.
- `SP_ThemLoaiDichVu`: gần khớp `LoaiDichVuDAO.them`, nhưng cần test trạng thái hợp lệ, lỗi trùng tên, và behavior UI.

Không chuyển trong lượt này vì user yêu cầu không thay đổi behavior nếu chưa chắc 100%.

## Procedure Nên Sửa Trước Khi Dùng

- `SP_XemChiTietHoaDon`: thêm overload/wrapper theo `MaHoaDon`, hoặc đổi Java mapping có test export hóa đơn.
- `SP_BaoCaoDoanhThu`: đồng bộ công thức với `ThongKeDAO`, đặc biệt `ThanhTien`, `DaTraTruoc`, filter trạng thái và filter loại doanh thu.
- `SP_VoHieuHoaChiNhanh`: sửa message hoặc thực sự cập nhật không gian nếu nghiệp vụ xác nhận.
- `SP_NhapKhoDichVu`: bỏ phụ thuộc tên + `ROWNUM = 1`; dùng `MaNV`, `MaLoaiDV`, `MaDV` ổn định.
- `SP_ThemKhongGian`: bổ sung `TrangThaiKG` hoặc xác nhận rule luôn tạo `Trống`; bỏ/chuẩn hóa QR output.
- `SP_TraCuuKhongGian`: trả đủ `TrangThaiLoaiKG` và order tương đương Java nếu muốn thay query.
- Nhóm `QuanLyNguoiDung`: cần viết lại theo transaction tạo/cập nhật/xóa người dùng + khách hàng + vai trò hiện tại.
- Nhóm `QuanLyPGG`: cần hỗ trợ `TrangThai`, `MaChuSoPGG` và soft-disable thay hard delete.

## Đề Xuất Deprecate/Xóa Sau Khi Xác Nhận

| Procedure | Lý do không dùng được | Code hiện tại thay thế | Rủi ro nếu xóa | Cần xác nhận |
|---|---|---|---|---|
| `pro_SinhMaQR` | QR format không khớp Java, không tạo ảnh/token nhận chỗ | `MaQRUtil`, `CongThongTinService`, `QuanLyDatChoTruocDAO` | App/DB job ngoài repo có thể còn gọi | Tech lead + DBA |
| Bản non-canonical cũ `QuanLyLoaiDV/SP_TraCuuDichVu` | Trùng tên procedure schema-wide; nguồn đã rename thành `SP_TraCuuDichVuTheoLoai` | `DichVuDAO.layDanhSachDichVu` đang dùng canonical `QuanLyDichVu/SP_TraCuuDichVu` | DB hiện có thể vẫn còn object deploy nhầm nếu chưa chạy lại canonical | Tech lead + DBA xác nhận object production |
| `sp_TraCuuDichVuDaDat` | Chưa có caller Java; alias lỗi đã sửa | Query trực tiếp theo nhu cầu từng màn | Có thể có tool ngoài repo gọi | Tech lead |
| `SP_XoaPhieuGiamGia` | Hard delete trái với Java soft-disable | `PhieuGiamGiaDAO.xoa` update `TrangThai` | Mất khả năng xóa thật nếu có màn admin cũ | PO/nghiệp vụ + DBA |
| `SP_DangKy` | Không khớp flow đăng ký hiện tại | `CongThongTinWebRepository.taoHoiVien`, `KhachHangDAO.insert` | App cũ ngoài repo có thể gọi | PO/nghiệp vụ + Tech lead |
| `SP_CapNhatTrangThaiDichVu` | Trùng chức năng một phần, đặt sai module, chưa có caller | `sp_CapNhatDichVu` qua `DichVuDAO` | Nếu sau này có màn status-only ẩn sẽ mất SP | Tech lead |

## Điểm Query Trực Tiếp Trùng Nghiệp Vụ Procedure Nhưng Chưa Nên Đổi

- `PhienLamViecDAO.ketThucPhien` và `SP_KetThucPhien`: cùng miền kết thúc phiên, nhưng khác đường tính phụ thu/trạng thái không gian; không chuyển để tránh đổi tiền.
- `HoaDonDAO.layThongTinChiTietHoaDon` và `SP_XemChiTietHoaDon`: khác input (`MaHoaDon` vs `MaPhien`) và output DTO; không chuyển.
- `PhieuGiamGiaDAO` và nhóm `QuanLyPGG`: procedure thiếu field hoặc hard delete; không chuyển.
- `KhongGianDAO` và nhóm `QuanLyKhongGian`: procedure thiếu field/order hoặc khác semantics delete/disable; không chuyển.
- `LoaiDichVuDAO` và nhóm `QuanLyLoaiDV`: một số procedure có rule chặt hơn Java; không chuyển khi chưa xác nhận nghiệp vụ.
- `KhachHangDAO`/`CongThongTinWebRepository` và nhóm `QuanLyNguoiDung`: procedure không bao phủ user + customer + role transaction; không chuyển.
- `CongThongTinWebRepository`/`QuanLyDatChoTruocDAO` dùng anonymous PL/SQL cho đặt chỗ/QR/mở phiên đặt trước; hiện chưa có named procedure đầy đủ tương đương.

## Kết Luận

Trong lượt 2.1 chỉ tạo báo cáo audit, không sửa Java/SQL, không xóa procedure và không đổi nghiệp vụ. Các tối ưu an toàn nhất tiếp theo là:

1. Chạy migration/report để xác nhận DB đang giữ canonical `SP_TraCuuDichVu` và tạo `SP_TraCuuDichVuTheoLoai` nếu cần.
2. Compile `sp_TraCuuDichVuDaDat` trên DB test sau khi sửa alias.
3. Chuẩn hóa nhóm PGG theo soft-delete hiện tại trước khi cân nhắc dùng procedure.
4. Chỉ chuyển các candidate `SP_CapNhatKhongGian`, `SP_ThemLoaiDichVu` sau khi có test Java/UI xác nhận output không đổi.

Không cần chạy migration SQL cho báo cáo này.

## Quyết định cuối cùng: Có nên ép dùng đủ 35 stored procedure không?

Không nên ép Java dùng đủ 35 stored procedure.

Stored procedure chỉ nên được dùng khi nó là nguồn nghiệp vụ chuẩn, đủ input/output cho DTO Java, đúng transaction boundary, trả message có thể map thân thiện, và khi chuyển sang dùng procedure không làm đổi behavior hiện tại. Với dự án hiện tại, việc ép dùng đủ 35 procedure có rủi ro làm sai nghiệp vụ cao hơn lợi ích vì nhiều procedure chưa khớp flow Java, thiếu field, khác transaction, khác semantics xóa/vô hiệu hóa, hoặc đang bị trùng tên.

Không dùng procedure chỉ vì nó tồn tại.

### A. Giữ nguyên đang dùng

Các procedure này đang được Java gọi và phù hợp với luồng hiện tại. Giữ nguyên, chỉ hardening message/logging ở tầng gọi nếu cần.

- `SP_ThanhToanVoiPhieuGiamGia`
- `SP_KetThucPhien`
- `SP_ThemChiNhanh`
- `SP_CapNhatChiNhanh`
- `SP_TraCuuChiNhanh`
- `sp_ThemDichVu`
- `sp_CapNhatDichVu`
- `SP_LayDanhSachKho`
- `sp_MoPhienLamViecTrucTiep`

### B. Có thể chuyển sau khi test kỹ

Các procedure này có vẻ gần khớp code Java hiện tại, nhưng chưa nên chuyển ngay vì cần test CRUD/UI/message và xác nhận không đổi behavior.

- `SP_CapNhatKhongGian`
- `SP_ThemLoaiDichVu`

### C. Cần sửa procedure trước khi dùng

Nhóm này thiếu field, sai/thiếu output, message chưa đúng, có commit nội bộ cần cân nhắc, hoặc chưa khớp DTO Java hiện tại. Chỉ đề xuất sửa procedure, chưa ép Java dùng.

- `SP_BaoCaoDoanhThu`
- `SP_XemChiTietHoaDon`
- `SP_VoHieuHoaChiNhanh`
- `SP_TraCuuDichVuTheoLoai` nếu sau này cần caller riêng cho output có thông tin loại dịch vụ
- `sp_ThemChiTietDichVu`
- `SP_NhapKhoDichVu`
- `SP_ThemKhongGian`
- `SP_TraCuuKhongGian`
- `SP_VoHieuHoaKhongGian`
- `SP_CapNhatLoaiDichVu`
- `SP_ThemKhachHang`
- `SP_CapNhatKhachHang`
- `SP_TraCuuHoiVien`
- `SP_ThemPhieuGiamGia`
- `SP_CapNhatPhieuGiamGia`
- `sp_TraCuuPhienLamViec`
- `sp_XemChiTietPhienLamViec`

### D. Không nên dùng vì khác nghiệp vụ hiện tại

Nhóm này không nên chuyển sang dùng procedure trong code hiện tại vì sẽ làm đổi semantics đang chạy đúng, ví dụ hard delete trong khi Java đang soft delete, hoặc procedure không bao phủ đủ transaction user + role + customer.

- `SP_XoaKhachHang`: procedure chỉ xóa `KHACHHANG`, trong khi Java đang xử lý cả role/user/customer theo transaction riêng.
- `SP_XoaPhieuGiamGia`: procedure hard delete, trong khi Java đang soft-disable bằng `TrangThai`.
- `SP_DangKy`: procedure chỉ tạo `NGUOIDUNG`, không bao phủ khách hàng, vai trò và dữ liệu đăng ký hiện tại.
- `SP_VoHieuHoaKhongGian`: procedure đổi trạng thái sang `Bảo trì`, trong khi Java `KhongGianDAO.xoa` đang là hard delete; không chuyển nếu chưa đổi nghiệp vụ quản lý không gian.

### E. Nên deprecate/xóa sau xác nhận

Không xóa ngay. Chỉ đưa vào danh sách deprecate/xóa sau khi PO/Tech lead/DBA xác nhận không còn caller ngoài repo và có migration/rollback rõ ràng.

- `pro_SinhMaQR`
- Bản non-canonical cũ của `SP_TraCuuDichVu` trong `QuanLyLoaiDV`
- `sp_TraCuuDichVuDaDat`
- `SP_XoaPhieuGiamGia`
- `SP_DangKy`
- `SP_CapNhatTrangThaiDichVu`

Lưu ý: Một số procedure có thể xuất hiện ở nhiều nhóm vì vừa khác nghiệp vụ hiện tại vừa nên deprecate sau xác nhận, nên tổng số lượng trong bảng không dùng để đối chiếu trực tiếp với 35 procedure trong báo cáo audit.

| Nhóm | Số lượng ước tính | Hành động |
|------|-------------------|-----------|
| Đang dùng tốt | 9 | Giữ nguyên |
| Có thể chuyển sau test | 2 | Chưa chuyển ngay |
| Cần sửa trước khi dùng | 17 | Chỉ đề xuất |
| Không nên dùng vì khác nghiệp vụ | 4 | Không chuyển |
| Nên deprecate/xóa sau xác nhận | 6 | Không xóa ngay |

Kết luận triển khai: không ép dùng đủ procedure. Hai lỗi kỹ thuật rõ ràng nhất đã được xử lý ở source: rename nguồn không canonical của `SP_TraCuuDichVu` thành `SP_TraCuuDichVuTheoLoai`, và sửa alias `ctddv` trong `sp_TraCuuDichVuDaDat`.
