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
- Dashboard doanh thu cho quản lý.
- QR check-in/check-out ở mức utility.

## Report và export

Dự án hiện có các điểm phục vụ yêu cầu đồ án về thống kê/report và xuất file:

- Xem thống kê doanh thu tại màn hình `TongQuanForm`.
- Lọc báo cáo theo từ ngày, đến ngày, chi nhánh, loại doanh thu.
- Xuất hóa đơn PDF bằng `HoaDonPDFExporter`, được gọi từ màn hình quản lý/thanh toán hóa đơn.
- Xuất báo cáo doanh thu CSV bằng `DoanhThuReportExporter`.
- CSV có UTF-8 BOM để Excel mở tiếng Việt ổn định.
- CSV có header tiếng Việt rõ ràng và escape đúng dấu phẩy, dấu nháy kép, xuống dòng.
- Xuất báo cáo doanh thu PDF fallback bằng `DoanhThuReportExporter`.
- Xuất báo cáo doanh thu chuyên dụng bằng JasperReports qua nút `Xuất PDF Jasper` trong `TongQuanForm`.
- JasperReports dùng dữ liệu do Java/DAO lấy sẵn và truyền vào `JRBeanCollectionDataSource`, không query trực tiếp Oracle trong `.jrxml`.

Lưu ý: dự án chưa triển khai xuất Excel `.xlsx` thật, nên tài liệu và UI hiện ghi rõ là CSV/PDF, không ghi là Excel.

## Báo cáo chuyên dụng bằng JasperReports

- Template nằm tại `src/main/resources/reports/bao_cao_doanh_thu.jrxml`.
- Exporter nằm tại `DoanhThuJasperReportExporter`.
- Dữ liệu chi tiết từng dòng dùng `DoanhThuReportRowDTO`.
- Demo: mở màn `Tổng quan`, chọn khoảng ngày/chi nhánh/loại doanh thu, bấm `Xuất PDF Jasper`, chọn nơi lưu file PDF.
- Để tránh phụ thuộc font proprietary, PDF Jasper dùng text không dấu nhất quán khi export. Source `.jrxml` và tài liệu vẫn là UTF-8.

## Kiểm thử

Bộ test hiện tại là unit/static test chạy offline, không cần Oracle local:

- `PasswordUtilTest`: kiểm tra BCrypt hash/verify và salt.
- `NguoiDungInputValidatorTest`: kiểm tra validate input đăng ký cơ bản.
- `MaQRUtilTest`: kiểm tra tạo QR PNG và Data URI.
- `SqlDialectStaticTest`: chặn cú pháp SQL Server trong source Java của dự án Oracle.
- `EncodingStaticTest`: bắt mojibake rõ trong source/resource/Database.
- `SqlScriptSmokeTest`: smoke test script SQL Oracle.
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

## Lưu ý cấu hình

Không commit secret thật. Các file cấu hình thật như `db.properties`, `application.properties`, `.gitignore` được giữ nguyên theo chủ repo.
