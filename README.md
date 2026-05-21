# Workspace Management System

Hệ thống quản lý không gian làm việc/học tập dùng Java 17, Maven, Swing/JDBC với Oracle Database. Dự án cũng có phần Spring Boot web đã được giảng viên cho phép như một phần mở rộng.

## Công nghệ chính

- Java 17, Maven.
- Swing desktop app, JDBC/DAO/Service/Controller.
- Oracle Database, script nằm trong thư mục `Database/`.
- Spring Boot 3.3.5, Thymeleaf cho web app.
- iText để xuất hóa đơn PDF và PDF doanh thu fallback.
- JasperReports để xuất báo cáo doanh thu chuyên dụng.
- BCrypt để hash mật khẩu.
- JUnit Jupiter qua `spring-boot-starter-test`.

## Chức năng chính

- Đăng ký, đăng nhập, quên mật khẩu, OTP email.
- Quản lý hội viên/khách hàng, nhân viên, người dùng, vai trò.
- Quản lý chi nhánh, không gian, loại không gian.
- Đặt chỗ, mở phiên làm việc, gia hạn phiên, gọi dịch vụ.
- Thanh toán hóa đơn, áp dụng phiếu giảm giá, tích lũy/thăng hạng hội viên.
- Dashboard doanh thu cho quản lý/admin.
- QR check-in/check-out ở mức utility.

## Report và export

- Xem thống kê doanh thu tại màn hình `TongQuanForm`.
- Lọc báo cáo theo từ ngày, đến ngày, chi nhánh, loại doanh thu.
- Xuất hóa đơn PDF bằng `HoaDonPDFExporter`.
- Xuất báo cáo doanh thu CSV/PDF fallback bằng `DoanhThuReportExporter`.
- CSV có UTF-8 BOM để Excel mở tiếng Việt ổn định.
- Xuất báo cáo doanh thu chuyên dụng bằng JasperReports qua nút `Xuất PDF Jasper` trong `TongQuanForm`.
- JasperReports dùng dữ liệu do Java/DAO lấy sẵn và truyền vào `JRBeanCollectionDataSource`, không query trực tiếp Oracle trong `.jrxml`.

Lưu ý: dự án chưa triển khai xuất Excel `.xlsx` thật, nên tài liệu và UI hiện ghi rõ là CSV/PDF.

## Xử lý đồng thời

Dự án đã bổ sung xử lý đồng thời cho ba luồng nghiệp vụ chính:

- Mở phiên làm việc trực tiếp: khóa dòng `KHONGGIAN` bằng `FOR UPDATE NOWAIT`, chỉ mở phiên khi không gian đang `Trống`.
- Nhập kho dịch vụ: khóa `LOAIDICHVU`/`DICHVU` khi tìm hoặc tạo mới, tránh mất cập nhật số lượng tồn khi hai nhân viên nhập cùng lúc.
- Thanh toán và phiếu giảm giá: khóa `PHIENLAMVIEC`, `HOADON`, và `PHIEUGIAMGIA` trước khi cập nhật, tránh thanh toán trùng phiên hoặc dùng vượt lượt mã giảm giá.

Tài liệu chi tiết nằm ở `docs/CONCURRENCY.md`. Script demo hai session nằm trong `Database/07_concurrency_test/`.

## Kiểm thử

Bộ test hiện tại là unit/static test chạy offline, không cần Oracle local:

- `PasswordUtilTest`: kiểm tra BCrypt hash/verify và salt.
- `NguoiDungInputValidatorTest`: kiểm tra validate input đăng ký cơ bản.
- `MaQRUtilTest`: kiểm tra tạo QR PNG và Data URI.
- `SqlDialectStaticTest`: chặn cú pháp SQL Server trong source Java của dự án Oracle.
- `EncodingStaticTest`: bắt mojibake rõ trong source/resource/Database.
- `SqlScriptSmokeTest`: smoke test script SQL Oracle.
- `ConcurrencySqlStaticTest`: chặn `DBMS_SESSION.SLEEP`, trạng thái hóa đơn sai constraint, cột kho không tồn tại và insert chuỗi vào BLOB.
- `DoanhThuReportExporterTest`: kiểm tra CSV doanh thu có BOM, header tiếng Việt và escape đúng.
- `DoanhThuJasperReportExporterTest`: kiểm tra load/compile template JasperReports và export PDF từ dữ liệu mẫu offline.

Chạy kiểm thử:

```powershell
mvn -q -DskipTests compile
mvn test
```

Nếu Maven chưa có trong PATH, có thể dùng Maven đi kèm NetBeans:

```powershell
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' -q -DskipTests compile
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' test
```

## Database

- Script tạo bảng, constraint, function, procedure, trigger và data nằm trong `Database/`.
- Project dùng Oracle, không dùng SQL Server.
- Cột chi nhánh của `KHONGGIAN` là `MaCN`.
- Báo cáo JasperReports không query trực tiếp DB trong `.jrxml`; dữ liệu đi qua DAO/service hiện có.
- Các procedure nghiệp vụ chính tự quản lý `COMMIT`/`ROLLBACK` theo phong cách hiện có của project.

## Lưu ý cấu hình

Không commit secret thật. Các file cấu hình thật như `db.properties`, `application.properties`, `.gitignore` được giữ nguyên theo chủ repo.
