# FIX REPORT

## Loi da phat hien va xu ly

1. Maven khong co san tren PATH (`mvn` khong nhan lenh).
   - Da dung Maven di kem NetBeans: `C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd`.
   - Lan dau Maven bi chan network trong sandbox khi tai dependency, sau do chay lai voi quyen duoc phep.

2. DAO trang chu hoi vien dung schema/SQL cu khong khop Oracle hien tai.
   - `SELECT TOP 10` la cu phap SQL Server, khong dung voi Oracle.
   - Bang/cot cu `PHIEUDATCHO`, `MaPhieu`, `TenKhongGian`, `SoGio`, `UUDAI_KHACHHANG`, `DiemTichLuy` khong khop cac file tao bang trong `Database/`.
   - Da doi sang `DATCHO`, `KHACHHANG.TongChiTieu`, `KHONGGIAN.TenKG`, `PHIEUGIAMGIA`, va `FETCH FIRST 10 ROWS ONLY`.

3. Man hinh Swing `TrangChuForm` cua hoi vien co nhieu chuoi tieng Viet bi loi hien thi/ma hoa.
   - Da tao lai noi dung nhan giao dien bang UTF-8 hop le.
   - Giu nguyen chuc nang hien co: load tong quan, bang lich su dat cho, panel khong gian noi bat.

4. Cau SQL gia han gio dung phep nhan interval voi bind parameter.
   - `INTERVAL '1' HOUR * ?` de gay loi Oracle khi bind.
   - Da doi thanh `NUMTODSINTERVAL(?, 'HOUR')` o ca luong Swing va web.

5. Import trung lap trong `ThongTinHoiVienController`.
   - Da xoa import trung de lam sach code.

6. Du an chua co Unit Test JUnit that.
   - Truoc do `src/test/java` chi co 2 file rong `junit5.txt`, `mvn test` pass nhung khong kiem tra nghiep vu.
   - Da them dependency `spring-boot-starter-test` va bo test JUnit Jupiter chay offline, khong can Oracle.

7. Validation dang ky/OTP/reset password dang nam truc tiep trong service va kho test neu khong dung DAO.
   - Da tach helper validate thuan Java `NguoiDungInputValidator`.
   - `NguoiDungService` chi goi helper o cac nhanh validate dau vao, khong doi flow DAO/email/OTP/hash hien co.

8. Smoke check SQL script phat hien `Database/CacChinhSuaMoi.sql` co `COMMIT` va `ALTER TABLE ... ADD` thieu dau `;`.
   - Da them dau `;` de script Oracle ro rang va pass smoke test.

## File da sua

- `src/main/java/com/wms/dao/TrangChuHoiVien/TrangChuDAO.java`
  - Sua SQL theo schema Oracle hien tai va sua thong bao loi tieng Viet.

- `src/main/java/com/wms/view/TrangChuHoiVien/TrangChu/TrangChuForm.java`
  - Sua chuoi UI bi loi font/ma hoa va giu man hinh tong quan hoi vien hoat dong voi controller cu.

- `src/main/java/com/wms/dao/TrangChuQuanLy/QuanLyDichVuDat/ChiTietDichVuDAO.java`
  - Sua SQL gia han gio sang `NUMTODSINTERVAL`.

- `src/main/java/com/wms/web/repository/CongThongTinWebRepository.java`
  - Sua SQL gia han gio sang `NUMTODSINTERVAL`.

- `src/main/java/com/wms/controller/TrangChuQuanLy/QuanLyHoiVien/ThongTinHoiVienController.java`
  - Xoa import bi lap.

- `pom.xml`
  - Them `spring-boot-starter-test` voi scope `test` de chay JUnit Jupiter/AssertJ qua Maven.

- `src/main/java/com/wms/service/TrangChuGioiThieu/NguoiDungInputValidator.java`
  - Helper validate input thuan Java cho dang ky, OTP va dat lai mat khau; khong can DAO/Oracle.

- `src/main/java/com/wms/service/TrangChuGioiThieu/NguoiDungService.java`
  - Doi cac nhanh validate dau vao sang helper moi, giu nguyen flow xu ly chinh.

- `Database/CacChinhSuaMoi.sql`
  - Them dau `;` cho `ALTER TABLE ... ADD` va `COMMIT` de pass smoke test SQL.

- `src/test/java/com/wms/util/PasswordUtilTest.java`
  - Kiem tra BCrypt hash/verify va salt.

- `src/test/java/com/wms/util/MaQRUtilTest.java`
  - Kiem tra tao QR PNG va data URI offline.

- `src/test/java/com/wms/service/TrangChuGioiThieu/NguoiDungInputValidatorTest.java`
  - Kiem tra username/email/password rong va email sai format.

- `src/test/java/com/wms/staticcheck/SqlDialectStaticTest.java`
  - Static test chan cu phap SQL Server trong source Java cua du an Oracle.

- `src/test/java/com/wms/staticcheck/EncodingStaticTest.java`
  - Static test bat mojibake ro rang bang Unicode escape; khong fail vi tieng Viet hop le nhu `MA`, `DA`, hoac chu co dau hop le.

- `src/test/java/com/wms/staticcheck/SqlScriptSmokeTest.java`
  - Smoke test script SQL Oracle: `COMMIT;`, `ALTER TABLE ... ADD ...;`, va block `CREATE OR REPLACE ... END; /`.

- `src/test/java/com/wms/dao/junit5.txt`
  - Xoa file rong gay hieu nham la da co test.

- `src/test/java/com/wms/util/junit5.txt`
  - Xoa file rong gay hieu nham la da co test.

## Unit Test da them va tieu chi do an duoc bao ve

- `PasswordUtilTest`
  - Bao ve tieu chi dang nhap/bao mat mat khau: hash khong rong, verify dung/sai, BCrypt co salt.

- `NguoiDungInputValidatorTest`
  - Bao ve tieu chi kiem tra nhap lieu: username/email/password rong va email sai format bi chan truoc khi goi DAO.

- `SqlDialectStaticTest`
  - Bao ve tieu chi dung Oracle/phu hop CSDL: fail neu source Java con `SELECT TOP`, `GETDATE()`, `ISNULL(`, hoac literal SQL Server `N'`.

- `EncodingStaticTest`
  - Bao ve tieu chi UTF-8/source sach: scan `src/main/java`, `src/main/resources`, `Database`; khong dung whitelist `.form`.

- `SqlScriptSmokeTest`
  - Bao ve chat luong script database: bat thieu `;` o `COMMIT`/`ALTER TABLE ADD` va thieu `/` sau PL/SQL block.

- `MaQRUtilTest`
  - Bao ve diem nang cao/trien khai thuc te: QR PNG va Data URI tao duoc offline.

## File da co thay doi san trong working tree khi bat dau va duoc giu lai

- `src/main/java/com/wms/util/DBUtils.java`
  - Package dang la `com.wms.util`, khop duong dan thu muc.

- `src/main/java/com/wms/view/TrangChuQuanLy/QuanLyNguoiDung/QuanLyNguoiDungForm.java`
  - Co getter `getCbxNhomQuyen()` cho controller quan ly nguoi dung.

- `src/main/java/com/wms/web/controller/NhanVienWebController.java`
  - Ten tham so constructor da dung chuan camelCase.

- `src/main/java/com/wms/web/scheduler/LichKiemTraThanhToanDatCho.java`
  - Ten tham so constructor da dung chuan camelCase.

- `src/main/java/com/wms/web/service/CongThongTinService.java`
  - Import repository dung package `com.wms.web.repository`.

- `src/main/java/com/wms/web/service/XacThucWebService.java`
  - Import repository dung package, dung `getMatKhau()` va `khopMa()` theo model/form hien tai.

## Cach test lai

Do `mvn` khong co tren PATH cua may hien tai, da chay bang Maven di kem NetBeans:

```powershell
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' -q -DskipTests compile
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' test
```

Ket qua:

- `compile`: BUILD SUCCESS.
- `test`: BUILD SUCCESS, 15 tests run, 0 failures, 0 errors, 0 skipped.

Neu da cai Maven vao PATH, co the chay lai bang dung lenh yeu cau:

```powershell
mvn -q -DskipTests compile
mvn test
```

## Loi con ton tai / chua xac thuc

- Chua chay runtime ket noi Oracle that, vi can database Oracle dung schema va du lieu trong `Database/`.
- Da co test Java that, nhung chua co integration test voi Oracle that theo dung rang buoc khong phu thuoc DB local.
- Khong sua `db.properties`, `application.properties`, `.gitignore`, hoac secret theo dung yeu cau.
