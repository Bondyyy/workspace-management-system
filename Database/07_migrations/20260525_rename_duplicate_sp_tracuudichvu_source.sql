SET SERVEROUTPUT ON;

PROMPT ===== CREATE RENAMED NON-CANONICAL SERVICE LOOKUP PROCEDURE =====
PROMPT Canonical SP_TraCuuDichVu remains Database/04_procedures/QuanLyDichVu/SP_TraCuuDichVu.sql.
PROMPT This migration creates SP_TraCuuDichVuTheoLoai to avoid source-level deploy overwrite.
PROMPT It does not DROP the old SP_TraCuuDichVu object; DBA confirmation is required before any cleanup.

CREATE OR REPLACE PROCEDURE SP_TraCuuDichVuTheoLoai(
    p_MaLoaiDV IN VARCHAR2,
    p_TrangThaiDV IN VARCHAR2,
    p_TuKhoa IN VARCHAR2,
    p_outCursor OUT SYS_REFCURSOR,
    p_outMessage OUT VARCHAR2
) AS
BEGIN
    OPEN p_outCursor FOR
        SELECT DV.MaDV,
               DV.TenDV,
               DV.DonGia,
               DV.TrangThaiDV,
               DV.HinhAnh,
               DV.MaLoaiDV,
               LDV.TenLoaiDV,
               LDV.TrangThaiLDV,
               DV.SoLuong,
               DV.GiaNhap
        FROM DICHVU DV
        JOIN LOAIDICHVU LDV ON DV.MaLoaiDV = LDV.MaLoaiDV
        WHERE (p_MaLoaiDV IS NULL OR DV.MaLoaiDV = p_MaLoaiDV)
          AND (p_TrangThaiDV IS NULL OR DV.TrangThaiDV = p_TrangThaiDV)
          AND (p_TuKhoa IS NULL
               OR UPPER(DV.TenDV) LIKE '%' || UPPER(p_TuKhoa) || '%')
        ORDER BY LDV.TenLoaiDV, DV.TenDV;

    p_outMessage := 'Tra cứu dịch vụ thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi tra cứu dịch vụ: ' || SQLERRM;
        RAISE;
END SP_TraCuuDichVuTheoLoai;
/

PROMPT ===== NOTE =====
PROMPT Existing Java caller uses SP_TraCuuDichVu with the canonical QuanLyDichVu output.
PROMPT Search Java before wiring SP_TraCuuDichVuTheoLoai into any UI flow.
