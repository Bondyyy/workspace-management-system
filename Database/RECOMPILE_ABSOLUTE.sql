-- ============================================================
-- RECOMPILE_ABSOLUTE.sql
-- Copy noi dung nay, dan vao SQL Developer va bam F5
-- KHONG can thay doi thu muc hien tai
-- ============================================================

@d:\DoAnSem4\Sem4\Database\03_function\TinhTienHoaDon\FN_TinhTienDichVu.sql
@d:\DoAnSem4\Sem4\Database\03_function\TinhTienHoaDon\FN_TinhTienKhongGian.sql
@d:\DoAnSem4\Sem4\Database\03_function\TinhTienHoaDon\FN_TinhTongTien.sql
@d:\DoAnSem4\Sem4\Database\03_function\TinhTienHoaDon\FN_TinhThanhTien.sql
@d:\DoAnSem4\Sem4\Database\03_function\FN_KiemTraPhieuGiamGiaHopLe.sql
@d:\DoAnSem4\Sem4\Database\05_triggers\ThanhToanTrucTiep\TRG_TaoHoaDonKhiMoPhien.sql
@d:\DoAnSem4\Sem4\Database\04_procedures\QuanLyPhien\SP_MoPhienLamViecTrucTiep.sql
@d:\DoAnSem4\Sem4\Database\04_procedures\QuanLyPhien\SP_KetThucPhien.sql
@d:\DoAnSem4\Sem4\Database\04_procedures\sp_ThanhToanVoiPGG.sql
@d:\DoAnSem4\Sem4\Database\04_procedures\HoaDon\SP_XemChiTietHoaDon.sql
@d:\DoAnSem4\Sem4\Database\04_procedures\SP_BaoCaoDoanhThu.sql
@d:\DoAnSem4\Sem4\Database\04_procedures\QuanLyPGG\SP_XoaPhieuGiamGia.sql
@d:\DoAnSem4\Sem4\Database\04_procedures\QuanLyDichVu\SP_TraCuuDichVuDaDat.sql

SELECT OBJECT_NAME, OBJECT_TYPE, STATUS
FROM USER_OBJECTS
WHERE
    OBJECT_TYPE IN (
        'FUNCTION',
        'PROCEDURE',
        'TRIGGER'
    )
    AND STATUS = 'INVALID'
ORDER BY OBJECT_TYPE, OBJECT_NAME;