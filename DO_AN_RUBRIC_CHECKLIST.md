# ĐỒ ÁN RUBRIC CHECKLIST

Audit theo yêu cầu đồ án Java Core/Swing/JDBC. Spring Boot web là phần mở rộng đã được giảng viên cho phép; luồng chính vẫn là Swing/JDBC.

## Tổng quan nhanh

- Công nghệ: Java 17, Maven, Swing, JDBC Oracle, Spring Boot web, Thymeleaf.
- Cấu trúc tương đối rõ theo `view`, `controller`, `service`, `dao`, `model`.
- Có script CSDL riêng trong `Database/`: table, constraint, function, procedure, trigger, data.
- Có nhiều chức năng thực tế: đặt chỗ, phiên làm việc, hóa đơn, dịch vụ, khuyến mãi, kho, nhân viên, phân quyền.
- Đã có unit/static test cơ bản, chưa có integration test Oracle thật và chưa có UI automation/runtime test.
- Report/export đã rõ hơn: hóa đơn PDF, dashboard doanh thu, xuất doanh thu CSV/PDF. Chưa triển khai Excel `.xlsx` thật.

## Bảng checklist rubric

| Tiêu chí | Hiện trạng trong project | File/chức năng minh chứng | Đánh giá | Việc cần sửa cụ thể |
|---|---|---|---|---|
| Áp dụng quy trình phát triển phần mềm | Có Maven, package theo module, database script tách riêng. Tài liệu quy trình/use case/test plan còn mỏng. | `pom.xml`, `README.md`, `Database/`, `src/main/java/com/wms/...` | Cần cải thiện | Bổ sung tài liệu use case, quy trình cài DB, quy trình test, tài khoản demo. |
| Đăng ký / đăng nhập | Có đăng ký, đăng nhập, quên mật khẩu, OTP email cho Swing và web. Mật khẩu dùng BCrypt. Đã có test cho password và input đăng ký. | `DangNhapController.java`, `DangKyController.java`, `NguoiDungService.java`, `NguoiDungInputValidator.java`, `PasswordUtil.java`, `PasswordUtilTest.java`, `NguoiDungInputValidatorTest.java` | Đạt cơ bản | Nếu còn thời gian, thêm test service với mock DAO cho tài khoản khóa, sai mật khẩu, OTP sai/hết hạn. |
| CRUD | Có CRUD/gần CRUD cho chi nhánh, không gian, loại không gian, dịch vụ, phiếu giảm giá, hội viên, nhân viên, người dùng, vai trò, phiên, hóa đơn. Một số “xóa” là vô hiệu hóa theo nghiệp vụ. | Các form/DAO trong `TrangChuQuanLy/*` | Đạt | Lập bảng CRUD trong báo cáo đồ án, ghi rõ entity nào thêm/sửa/xóa/tìm kiếm. |
| Thống kê/report | Có dashboard doanh thu, biểu đồ 7 ngày, cơ cấu thanh toán, giao dịch gần nhất, lọc theo ngày/chi nhánh/loại. Query doanh thu đã dùng `kg.MaCN` đúng schema Oracle. | `TongQuanForm.java`, `TongQuanController.java`, `TongQuanService.java`, `ThongKeDAO.java` | Đạt cơ bản | Chạy demo với Oracle thật để xác nhận số liệu. Có thể đồng nhất thêm với procedure `SP_BaoCaoDoanhThu` nếu muốn. |
| Xuất report sang file | Có hóa đơn PDF bằng iText. Có doanh thu CSV/PDF qua `DoanhThuReportExporter`. CSV có UTF-8 BOM, header tiếng Việt và escape đúng. `.form` đã đổi nút từ Excel sang CSV. | `HoaDonPDFExporter.java`, `QuanLyHoaDonForm.java`, `ThanhToanHoaDonForm.java`, `TongQuanForm.java`, `DoanhThuReportExporter.java`, `DoanhThuReportExporterTest.java` | Đạt | Không ghi Excel nếu chưa làm Excel thật. Nếu giảng viên yêu cầu Excel, bổ sung Apache POI sau. |
| UI/UX logic, dễ sử dụng | Có nhiều màn hình đầy đủ, menu sidebar, bảng dữ liệu, form nhập, file chooser, sơ đồ không gian. Một số màn NetBeans vẫn cần kiểm tra encoding/runtime bằng mắt. | `TrangChuQuanLyForm.java`, `TrangChuHoiVienForm.java`, các `*Form.java`, templates web | Cần cải thiện | Test tay các màn demo chính: login, Tổng quan, Hội viên, Nhân viên, hóa đơn PDF, xuất CSV/PDF. |
| Tối thiểu 3 actor không tính Guest | Có ít nhất 4 actor hợp lý: hội viên/khách hàng, nhân viên/lễ tân, quản lý, admin/quản trị hệ thống. | `TrangChuHoiVienForm.java`, `TrangChuQuanLyForm.java`, `NhanVienWebController.java`, `QuanLyVaiTroForm.java`, `SuperAdminCreator.java`, `NguoiDungDTO.hasRole/hasChucNang` | Đạt | Chuẩn bị tài khoản demo và ảnh màn hình riêng cho từng actor. |
| Mỗi actor có view/chức năng khác nhau | Hội viên có portal riêng; staff web có `/staff/bookings`; quản lý/admin dùng `TrangChuQuanLyForm` với menu theo chức năng. | `TrangChuHoiVienForm.java`, `TrangChuQuanLyForm.java`, `ChanXacThuc.java`, `NhanVienWebController.java`, `NguoiDungDTO.java` | Đạt | Demo bằng 3-4 tài khoản khác nhau. |
| Kiểm tra nhập liệu | Có validate trong service/form/DB và web Bean Validation. Đã có `NguoiDungInputValidatorTest` cho username/email/password rỗng và email sai format. | `NguoiDungInputValidator.java`, `NguoiDungInputValidatorTest.java`, `DangKyWebForm.java`, `Database/02_constraint/RangBuocMienGiaTri.sql` | Đạt cơ bản | Mở rộng test cho booking, phiếu giảm giá, nhân viên, hội viên. |
| Tự động điền/gợi ý/chuyển đổi dữ liệu | Có sinh mã, auto fill khi chọn bảng, combobox role/chi nhánh/loại, sơ đồ không gian, QR, OTP, auto tính hóa đơn, auto cập nhật trạng thái. | `DataInitializer.java`, `MaQRUtil.java`, `SoDoKhongGianPanel.java`, các trigger trong `Database/05_triggers` | Đạt | Nêu rõ các điểm tự động trong slide/báo cáo. |
| Chống tấn công | JDBC chủ yếu dùng `PreparedStatement`/`CallableStatement`, mật khẩu BCrypt, DB có constraint. Có `PasswordUtilTest`. Web chưa có Spring Security/CSRF đầy đủ. | `PasswordUtil.java`, `PasswordUtilTest.java`, các DAO/repository, `Database/02_constraint` | Cần cải thiện | Nếu kịp, thêm test injection cho search và ghi rõ phạm vi web là phần mở rộng. |
| Tách giao diện/nghiệp vụ, MVC/3 lớp | Phần lớn Swing theo `view-controller-service-dao-model`. Web theo `controller-service-repository-model-form-template`. Vẫn còn một số logic trong form/DAO nhưng chấp nhận được cho đồ án. | `src/main/java/com/wms/view`, `controller`, `service`, `dao`, `model`, `web/*` | Đạt | Không refactor lớn; chỉ tách thêm validate/export khi cần. |
| OOP, tổ chức lớp tốt | DTO/model riêng, util riêng, service/DAO theo module. Có helper PDF, QR, email, password, input validator, exporter doanh thu. | `model/*DTO.java`, `HoaDonPDFExporter.java`, `DoanhThuReportExporter.java`, `MaQRUtil.java`, `PasswordUtil.java`, `NguoiDungInputValidator.java` | Đạt | Trong báo cáo nêu rõ package/layer và các util/helper có test. |
| Độc lập CSDL ở mức phù hợp | SQL được gom trong DAO/repository. Project phụ thuộc Oracle có chủ đích. Đã có `SqlDialectStaticTest` chặn dấu vết SQL Server trong Java. Query doanh thu dùng `MaCN` đúng schema. | `DatabaseConnection.java`, `DBUtils.java`, `ThongKeDAO.java`, `SqlDialectStaticTest.java` | Đạt cơ bản | Ghi rõ độc lập ở mức cấu hình/DAO, không độc lập vendor vì đề tài dùng Oracle. |
| Dùng Maven | Có `pom.xml`, Java 17, Spring Boot parent, Oracle JDBC, iText, BCrypt, Surefire, `spring-boot-starter-test`. | `pom.xml` | Đạt | Duy trì `mvn test` trước khi nộp. |
| Tìm kiếm theo nhiều tiêu chí | Có tìm kiếm/filter ở nhiều module: chi nhánh, không gian, hóa đơn, tổng quan theo ngày/chi nhánh/loại, phiên, đặt chỗ. | `KhongGianDAO.timKiemKhongGian`, `HoaDonDAO`, `ThongKeDAO`, `PhienLamViecDAO`, `QuanLyDatChoTruocDAO` | Đạt | Test tay lại filter ngày và filter chi nhánh với Oracle thật. |
| Unit Test JUnit | Đã có test Java thật. `mvn test` pass 16 tests. Bộ test hiện là unit/static test cơ bản, không cần Oracle. Có thêm test CSV exporter doanh thu. | `PasswordUtilTest.java`, `MaQRUtilTest.java`, `NguoiDungInputValidatorTest.java`, `SqlDialectStaticTest.java`, `EncodingStaticTest.java`, `SqlScriptSmokeTest.java`, `DoanhThuReportExporterTest.java` | Đạt cơ bản | Mở rộng test nghiệp vụ/integration test nếu còn thời gian: auth service mock DAO, booking validation, Oracle integration smoke. |
| Encoding / UTF-8 source | Có `EncodingStaticTest` scan `src/main/java`, `src/main/resources`, `Database` để bắt mojibake rõ. Không fail vì ký tự tiếng Việt hợp lệ. | `EncodingStaticTest.java` | Đạt cơ bản | Tiếp tục sửa chuỗi UI/log/PDF còn xấu nếu demo phát hiện. |
| DB script smoke test | Có `SqlScriptSmokeTest` kiểm tra `COMMIT;`, `ALTER TABLE ... ADD ...;`, và block `CREATE OR REPLACE ... END; /`. | `SqlScriptSmokeTest.java`, `Database/CacChinhSuaMoi.sql` | Đạt cơ bản | Chạy script với Oracle thật trước khi nộp. |
| Điểm nâng cao/sáng tạo/triển khai thực tế | Có web app, OTP email, QR, hóa đơn PDF, doanh thu CSV/PDF, scheduler, webhook thanh toán, trigger/procedure DB, RBAC, dashboard doanh thu, sơ đồ không gian. | `WebApplication.java`, `MaQRUtil.java`, `HoaDonPDFExporter.java`, `DoanhThuReportExporter.java`, `WebhookThanhToanController.java`, `Database/05_triggers` | Đạt | Demo QR, PDF hóa đơn, dashboard, export doanh thu, role-based menu. |

## Actor và chức năng tương ứng

| Actor | Màn hình/view | Chức năng chính | Minh chứng |
|---|---|---|---|
| Hội viên / khách hàng | Swing: `TrangChuHoiVienForm`, `XemChiNhanhForm`, `XemSoDoKhongGianForm`, `ChuyenKhoanDatTruoc`, `ThongTinHoiVienForm`, `UuDaiCuaToiForm`. Web: `/portal`, `/portal/branches`, `/portal/history`, `/portal/benefits`, `/portal/account`, `/portal/checkout`. | Đăng ký/đăng nhập, xem chi nhánh/không gian, đặt chỗ, thanh toán đặt trước, xem lịch sử, xem ưu đãi, cập nhật tài khoản. | `TrangChuHoiVienForm.java`, `CongThongTinController.java`, templates web |
| Nhân viên / lễ tân | `MoPhienMoiForm`, `QuanLyPhienForm`, `QuanLyDichVuDatForm`, `QuanLyHoaDonForm`, `ThanhToanHoaDonForm`, `QuanLyDatChoTruocForm`, web `/staff/bookings`. | Mở phiên, xác nhận đặt chỗ, check-in, thêm dịch vụ, gia hạn giờ, kết thúc phiên, thanh toán và xuất hóa đơn. | `MoPhienMoiController.java`, `PhienLamViecService.java`, `NhanVienWebController.java`, `QuanLyHoaDonForm.java` |
| Quản lý | `TrangChuQuanLyForm`, `TongQuanForm`, các màn chi nhánh/không gian/dịch vụ/kho/hội viên/nhân viên/phiên/hóa đơn/khuyến mãi. | Xem dashboard doanh thu, lọc báo cáo, xuất CSV/PDF, quản lý vận hành. | `TrangChuQuanLyForm.java`, `TongQuanForm.java`, `QuanLyChiNhanhForm.java`, `QuanLyNhanVienForm.java` |
| Admin / quản trị hệ thống | `QuanLyNguoiDungForm`, `QuanLyVaiTroForm`, `QuanLyNhanVienForm`, menu admin trong `TrangChuQuanLyForm`. | Quản lý người dùng, vai trò/chức năng, tạo super admin, phân quyền. | `SuperAdminCreator.java`, `QuanLyNguoiDungController.java`, `VaiTroController.java`, `VaiTroDAO.java` |

## Kiểm tra report / export

| Hạng mục | Hiện trạng | Đánh giá | Ghi chú |
|---|---|---|---|
| Hóa đơn PDF | Có xuất PDF bằng iText qua `HoaDonPDFExporter`, gọi từ `QuanLyHoaDonForm` và `ThanhToanHoaDonForm`. | Đạt | Cần test tay với dữ liệu Oracle thật trước khi demo. |
| Báo cáo doanh thu | Có dashboard doanh thu, lọc từ ngày/đến ngày/chi nhánh/loại, biểu đồ 7 ngày, cơ cấu thanh toán, giao dịch gần nhất. | Đạt cơ bản | Query chi nhánh dùng `kg.MaCN`, không còn `MaChiNhanh`. |
| CSV doanh thu | Có `DoanhThuReportExporter.xuatCsv`, UTF-8 BOM, header tiếng Việt, escape dấu phẩy/nháy kép/xuống dòng. | Đạt | Đã có `DoanhThuReportExporterTest`. |
| PDF doanh thu | Có `DoanhThuReportExporter.xuatPdf`, gồm tiêu đề, khoảng ngày, chi nhánh, tổng doanh thu, số hóa đơn, bảng hóa đơn/giao dịch, ngày xuất. | Đạt cơ bản | PDF dùng text không dấu nhất quán để tránh lỗi font iText. |
| Excel | Chưa làm Excel `.xlsx` thật, không có Apache POI. UI/tài liệu hiện ghi CSV/PDF. | Không áp dụng nếu rubric không bắt buộc Excel | Nếu giảng viên yêu cầu Excel riêng, thêm Apache POI và exporter `.xlsx`. |

## Kiểm tra Unit Test

Hiện trạng: **Đạt cơ bản**.

- Đã có `spring-boot-starter-test` trong `pom.xml`.
- Đã xóa file test rỗng kiểu `junit5.txt`.
- `mvn test` pass 16 tests, 0 failures, 0 errors, 0 skipped.
- Bộ test không cần Oracle local.
- Chưa có integration test Oracle thật, chưa có UI automation/runtime test cho Swing/web.

| Test hiện có | Mục đích | Tiêu chí đồ án được bảo vệ |
|---|---|---|
| `PasswordUtilTest` | Kiểm tra BCrypt hash/verify và salt. | Đăng nhập, bảo mật mật khẩu. |
| `NguoiDungInputValidatorTest` | Kiểm tra username/email/password rỗng và email sai format. | Kiểm tra nhập liệu. |
| `SqlDialectStaticTest` | Chặn `SELECT TOP`, `GETDATE()`, `ISNULL(`, literal SQL Server `N'`. | Phù hợp Oracle, giảm lỗi runtime sai dialect. |
| `EncodingStaticTest` | Bắt mojibake rõ trong source/resource/Database. | UTF-8/source sạch, UI/UX. |
| `SqlScriptSmokeTest` | Kiểm tra lỗi script SQL phổ biến. | Chất lượng database script. |
| `MaQRUtilTest` | Kiểm tra tạo QR PNG và Data URI. | Điểm nâng cao/thực tế triển khai. |
| `DoanhThuReportExporterTest` | Kiểm tra CSV doanh thu có BOM, header và escape đúng. | Report/export doanh thu. |

## Ưu tiên sửa trước khi nộp

1. Chạy demo với Oracle thật theo script `Database/`: login, đặt chỗ, mở phiên, thanh toán, xuất hóa đơn PDF, xuất doanh thu CSV/PDF.
2. Mở rộng test nghiệp vụ/integration test nếu còn thời gian: auth service mock DAO, booking validation, Oracle smoke test.
3. Kiểm tra số liệu dashboard doanh thu với dữ liệu thật, nhất là trạng thái thanh toán và filter chi nhánh.
4. Sửa encoding/mojibake còn nhìn thấy trong các màn demo chính nếu phát hiện.
5. Tạo tài liệu use case/actor/test plan ngắn để nộp kèm.
6. Chuẩn bị tài khoản demo cho hội viên, nhân viên/lễ tân, quản lý, admin.
7. Test tay validation các form quan trọng: nhân viên, hội viên, đặt chỗ, phiếu giảm giá, nhập kho.
8. Chỉ bổ sung Excel `.xlsx` nếu giảng viên yêu cầu riêng; hiện tại project đã có CSV/PDF cho doanh thu.
9. Tạo file config example nếu cần hướng dẫn chạy, không sửa/commit secret thật.
10. Chụp ảnh màn hình các luồng đẹp: QR, hóa đơn PDF, dashboard, export doanh thu, phân quyền.
