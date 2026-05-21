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

## Lỗi còn tồn tại / chưa xác thực

- Chưa chạy runtime với Oracle thật trong phiên làm việc này.
- Chưa có integration test Oracle thật.
- Chưa có UI automation/runtime test cho Swing hoặc web.
- Chưa có xuất Excel `.xlsx` thật; hiện tại project có doanh thu CSV/PDF và hóa đơn PDF.
- Không sửa `db.properties`, `application.properties`, `.gitignore`, hoặc secret thật.
