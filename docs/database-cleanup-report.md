# Database cleanup report

## Ket qua tom tat

- Tong so file procedure da xoa: 26
- Tong so file trigger da xoa: 3
- Tong so function da xoa: 0
- Trigger object da xoa ben trong file duoc giu lai: 1 (`TRG_CapNhatSLDaDungPhieuGiamGia`)
- `mvn clean compile`: PASS, build success.
- `mvn test`: PASS, 60 tests, 0 failures, 0 errors, 1 skipped.

## Bang file SQL da xoa

| STT | File SQL da xoa | Object ben trong | Loai | Ly do xoa | Bang chung kiem tra | Rui ro | Ket qua test |
|-----|------------------|------------------|------|-----------|---------------------|--------|--------------|
| 1 | `Database/04_procedures/sp_ThanhToanVoiPGG.sql` | `SP_ThanhToanVoiPhieuGiamGia` | Procedure | Trung object voi ban canonical trong `Database/04_procedures/QuanLyPGG/`. | `rg -n -i "SP_ThanhToanVoiPhieuGiamGia|sp_ThanhToanVoiPGG.sql" .` cho thay Java goi object, test/dev utility da doi sang path canonical. | Thap, vi object canonical van duoc giu. | PASS |
| 2 | `Database/04_procedures/HoaDon/SP_KetThucPhien.sql` | `SP_KetThucPhien` | Procedure | Trung object voi ban canonical trong `Database/04_procedures/QuanLyPhien/`. | `rg -n -i "SP_KetThucPhien|SP_KetThucPhien.sql" .` cho thay Java goi object, test da doi sang path canonical. | Thap, vi object canonical van duoc giu. | PASS |
| 3 | `Database/04_procedures/QuanLyDichVu/SP_TraCuuDichVuTheoLoai.sql` | `SP_TraCuuDichVuTheoLoai` | Procedure | Trung voi ban o `QuanLyLoaiDV` va khong co caller trong source hien tai. | `rg -n -i "SP_TraCuuDichVuTheoLoai" .` truoc khi xoa chi thay 2 file dinh nghia va prompt deprecated. | Thap, module dang dung `SP_TraCuuDichVu`. | PASS |
| 4 | `Database/04_procedures/QuanLyLoaiDV/SP_TraCuuDichVuTheoLoai.sql` | `SP_TraCuuDichVuTheoLoai` | Procedure | Trung voi ban o `QuanLyDichVu` va khong co caller trong source hien tai. | `rg -n -i "SP_TraCuuDichVuTheoLoai" .` truoc khi xoa chi thay 2 file dinh nghia va prompt deprecated. | Thap, module dang dung `SP_TraCuuDichVu`. | PASS |
| 5 | `Database/04_procedures/QuanLyLoaiDV/SP_TraCuuDichVu.sql` | Khong co object, chi `PROMPT` deprecated | Procedure script placeholder | File chi la thong bao deprecated, khong tao procedure/function/trigger. | `Get-Content` xac nhan chi co `PROMPT`; `rg -n -i "SP_TraCuuDichVu.sql|SP_TraCuuDichVu" .` xac nhan canonical o `QuanLyDichVu` van duoc Java goi. | Thap. | PASS |
| 6 | `Database/04_procedures/SP_BaoCaoDoanhThu.sql` | `SP_BaoCaoDoanhThu` | Procedure | Khong co Java/SQL/test caller trong source hien tai. | `rg -n -i "SP_BaoCaoDoanhThu|SP_BaoCaoDoanhThu.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu DBA/manual script ngoai repo con goi. | PASS |
| 7 | `Database/04_procedures/HoaDon/SP_XemChiTietHoaDon.sql` | `SP_XemChiTietHoaDon` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dang query truc tiep. | `rg -n -i "SP_XemChiTietHoaDon|SP_XemChiTietHoaDon.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 8 | `Database/04_procedures/QuanLyDichVu/SP_TraCuuDichVuDaDat.sql` | `sp_TraCuuDichVuDaDat` | Procedure | Khong co Java/SQL/test caller trong source hien tai. | `rg -n -i "SP_TraCuuDichVuDaDat|SP_TraCuuDichVuDaDat.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 9 | `Database/04_procedures/QuanLyKhongGian/SP_CapNhatKhongGian.sql` | `SP_CapNhatKhongGian` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_CapNhatKhongGian|SP_CapNhatKhongGian.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 10 | `Database/04_procedures/QuanLyKhongGian/SP_ThemKhongGian.sql` | `SP_ThemKhongGian` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_ThemKhongGian|SP_ThemKhongGian.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 11 | `Database/04_procedures/QuanLyKhongGian/SP_TraCuuKhongGian.sql` | `SP_TraCuuKhongGian` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_TraCuuKhongGian|SP_TraCuuKhongGian.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 12 | `Database/04_procedures/QuanLyKhongGian/SP_VoHieuHoaKhongGian.sql` | `SP_VoHieuHoaKhongGian` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_VoHieuHoaKhongGian|SP_VoHieuHoaKhongGian.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 13 | `Database/04_procedures/QuanLyLoaiDV/SP_CapNhatLoaiDichVu.sql` | `SP_CapNhatLoaiDichVu` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_CapNhatLoaiDichVu|SP_CapNhatLoaiDichVu.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 14 | `Database/04_procedures/QuanLyLoaiDV/SP_CapNhatTrangThaiDichVu.sql` | `SP_CapNhatTrangThaiDichVu` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_CapNhatTrangThaiDichVu|SP_CapNhatTrangThaiDichVu.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 15 | `Database/04_procedures/QuanLyLoaiDV/SP_ThemLoaiDichVu.sql` | `SP_ThemLoaiDichVu` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_ThemLoaiDichVu|SP_ThemLoaiDichVu.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 16 | `Database/04_procedures/QuanLyNguoiDung/SP_CapNhatKhachHang.sql` | `SP_CapNhatKhachHang` | Procedure | Khong co Java/SQL/test caller trong source hien tai. | `rg -n -i "SP_CapNhatKhachHang|SP_CapNhatKhachHang.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 17 | `Database/04_procedures/QuanLyNguoiDung/SP_DangKy.sql` | `SP_DangKy` | Procedure | Khong co Java/SQL/test caller trong source hien tai. | `rg -n -i "SP_DangKy|SP_DangKy.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 18 | `Database/04_procedures/QuanLyNguoiDung/SP_ThemKhachHang.sql` | `SP_ThemKhachHang` | Procedure | Khong co Java/SQL/test caller trong source hien tai. | `rg -n -i "SP_ThemKhachHang|SP_ThemKhachHang.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 19 | `Database/04_procedures/QuanLyNguoiDung/SP_TraCuuHoiVien.sql` | `SP_TraCuuHoiVien` | Procedure | Khong co Java/SQL/test caller trong source hien tai. | `rg -n -i "SP_TraCuuHoiVien|SP_TraCuuHoiVien.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 20 | `Database/04_procedures/QuanLyNguoiDung/SP_XoaKhachHang.sql` | `SP_XoaKhachHang` | Procedure | Khong co Java/SQL/test caller trong source hien tai. | `rg -n -i "SP_XoaKhachHang|SP_XoaKhachHang.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 21 | `Database/04_procedures/QuanLyPGG/SP_CapNhatPhieuGiamGia.sql` | `SP_CapNhatPhieuGiamGia` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_CapNhatPhieuGiamGia|SP_CapNhatPhieuGiamGia.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 22 | `Database/04_procedures/QuanLyPGG/SP_ThemPhieuGiamGia.sql` | `SP_ThemPhieuGiamGia` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_ThemPhieuGiamGia|SP_ThemPhieuGiamGia.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 23 | `Database/04_procedures/QuanLyPGG/SP_XoaPhieuGiamGia.sql` | `SP_XoaPhieuGiamGia` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung direct SQL. | `rg -n -i "SP_XoaPhieuGiamGia|SP_XoaPhieuGiamGia.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 24 | `Database/04_procedures/QuanLyPhien/sp_PhatSinhMaQR.sql` | `pro_SinhMaQR` | Procedure | Khong co Java/SQL/test caller trong source hien tai; QR dang duoc tao boi Java/trigger luong dat cho. | `rg -n -i "pro_SinhMaQR|sp_PhatSinhMaQR|sp_PhatSinhMaQR.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 25 | `Database/04_procedures/QuanLyPhien/SP_TraCuuPhienLamViec.sql` | `sp_TraCuuPhienLamViec` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung query truc tiep. | `rg -n -i "SP_TraCuuPhienLamViec|SP_TraCuuPhienLamViec.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 26 | `Database/04_procedures/QuanLyPhien/SP_XemChiTietPhienLamViec.sql` | `sp_XemChiTietPhienLamViec` | Procedure | Khong co Java/SQL/test caller trong source hien tai; DAO dung query truc tiep. | `rg -n -i "SP_XemChiTietPhienLamViec|SP_XemChiTietPhienLamViec.sql" .` truoc khi xoa chi thay chinh file dinh nghia. | Thap den trung binh neu co caller ngoai repo. | PASS |
| 27 | `Database/05_triggers/DatCho/TRG_KiemTraQRDattruoc.sql` | `TRG_KiemTraVeDatCho` | Trigger | Trung object va noi dung voi `TRG_KiemTraVeDatTruoc.sql`. | `Get-Content` xac nhan noi dung trung; `rg -n -i "TRG_KiemTraVeDatCho|TRG_KiemTraQRDattruoc.sql" .` sau xoa chi con ban canonical. | Thap, vi trigger canonical van duoc giu. | PASS |
| 28 | `Database/05_triggers/DatCho/TRG_DATCHO_CHINHANH_HOATDONG.sql` | `TRG_DATCHO_CHINHANH_HOATDONG` | Trigger | Trung nghiep vu voi `TRG_DC_CN_HOATDONG`, ban canonical xu ly `NULL` va `NO_DATA_FOUND` ro hon. | `Get-Content` so sanh logic; `rg -n -i "TRG_DATCHO_CHINHANH_HOATDONG|TRG_DC_CN_HOATDONG" .` sau xoa chi con trigger canonical. | Thap, vi trigger canonical van duoc giu. | PASS |
| 29 | `Database/05_triggers/DatCho/TRG_HD_NV_CUNG_CN.sql` | `TRG_HD_NV_CUNG_CN` | Trigger | Trung nghiep vu voi `TRG_HOADON_NV_CUNG_CHINHANH`, giu ban dung module `ThanhToanTrucTiep`. | `Get-Content` so sanh logic; `rg -n -i "TRG_HD_NV_CUNG_CN|TRG_HOADON_NV_CUNG_CHINHANH" .` sau xoa chi con trigger canonical. | Thap, vi trigger canonical van duoc giu. | PASS |

## Trigger object da xoa trong file giu lai

| File | Object da xoa | Object giu lai | Ly do |
|------|---------------|----------------|-------|
| `Database/05_triggers/QuanLyPGG/TRG_CapNhatSLDaDungPhieuGiamGia.sql` | `TRG_CapNhatSLDaDungPhieuGiamGia` | `TRG_CapNhatSLDaDungPGG_DatCho` | Trigger dau chi `NULL` va comment noi luot dung da duoc cong trong `SP_ThanhToanVoiPhieuGiamGia`; trigger dat cho van can tu chay khi `DATCHO.TrangThaiDatTruoc` doi sang da thanh toan. |

## Function giu lai

- `Database/03_function/FN_TinhTienKhongGian.sql`: duoc `TRG_TaoHoaDonKhiMoPhien` va DAO hoa don goi.
- `Database/03_function/FN_TinhTienDichVu.sql`: duoc `FN_TinhTongTien`, `FN_TinhThanhTien` va DAO hoa don goi.
- `Database/03_function/FN_TinhTongTien.sql`: duoc `SP_KetThucPhien`, `SP_ThanhToanVoiPhieuGiamGia`, trigger tinh hoa don va nhieu DAO goi.
- `Database/03_function/FN_TinhThanhTien.sql`: duoc `TRG_TinhToanHoaDon` goi.
- `Database/03_function/FN_KiemTraPhieuGiamGiaHopLe.sql`: duoc `SP_ThanhToanVoiPhieuGiamGia` goi.

Lenh kiem tra: `rg -n -i "FN_TinhTienKhongGian|FN_TinhTienDichVu|FN_TinhTongTien|FN_TinhThanhTien|FN_KiemTraPhieuGiamGiaHopLe" .`

## Trigger giu lai du khong co caller truc tiep

Trigger tu chay theo DML nen khong xoa chi vi Java khong goi truc tiep. Cac trigger nghiep vu duoc giu lai gom:

- `TRG_TaoHoaDonKhiMoPhien`
- `TRG_TinhToanHoaDon`
- `TRG_CapNhatTrangThaiKhongGian`
- `TRG_KiemTraTruocMoPhien`
- `TRG_HuyHieuLucVeCheckIn`
- `TRG_VoHieuQRSauNhanCho`
- `TRG_TichLuyChiTieuDatCho`
- `TRG_TichLuyChiTieuKhachHang`
- `TRG_TuDongThangHang`
- `TRG_ChanThemDichVu_PhienDong`
- `TRG_KiemTraDichVu`
- `TRG_KiemTraPhieuGiamGia`
- `TRG_KiemTraPhuongThucThanhToan`
- `TRG_TaoMaHoaDon`
- `TRG_KiemTraPhienTruocThanhToan`
- `TRG_CapNhatSLDaDungPGG_DatCho`
- `TRG_HOADON_NV_CUNG_CHINHANH`
- `TRG_DC_CN_HOATDONG`
- `TRG_KiemTraVeDatCho`

## Procedure nghi ngo du nhung chua xoa

- `Database/04_procedures/QuanLyDichVu/SP_ThemChiTietDichVu.sql`: `rg -n -i "SP_ThemChiTietDichVu|sp_ThemChiTietDichVu" .` chi thay file dinh nghia, nhung file co `CREATE OR REPLACE PROCEDURE` hop le. Dieu kien xoa trong yeu cau A8 la file rong/binary/khong co `CREATE PROCEDURE`, nen tam giu de tranh xoa qua tay ngoai danh sach ung vien B.

## File/path tham chieu da cap nhat

- `src/test/java/com/wms/util/DaTraTruocTest.java`: doi compile DB object sang `Database/04_procedures/QuanLyPhien/SP_KetThucPhien.sql` va `Database/04_procedures/QuanLyPGG/sp_ThanhToanVoiPGG.sql`.
- `src/main/java/com/wms/util/DbQueryTest.java`: doi path sang `Database/04_procedures/QuanLyPGG/sp_ThanhToanVoiPGG.sql`.
- Khong tim thay README/script huong dan nao khac con nhac file da xoa.

## Kiem tra sau cleanup

- `rg -n -i "CREATE OR REPLACE PROCEDURE|CREATE OR REPLACE FUNCTION|CREATE OR REPLACE TRIGGER" Database/03_function Database/04_procedures Database/05_triggers`: xac nhan cac object canonical con lai.
- `rg -n -i "<cac object/file da xoa>" .`: sau xoa khong con caller noi bo cho cac object/file du thua; cac match con lai la object canonical duoc giu.
- `mvn clean compile`: BUILD SUCCESS.
- `mvn test`: BUILD SUCCESS; 60 tests, 0 failures, 0 errors, 1 skipped.

## Rollback

Co the khoi phuc tung file bang:

```bash
git restore <duong-dan-file>
```

Hoac lay lai tu nhanh `main`:

```bash
git checkout main -- <duong-dan-file>
```

Vi du:

```bash
git restore Database/04_procedures/sp_ThanhToanVoiPGG.sql
git checkout main -- Database/05_triggers/DatCho/TRG_HD_NV_CUNG_CN.sql
```
