CREATE OR REPLACE PROCEDURE SP_TraCuuChiNhanh(
    p_TuKhoa IN VARCHAR2,
    p_TrangThai IN VARCHAR2,
    p_outCursor OUT SYS_REFCURSOR,
    p_outMessage OUT VARCHAR2
) AS
BEGIN
    -- Lọc mềm: p_TuKhoa NULL = không lọc; p_TrangThai NULL = tất cả trạng thái
    OPEN p_outCursor FOR
        SELECT CN.MaCN,
               CN.TenCN,
               CN.DiaChi,
               CN.DuongDayNong,
               CN.ThoiGianMoCua,
               CN.ThoiGianDongCua,
               CN.TrangThai,
               NV.MaNV AS MaNV_QuanLy
        FROM CHINHANH CN
        LEFT JOIN NHANVIEN NV ON NV.MaCN = CN.MaCN AND NV.LoaiNV = 'Quản lý'
        WHERE (p_TuKhoa IS NULL
               OR UPPER(CN.TenCN)  LIKE '%' || UPPER(p_TuKhoa) || '%'
               OR UPPER(CN.DiaChi) LIKE '%' || UPPER(p_TuKhoa) || '%')
          AND (p_TrangThai IS NULL OR CN.TrangThai = p_TrangThai)
        ORDER BY
            CASE
                WHEN REGEXP_LIKE(CN.MaCN, '^CN[0-9]+$')
                THEN TO_NUMBER(REGEXP_SUBSTR(CN.MaCN, '[0-9]+$'))
            END NULLS LAST,
            CN.MaCN;

    p_outMessage := 'Tra cứu chi nhánh thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi tra cứu chi nhánh: ' || SQLERRM;
        RAISE;
END SP_TraCuuChiNhanh;
/
