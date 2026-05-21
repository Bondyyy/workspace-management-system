# DO AN RUBRIC CHECKLIST

Audit theo yeu cau do an Java Core/Swing/JDBC. Spring Boot web duoc xem la phan mo rong duoc phep, nhung danh gia chinh van dua tren Swing/JDBC.

## Tong quan nhanh

- Cong nghe: Java 17, Maven, Swing, JDBC Oracle, Spring Boot web, Thymeleaf.
- Co cau truc 3 lop kha ro: `view`, `controller`, `service`, `dao`, `model`.
- Co database script rieng trong `Database/`: table, constraint, function, procedure, trigger, data.
- Co nhieu chuc nang thuc te: dat cho, phien lam viec, hoa don, dich vu, khuyen mai, kho, nhan vien, phan quyen.
- Diem yeu lon nhat truoc khi nop: Unit Test chua co, README va mot so UI/log bi loi encoding, report Excel/PDF doanh thu chua du, mot so SQL/UI co dau vet lech schema hoac lech NetBeans `.form`.

## Bang checklist rubric

| Tieu chi | Hien trang trong project | File/chuc nang minh chung | Danh gia | Viec can sua cu the |
|---|---|---|---|---|
| Ap dung quy trinh phat trien phan mem | Co cau truc Maven, package theo module, Database scripts tach rieng. Chua thay tai lieu quy trinh nhu requirement, use case, test plan, changelog, huong dan deploy DB ro rang. | `pom.xml`, `README.md`, `Database/`, `src/main/java/com/wms/...` | Can cai thien | Bo sung tai lieu `docs/`: use case, actor, quy trinh cai dat DB, quy trinh test, checklist nghiem thu. Sua README dang bi loi encoding. |
| Dang ky / dang nhap | Co dang ky, dang nhap, quen mat khau, OTP email cho Swing; web cung co dang ky/dang nhap/OTP. Mat khau dung BCrypt. | `DangNhapController.java`, `DangKyController.java`, `NguoiDungService.java`, `PasswordUtil.java`, `XacThucController.java`, `XacThucWebService.java`, `DangNhapForm.java`, `DangKyForm.java`, templates `dang-nhap.html`, `dang-ky.html`, `xac-thuc-otp.html` | Dat | Viet test cho login dung/sai mat khau/tai khoan khoa; lam sach cac chuoi log/UI bi mojibake trong service/form. |
| CRUD | Co CRUD/gan CRUD cho nhieu nhom: chi nhanh, khong gian, loai khong gian, dich vu, loai dich vu, phieu giam gia, hoi vien, nhan vien, nguoi dung, vai tro, phien, hoa don, dich vu da dat. Mot so "xoa" la vo hieu hoa theo nghiep vu. | `QuanLyChiNhanhForm.java` + `ChiNhanhDAO.java`; `QuanLyKhongGianForm.java` + `KhongGianDAO.java`; `QuanLyThongTinDichVuForm.java` + `DichVuDAO.java`; `QuanLyPhieuGiamGiaForm.java` + `PhieuGiamGiaDAO.java`; `QuanLyNguoiDungForm.java` + `NguoiDungDAO.java`; `QuanLyVaiTroForm.java` + `VaiTroDAO.java` | Dat | Lap bang CRUD trong tai lieu do an, ghi ro entity nao them/sua/xoa/tim kiem. Kiem tra lai cac nut xoa/vo hieu hoa co thong bao ro cho nguoi dung. |
| Thong ke/report | Co dashboard doanh thu, bieu do 7 ngay, co cau thanh toan, giao dich gan nhat. Co procedure DB `SP_BaoCaoDoanhThu`, nhung Java hien dang tinh qua `ThongKeDAO`, chua goi procedure nay. | `TongQuanForm.java`, `TongQuanController.java`, `TongQuanService.java`, `ThongKeDAO.java`, `Database/04_procedures/SP_BaoCaoDoanhThu.sql` | Can cai thien | Sua encoding trong man Tong Quan. Dong nhat dung procedure `SP_BaoCaoDoanhThu` hoac ghi ro Java query thay procedure. Bo sung report doanh thu PDF/Excel neu rubric yeu cau file ngoai CSV. |
| Xuat report sang file | Co xuat hoa don PDF bang iText. Co xuat CSV cho danh sach hoa don/doanh thu o Tong Quan. Chua thay xuat Excel thuc su trong `.java`; `.form` con dau vet `btnXuatExcel`. | `HoaDonPDFExporter.java`, `QuanLyHoaDonForm.java`, `ThanhToanHoaDonForm.java`, `TongQuanForm.java` | Can cai thien | Bo sung `DoanhThuReportExporter` xuat PDF hoac XLSX/CSV ro rang. Neu khong lam Excel thi xoa/doi nhan `.form` de tranh bi hoi. |
| UI/UX logic, de su dung | Co nhieu man hinh day du, menu sidebar, bang du lieu, form nhap, file chooser, so do khong gian. Tuy nhien nhieu file dang mojibake tieng Viet, nhieu layout tuyet doi NetBeans co nguy co tran giao dien. | `TrangChuQuanLyForm.java`, `TrangChuHoiVienForm.java`, cac `*Form.java`, `static/css/webapp.css`, templates web | Can cai thien | Sua encoding cac man quan trong: login, main menu, Tong Quan, Hoi Vien, Nhan Vien, PDF. Kiem tra lai text tren UI truoc khi demo. |
| Toi thieu 3 actor khong tinh Guest | Co it nhat 4 actor hop ly: Hoi vien/khach hang, nhan vien/le tan, quan ly, admin/quan tri he thong. | `TrangChuHoiVienForm.java`, `TrangChuQuanLyForm.java`, `NhanVienWebController.java`, `QuanLyVaiTroForm.java`, `SuperAdminCreator.java`, `NguoiDungDTO.hasRole/hasChucNang` | Dat | Tao bang actor trong bao cao do an, anh chup man hinh rieng tung actor. |
| Moi actor co view va chuc nang khac nhau | Hoi vien co portal rieng; staff web co man `/staff/bookings`; quan ly/admin dung `TrangChuQuanLyForm` voi menu an/hien theo chuc nang. Phan biet manager/admin co trong logic fallback. | `TrangChuHoiVienForm.java`, `TrangChuQuanLyForm.java`, `ChanXacThuc.java`, `NhanVienWebController.java`, `NguoiDungDTO.java` | Dat | Demo bang 3-4 tai khoan khac nhau. Bo sung seed role/user neu chua co trong data script. |
| Kiem tra nhap lieu | Co kiem tra trong service/form/DB: email, SDT, ngay, trang thai, gio mo cua, thoi gian dat cho, mat khau, duplicate username/email. Web co Bean Validation form. | `NhanVienService.java`, `HoiVienService.java`, `XacThucWebService.java`, `CongThongTinService.java`, `Database/02_constraint/RangBuocMienGiaTri.sql`, `NguoiDungDAO.kiemTra*` | Can cai thien | Tap trung validation dong nhat tren Swing: ngay sinh, email, SDT, so tien, so luong, ngay bat dau/ket thuc. Viet test validation. |
| Tu dong dien / goi y / chuyen doi du lieu | Co sinh ma, auto fill khi chon bang, combobox role/chi nhanh/loai, so do khong gian, QR, OTP, auto thang hang, auto cap nhat trang thai, auto tinh hoa don. | `SuperAdminCreator.java`, `DataInitializer.java`, `MaQRUtil.java`, `SoDoKhongGianPanel.java`, `TRG_TuDongThangHang.sql`, `TRG_TinhToanHoaDon.sql`, `TRG_CapNhatTrangThaiKhongGian.sql`, cac form `MouseClicked`/`load*` | Dat | Trong bao cao thuyet minh can neu ro "tu dong": sinh ma, OTP, QR, tinh tien, thang hang, cap nhat trang thai. |
| Chong tan cong | JDBC chu yeu dung `PreparedStatement`/`CallableStatement`, web dung `JdbcTemplate`, mat khau BCrypt, DB co unique/check/FK. Tuy nhien chua co Spring Security/CSRF, session Swing dung static `currentUser`, secret email/database dang co file that nhung khong duoc sua. | `PasswordUtil.java`, `NguoiDungService.java`, `XacThucWebService.java`, `CongThongTinWebRepository.java`, `Database/02_constraint`, `Database/02_constraint/Foreign.sql` | Can cai thien | Them test SQL injection cho search. Neu kip, them CSRF/Spring Security hoac ghi ro web la phu tro. Tao file example config va huong dan khong commit secret. |
| Tach giao dien va nghiep vu, MVC/3 lop | Phan lon Swing theo `view-controller-service-dao-model`. Web theo `controller-service-repository-model-form-template`. Van con mot so logic nam trong form va DAO goi DB truc tiep, nhung chap nhan duoc cho do an. | `src/main/java/com/wms/view`, `controller`, `service`, `dao`, `model`, `web/controller`, `web/service`, `web/repository` | Dat | Trong bao cao ve so do package/layer. Khong refactor lon; chi tach bot logic export/validation neu co thoi gian. |
| OOP, to chuc lop tot | DTO/model rieng, util rieng, service/DAO theo module. Co class helper PDF, QR, email, password. Co nhieu package theo nghiep vu. | `model/*DTO.java`, `HoaDonPDFExporter.java`, `MaQRUtil.java`, `EmailUtil.java`, `PasswordUtil.java`, cac service/dao theo module | Dat | Lam sach encoding/comment. Neu viet test thi uu tien service/util de chung minh OOP de test. |
| Doc lap CSDL o muc phu hop | Co `DatabaseConnection`, `DBUtils`, `application.properties`, DAO/repository gom SQL mot cho. Tuy nhien SQL phu thuoc Oracle manh: procedure, trigger, `NVL`, `SYSTIMESTAMP`, `FETCH FIRST`, `NUMTODSINTERVAL`. | `DatabaseConnection.java`, `DBUtils.java`, `Database/`, cac DAO | Can cai thien | Ghi trong bao cao: doc lap o muc cau hinh ket noi va DAO, khong doc lap vendor. Khong can doi DB vi de bai dung Oracle. |
| Dung Maven | Co `pom.xml`, Java 17, Spring Boot parent, Oracle JDBC, iText, BCrypt, Surefire. | `pom.xml` | Dat | Bo sung dependency test (`spring-boot-starter-test` hoac JUnit Jupiter) khi viet unit test. |
| Tim kiem theo nhieu tieu chi | Co tim kiem/filter o nhieu module: chi nhanh qua procedure, khong gian theo keyword/chi nhanh/loai, hoa don theo text/trang thai, tong quan theo tu ngay/den ngay/chi nhanh/loai, phien theo dieu kien, dat cho. | `ChiNhanhDAO.SP_TraCuuChiNhanh`, `KhongGianDAO.timKiemKhongGian`, `HoaDonDAO`, `ThongKeDAO.layDoanhThuTongHop`, `PhienLamViecDAO`, `QuanLyDatChoTruocDAO` | Dat | Ghi minh chung cu the trong slide/report. Test lai filter ngay va filter chi nhanh vi co dau hieu ten cot `MaChiNhanh`/`MaCN` can doi chieu DB. |
| Unit Test JUnit | Chua dat. `src/test/java` chi co `junit5.txt` rong, khong co class test. `pom.xml` chua co dependency JUnit/starter-test ro rang. | `src/test/java/com/wms/dao/junit5.txt`, `src/test/java/com/wms/util/junit5.txt`, `pom.xml` | Chua dat | Them JUnit Jupiter dependency va viet test toi thieu cho util/service/validation. Xem danh sach test de xuat ben duoi. |
| Diem nang cao / sang tao / trien khai thuc te | Co nhieu diem nang cao: web app Spring Boot, OTP email, QR check-in, PDF invoice, scheduler huy thanh toan qua han, webhook thanh toan, trigger/procedure DB, RBAC, dashboard doanh thu, so do khong gian. | `WebApplication.java`, `XacThucWebService.java`, `MaQRUtil.java`, `LichKiemTraThanhToanDatCho.java`, `WebhookThanhToanController.java`, `HoaDonPDFExporter.java`, `QuanLyVaiTroForm.java`, `Database/05_triggers`, `Database/04_procedures` | Dat | Trinh bay thanh muc "diem cong" trong bao cao. Demo 2-3 diem dep nhat: QR, PDF, dashboard, role-based menu. |

## Actor va chuc nang tuong ung

| Actor | Man hinh/view | Chuc nang chinh | Minh chung |
|---|---|---|---|
| Hoi vien / khach hang | Swing: `TrangChuHoiVienForm`, `TrangChuForm`, `XemChiNhanhForm`, `XemSoDoKhongGianForm`, `ChuyenKhoanDatTruoc`, `ThongTinHoiVienForm`, `UuDaiCuaToiForm`. Web: `/portal`, `/portal/branches`, `/portal/history`, `/portal/benefits`, `/portal/account`, `/portal/checkout`. | Dang ky/dang nhap, xem tong quan, dat cho khong gian, xem so do, thanh toan dat truoc, xem lich su, xem uu dai, cap nhat thong tin tai khoan, doi mat khau. | `TrangChuHoiVienForm.java`, `CongThongTinController.java`, templates `cong-thong-tin.html`, `chi-nhanh.html`, `so-do-khong-gian.html`, `thanh-toan.html`, `lich-su.html`, `uu-dai.html`, `tai-khoan.html` |
| Nhan vien / le tan | Swing quan ly: `MoPhienMoiForm`, `QuanLyPhienForm`, `QuanLyDichVuDatForm`, `QuanLyHoaDonForm`, `ThanhToanHoaDonForm`, `QuanLyDatChoTruocForm`. Web staff: `/staff/bookings`. | Mo phien truc tiep, xac nhan dat cho, danh dau da su dung/check-in, them dich vu vao phien, gia han gio, ket thuc phien, thanh toan hoa don tien mat/chuyen khoan, xem/xuat hoa don. | `MoPhienMoiController.java`, `PhienLamViecService.java`, `NhanVienWebController.java`, `QuanLyHoaDonForm.java`, `ThanhToanHoaDonForm.java`, `ChiTietDichVuDAO.java` |
| Quan ly | `TrangChuQuanLyForm` voi cac menu tong quan, chi nhanh, khong gian, dich vu, kho, phien, hoa don, hoi vien, nhan vien, phieu giam gia. | Xem dashboard doanh thu, loc bao cao, quan ly chi nhanh/khong gian/dich vu/kho/hoi vien/nhan vien/phien/hoa don/khuyen mai. | `TrangChuQuanLyForm.java`, `TongQuanForm.java`, `QuanLyChiNhanhForm.java`, `QuanLyKhongGianForm.java`, `QuanLyThongTinDichVuForm.java`, `QuanLyKhoForm.java`, `QuanLyNhanVienForm.java`, `QuanLyPhieuGiamGiaForm.java` |
| Admin / quan tri he thong | `QuanLyNguoiDungForm`, `QuanLyVaiTroForm`, `QuanLyNhanVienForm`, `QuanLyHangThanhVienForm`, menu admin trong `TrangChuQuanLyForm`. | Tao/sua/xoa nguoi dung, gan vai tro, quan ly vai tro/chuc nang, tao super admin, quan ly hang thanh vien, quan ly nhan vien va quyen. | `SuperAdminCreator.java`, `QuanLyNguoiDungController.java`, `QuanLyNguoiDungForm.java`, `VaiTroController.java`, `VaiTroDAO.java`, `QuanLyVaiTroForm.java`, `NguoiDungDTO.hasRole/hasChucNang` |

## Kiem tra report / export

| Hang muc | Hien trang | Danh gia | De xuat bo sung |
|---|---|---|---|
| Hoa don PDF | Co xuat PDF bang iText qua `HoaDonPDFExporter`, goi tu `QuanLyHoaDonForm` va `ThanhToanHoaDonForm`. | Dat, nhung can cai thien font/encoding | Sua noi dung PDF sang font Unicode ho tro tieng Viet, hoac tiep tuc bo dau nhung phai khong bi mojibake. |
| Bao cao doanh thu | Co dashboard doanh thu, loc tu ngay/den ngay/chi nhanh/loai, bieu do 7 ngay, co cau thanh toan, giao dich gan nhat. | Dat ve chuc nang xem, can cai thien ve xuat file | Kiem tra lai query theo schema Oracle (`MaCN` vs `MaChiNhanh`) va sua encoding tren UI. |
| CSV | Co nut `Xuất CSV` trong `TongQuanForm`, xuat danh sach hoa don theo dieu kien, co BOM UTF-8. | Dat co ban | Doi ten file mac dinh, them header tieng Viet dung UTF-8, them unit/integration test cho escape CSV. |
| Excel | `.form` co dau vet `btnXuatExcel`, nhung `.java` hien la `btnXuatCSV`; khong thay Apache POI dependency. | Chua dat neu giang vien yeu cau Excel | Neu can Excel: them Apache POI va class `DoanhThuExcelExporter`, hoac doi rubric/bao cao thanh CSV ro rang. |
| PDF doanh thu | Chua thay exporter PDF cho bao cao doanh thu. | Can cai thien | Them `DoanhThuPDFExporter` dung iText, xuat tong doanh thu + bang hoa don + thoi gian loc. |

## Kiem tra Unit Test

Hien trang: **Chua dat**.

- `src/test/java/com/wms/dao/junit5.txt` rong.
- `src/test/java/com/wms/util/junit5.txt` rong.
- Khong thay class `*Test.java`.
- `pom.xml` co Surefire plugin nhung chua co dependency test nhu `spring-boot-starter-test` hoac `junit-jupiter`.

Danh sach test toi thieu nen viet:

| Test can co | Muc dich | Goi y file |
|---|---|---|
| `PasswordUtilTest` | Hash khong null, verify dung/sai mat khau, hash moi khac hash cu. | `src/test/java/com/wms/util/PasswordUtilTest.java` |
| `MaQRUtilTest` | Tao token/PNG QR khong rong, chua ma dat cho/phien hop le. | `src/test/java/com/wms/util/MaQRUtilTest.java` |
| `XacThucWebServiceTest` | Dang ky sai confirm password, trung username/email, OTP het han/sai/dung. Mock repository/email neu can. | `src/test/java/com/wms/web/service/XacThucWebServiceTest.java` |
| `CongThongTinServiceValidationTest` | Kiem tra gio dat cho qua khu, duration invalid, ngoai gio chi nhanh, trung lich. | `src/test/java/com/wms/web/service/CongThongTinServiceValidationTest.java` |
| `HoaDonPDFExporterTest` | Neu tach logic format/export: tao file PDF temp va assert file ton tai/co size. | `src/test/java/com/wms/util/HoaDonPDFExporterTest.java` |
| `ThongKeDAOQueryTest` | Neu co test DB/Oracle test container kho qua thi test logic format date/CSV exporter rieng. | `src/test/java/com/wms/service/TrangChuQuanLy/TongQuan/...` |
| `NguoiDungServiceTest` | Authenticate: khong thay tai khoan, tai khoan khoa, sai mat khau, thanh cong. Mock DAO hoac tach interface. | `src/test/java/com/wms/service/TrangChuGioiThieu/NguoiDungServiceTest.java` |

## Cac diem can luu y khi bao ve

- Du an co diem manh ve nghiep vu va DB automation: trigger tinh hoa don, trang thai khong gian, QR, OTP, scheduler, webhook, RBAC.
- Can noi ro Spring Boot web la phan mo rong duoc phep; Swing/JDBC van co day du luong chinh.
- Nhieu file tieng Viet dang bi mojibake trong README/UI/log. Neu demo tren UI, day la rui ro rat de bi tru diem UI/UX.
- Mot so SQL trong `ThongKeDAO` can doi chieu voi schema DB hien tai, vi project dung `KHONGGIAN.MaCN` trong table script nhung code report co noi loc `kg.MaChiNhanh`.
- Khong nen refactor lon truoc khi nop; nen sua diem cham diem ro: test, encoding, report export.

## Uu tien sua truoc khi nop

1. Viet Unit Test JUnit toi thieu cho `PasswordUtil`, `MaQRUtil`, validation auth/booking; them dependency test vao `pom.xml`.
2. Sua encoding/mojibake trong README va cac man demo chinh: login, main manager, member portal, Tong Quan, PDF/export.
3. Chay demo voi Oracle that theo script `Database/`, kiem tra login, dat cho, mo phien, thanh toan, xuat PDF, xuat CSV.
4. Kiem tra va sua query report doanh thu, dac biet cot chi nhanh `MaCN`/`MaChiNhanh` va chuoi trang thai bi mojibake.
5. Bo sung xuat report doanh thu PDF hoac Excel; neu khong lam Excel thi doi UI/tai lieu thanh CSV nhat quan.
6. Tao tai lieu `docs/USE_CASES.md` hoac them vao README: actor, use case, quy trinh nghiep vu, tai khoan demo.
7. Tao tai lieu `docs/TEST_PLAN.md`: cac case test tay va ket qua mong doi.
8. Lam ro actor admin/quan ly/nhan vien/hoi vien bang tai khoan seed va anh chup man hinh.
9. Kiem tra validation cac form quan trong: them nhan vien, them hoi vien, tao dat cho, tao phieu giam gia, nhap kho.
10. Tao file config example (`db.properties.example`, `config.properties.example`) va ghi huong dan, khong sua/commit secret that.
