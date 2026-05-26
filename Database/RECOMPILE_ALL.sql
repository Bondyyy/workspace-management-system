-- ============================================================
-- RECOMPILE_ALL.sql - Chay bang F5 trong SQL Developer
-- Thu muc hien tai phai la: d:\DoAnSem4\Sem4\Database\
-- ============================================================

-- BUOC 1: Functions
@@03_function/TinhTienHoaDon/FN_TinhTienDichVu.sql
@@03_function/TinhTienHoaDon/FN_TinhTienKhongGian.sql
@@03_function/TinhTienHoaDon/FN_TinhTongTien.sql
@@03_function/TinhTienHoaDon/FN_TinhThanhTien.sql
@@03_function/FN_KiemTraPhieuGiamGiaHopLe.sql

-- BUOC 2: Triggers
@@05_triggers/ThanhToanTrucTiep/TRG_TaoHoaDonKhiMoPhien.sql

-- BUOC 3: Procedures
@@04_procedures/QuanLyPhien/SP_MoPhienLamViecTrucTiep.sql
@@04_procedures/QuanLyPhien/SP_KetThucPhien.sql
@@04_procedures/sp_ThanhToanVoiPGG.sql
@@04_procedures/HoaDon/SP_XemChiTietHoaDon.sql
@@04_procedures/SP_BaoCaoDoanhThu.sql
@@04_procedures/QuanLyPGG/SP_XoaPhieuGiamGia.sql
@@04_procedures/QuanLyDichVu/SP_TraCuuDichVuDaDat.sql

-- BUOC 4: Kiem tra
SELECT OBJECT_NAME, OBJECT_TYPE, STATUS
FROM USER_OBJECTS
WHERE OBJECT_TYPE IN ('FUNCTION', 'PROCEDURE', 'TRIGGER')
  AND STATUS = 'INVALID'
ORDER BY OBJECT_TYPE, OBJECT_NAME;
