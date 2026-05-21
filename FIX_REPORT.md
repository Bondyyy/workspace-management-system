# FIX REPORT

## Lỗi đã phát hiện và xử lý

1. Dự án ban đầu chưa có Unit Test JUnit thật.
   - Đã thêm `spring-boot-starter-test`.
   - Đã thay các file test rỗng bằng test Java thật chạy offline, không cần Oracle.

2. Validation đăng ký/OTP/reset password khó test nếu đi thẳng qua service/DAO.
   - Đã tách helper thuần Java `NguoiDungInputValidator`.
   - `NguoiDungService` chỉ gọi helper ở các nhánh validate đầu vào, không đổi flow DAO/email/OTP/hash.

3. Script SQL có lỗi smoke test.
   - Đã sửa `Database/CacChinhSuaMoi.sql` thiếu `;` cho `COMMIT`/`ALTER TABLE ... ADD`.

4. Dashboard doanh thu có rủi ro sai schema Oracle ở filter chi nhánh.
   - Đã rà `ThongKeDAO` và dùng `kg.MaCN` đúng với bảng `KHONGGIAN`.
   - Không dùng `MaChiNhanh` trong query doanh thu.
   - Trạng thái thanh toán dùng `Đã thanh toán%` để khớp các giá trị như `Đã thanh toán` và `Đã thanh toán thành công`.

5. Report/export doanh thu chưa rõ với người chấm.
   - Đã tách `DoanhThuReportExporter`.
   - Đã chuẩn hóa xuất doanh thu CSV/PDF trong `TongQuanForm`.
   - CSV có UTF-8 BOM, header tiếng Việt, escape dấu phẩy, dấu nháy kép, xuống dòng.
   - PDF doanh thu có tiêu đề, khoảng ngày, chi nhánh, tổng doanh thu, số hóa đơn, bảng giao dịch, ngày xuất.
   - PDF doanh thu dùng text không dấu nhất quán để tránh lỗi font tiếng Việt với font mặc định của iText.
   - `.form` của NetBeans đã đổi dấu vết nút Excel cũ thành `btnXuatCSV`/`Xuất CSV`.

6. Tài liệu checklist/README còn ghi chưa rõ về Unit Test và report/export.
   - Đã cập nhật `README.md` và `DO_AN_RUBRIC_CHECKLIST.md` bằng tiếng Việt có dấu.
   - Checklist ghi trung thực: đã có unit/static test cơ bản, chưa có integration test Oracle thật.
   - Checklist ghi rõ: hóa đơn PDF, doanh thu CSV/PDF, không ghi Excel nếu chưa làm Excel thật.

## File đã sửa

- `pom.xml`
  - Thêm dependency test để chạy JUnit Jupiter.

- `src/main/java/com/wms/service/TrangChuGioiThieu/NguoiDungInputValidator.java`
  - Helper validate input thuần Java cho đăng ký/OTP/reset password.

- `src/main/java/com/wms/service/TrangChuGioiThieu/NguoiDungService.java`
  - Gọi helper validate, giữ nguyên flow nghiệp vụ hiện có.

- `Database/CacChinhSuaMoi.sql`
  - Sửa dấu `;` để script Oracle rõ ràng hơn và pass smoke test.

- `src/main/java/com/wms/dao/TrangChuQuanLy/TongQuan/ThongKeDAO.java`
  - Sửa query doanh thu dùng `kg.MaCN`.
  - Làm sạch chuỗi tiếng Việt và tên method bị mojibake.
  - Giữ các public method chính đang được controller/service dùng.

- `src/main/java/com/wms/service/TrangChuQuanLy/TongQuan/TongQuanService.java`
  - Cập nhật call tới method biểu đồ 7 ngày đã làm sạch tên.

- `src/main/java/com/wms/view/TrangChuQuanLy/TongQuan/TongQuanForm.java`
  - Chuẩn hóa màn Tổng quan: lọc báo cáo, xem thống kê, xuất CSV, xuất PDF.
  - Dùng `DoanhThuReportExporter` thay vì viết CSV trực tiếp trong form.

- `src/main/java/com/wms/view/TrangChuQuanLy/TongQuan/TongQuanForm.form`
  - Đổi dấu vết Excel sang CSV ở tên component, text và handler metadata.

- `src/main/java/com/wms/util/DoanhThuReportExporter.java`
  - Export doanh thu CSV/PDF.
  - CSV có BOM UTF-8 và escape đúng.
  - PDF có cấu trúc báo cáo doanh thu rõ ràng.

- `src/test/java/com/wms/util/PasswordUtilTest.java`
  - Kiểm tra BCrypt hash/verify/salt.

- `src/test/java/com/wms/util/MaQRUtilTest.java`
  - Kiểm tra QR PNG và Data URI.

- `src/test/java/com/wms/service/TrangChuGioiThieu/NguoiDungInputValidatorTest.java`
  - Kiểm tra validate đăng ký cơ bản.

- `src/test/java/com/wms/staticcheck/SqlDialectStaticTest.java`
  - Chặn dấu vết SQL Server trong source Java của dự án Oracle.

- `src/test/java/com/wms/staticcheck/EncodingStaticTest.java`
  - Bắt mojibake rõ trong source/resource/Database.

- `src/test/java/com/wms/staticcheck/SqlScriptSmokeTest.java`
  - Smoke test script SQL Oracle.

- `src/test/java/com/wms/util/DoanhThuReportExporterTest.java`
  - Kiểm tra CSV doanh thu tạo file không rỗng, có BOM, header tiếng Việt, escape dấu phẩy/nháy kép/xuống dòng.

- `README.md`
  - Cập nhật mô tả chức năng report/export và cách chạy test.

- `DO_AN_RUBRIC_CHECKLIST.md`
  - Cập nhật trạng thái rubric: Unit Test đạt cơ bản, report/export doanh thu CSV/PDF rõ ràng.

## Unit Test và tiêu chí đồ án được bảo vệ

- `PasswordUtilTest`: đăng nhập, bảo mật mật khẩu.
- `NguoiDungInputValidatorTest`: kiểm tra nhập liệu.
- `SqlDialectStaticTest`: phù hợp Oracle, tránh lỗi runtime do sai dialect.
- `EncodingStaticTest`: UTF-8/source sạch.
- `SqlScriptSmokeTest`: chất lượng script database.
- `MaQRUtilTest`: điểm nâng cao/thực tế triển khai.
- `DoanhThuReportExporterTest`: thống kê/report và xuất file doanh thu.

## Cách test lại

Nếu Maven có trong PATH:

```powershell
mvn -q -DskipTests compile
mvn test
```

Nếu Maven chưa có trong PATH, dùng Maven đi kèm NetBeans:

```powershell
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' -q -DskipTests compile
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' test
```

## Kết quả kiểm thử gần nhất

- `mvn -q -DskipTests compile`: BUILD SUCCESS.
- `mvn test`: BUILD SUCCESS, 16 tests run, 0 failures, 0 errors, 0 skipped.

## Preflight trước khi tích hợp JasperReports

- Đã chạy `mvn clean` bằng Maven đi kèm NetBeans do `mvn` chưa có trên PATH: BUILD SUCCESS.
- Đã chạy `mvn -q -DskipTests compile` bằng Maven đi kèm NetBeans: BUILD SUCCESS.
- Đã chạy `mvn test` bằng Maven đi kèm NetBeans: BUILD SUCCESS, 16 tests run, 0 failures, 0 errors, 0 skipped.
- `DoanhThuReportExporter` vẫn có đủ `xuatCsv` và `xuatPdf`.
- `DoanhThuReportExporterTest` vẫn pass trong suite test.
- `TongQuanForm` vẫn có nút `Xuất CSV` và `Xuất PDF`.
- `README.md`, `FIX_REPORT.md`, `DO_AN_RUBRIC_CHECKLIST.md` không còn pattern mojibake rõ khi rà bằng `rg`.
- Không thêm dependency mới trong bước preflight này.
- Project đã sẵn sàng để thêm JasperReports ở bước tiếp theo.

## Tích hợp JasperReports cho báo cáo doanh thu chuyên dụng

- Đã thêm dependency `net.sf.jasperreports:jasperreports:6.21.5` vào `pom.xml`.
- Đã thêm template `src/main/resources/reports/bao_cao_doanh_thu.jrxml`.
- Đã thêm `DoanhThuReportRowDTO` để truyền dữ liệu chi tiết cho JasperReports.
- Đã thêm `DoanhThuJasperReportExporter`:
  - load template từ classpath `/reports/bao_cao_doanh_thu.jrxml`;
  - compile/fill report bằng `JRBeanCollectionDataSource`;
  - export PDF ra file người dùng chọn;
  - không query trực tiếp DB trong `.jrxml`.
- Đã thêm đường controller/service/DAO riêng để lấy dòng báo cáo doanh thu JasperReports, có tên khách hàng, chi nhánh, không gian, tổng tiền, thành tiền, phương thức và trạng thái thanh toán.
- Query doanh thu JasperReports dùng `PreparedStatement`, dùng `kg.MaCN`, không dùng `MaChiNhanh`, không dùng cú pháp SQL Server.
- Đã tích hợp nút `Xuất PDF Jasper` vào `TongQuanForm`, không xóa nút `Xuất CSV` và không thay thế PDF fallback hiện có.
- Đã thêm `DoanhThuJasperReportExporterTest` để kiểm tra load/compile `.jrxml` và export PDF từ dữ liệu mẫu offline.
- PDF JasperReports dùng text không dấu nhất quán khi export để tránh phụ thuộc font proprietary; source Java, `.jrxml`, README và checklist vẫn là UTF-8.

### Trạng thái build/test sau khi thêm JasperReports

- Đã thử chạy `mvn -q -DskipTests compile` bằng Maven đi kèm NetBeans.
- Lần chạy trong sandbox thất bại vì môi trường không cho Maven truy cập Maven Central: `Permission denied: getsockopt`.
- Lần chạy escalation bị hệ thống từ chối do giới hạn sử dụng phiên hiện tại, nên chưa xác minh được dependency JasperReports bằng Maven trong lượt này.
- Chưa sửa lan man hoặc thay thế logic report hiện có. Cần chạy lại:

```powershell
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' -q -DskipTests compile
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' test
```

hoặc:

```powershell
mvn -q -DskipTests compile
mvn test
```

## Lỗi còn tồn tại / chưa xác thực

- Chưa chạy runtime với Oracle thật trong phiên làm việc này.
- Chưa xác minh Maven sau khi thêm JasperReports do sandbox/network bị chặn trong lượt chạy hiện tại.
- Chưa có integration test Oracle thật.
- Chưa có UI automation/runtime test cho Swing hoặc web.
- Chưa có xuất Excel `.xlsx` thật; hiện tại project có doanh thu CSV/PDF và hóa đơn PDF.
- Không sửa `db.properties`, `application.properties`, `.gitignore`, hoặc secret thật.
