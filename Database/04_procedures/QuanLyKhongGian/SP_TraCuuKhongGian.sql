CREATE OR REPLACE PROCEDURE SP_TraCuuKhongGian(
    p_MaCN IN VARCHAR2,
    p_MaLoaiKG IN VARCHAR2,
    p_TrangThaiKG IN VARCHAR2,
    p_TuKhoa IN VARCHAR2,
    p_outCursor OUT SYS_REFCURSOR,
    p_outMessage OUT VARCHAR2
) AS
BEGIN
    -- Lọc mềm: p_TuKhoa NULL = không lọc; p_TrangThai NULL = tất cả trạng thái
    OPEN p_outCursor FOR
        SELECT KG.MaKG,
               KG.TenKG,
               KG.TrangThaiKG,
               KG.ViTri,
               KG.MaCN,
               CN.TenCN,
               KG.MaLoaiKG,
               LKG.TenLoaiKG,
               LKG.SucChua,
               LKG.DonGiaTheoGio,
               KG.ToaDoX,
               KG.ToaDoY,
               KG.ChieuDai,
               KG.ChieuRong
        FROM KHONGGIAN KG
        JOIN CHINHANH CN ON KG.MaCN = CN.MaCN
        JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
        WHERE (p_MaCN IS NULL OR KG.MaCN = p_MaCN)
          AND (p_MaLoaiKG IS NULL OR KG.MaLoaiKG = p_MaLoaiKG)
          AND (p_TrangThaiKG IS NULL OR KG.TrangThaiKG = p_TrangThaiKG)
          AND (p_TuKhoa IS NULL
               OR UPPER(KG.TenKG) LIKE '%' || UPPER(p_TuKhoa) || '%')
        ORDER BY CN.TenCN, KG.ToaDoY, KG.ToaDoX, KG.TenKG;

    p_outMessage := 'Tra cứu không gian thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi tra cứu không gian: ' || SQLERRM;
        RAISE;
END SP_TraCuuKhongGian;
/
