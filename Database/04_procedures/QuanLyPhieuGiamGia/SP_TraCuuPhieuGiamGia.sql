CREATE OR REPLACE PROCEDURE SP_TraCuuPhieuGiamGia(
    p_TuKhoa IN VARCHAR2,
    p_TrangThai IN VARCHAR2,
    p_outCursor OUT SYS_REFCURSOR,
    p_outMessage OUT VARCHAR2
) AS
    v_TuKhoa VARCHAR2(200);
BEGIN
    IF p_TuKhoa IS NOT NULL AND TRIM(p_TuKhoa) IS NOT NULL THEN
        v_TuKhoa := '%' || UPPER(TRIM(p_TuKhoa)) || '%';
    END IF;

    OPEN p_outCursor FOR
        SELECT MaPGG,
               MaChuSoPGG,
               GiaTriGiamGia,
               GiaTriApDungToiThieu,
               NgayBatDauApDung,
               NgayKetThucApDung,
               SLDaDung,
               SLToiDa,
               NgayTaoPGG,
               MaNV,
               TrangThai
        FROM PHIEUGIAMGIA
        WHERE (v_TuKhoa IS NULL
               OR UPPER(MaPGG) LIKE v_TuKhoa
               OR UPPER(MaChuSoPGG) LIKE v_TuKhoa)
          AND (p_TrangThai IS NULL
               OR TRIM(p_TrangThai) IS NULL
               OR p_TrangThai = 'Tất cả'
               OR TrangThai = p_TrangThai)
        ORDER BY NgayTaoPGG DESC;

    p_outMessage := 'Tra cứu phiếu giảm giá thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi tra cứu phiếu giảm giá: ' || SQLERRM;
        RAISE;
END SP_TraCuuPhieuGiamGia;
/
