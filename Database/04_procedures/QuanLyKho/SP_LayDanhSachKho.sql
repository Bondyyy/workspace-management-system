CREATE OR REPLACE PROCEDURE SP_LayDanhSachKho (
    p_Keyword IN VARCHAR2,
    p_Cursor OUT SYS_REFCURSOR
) AS
    v_Keyword VARCHAR2(200);
BEGIN
    IF p_Keyword IS NOT NULL AND TRIM(p_Keyword) != '' THEN
        v_Keyword := '%' || LOWER(TRIM(p_Keyword)) || '%';
    ELSE
        v_Keyword := '%';
    END IF;

    OPEN p_Cursor FOR
        SELECT
            d.MaDV,
            d.TenDV,
            NVL(l.TenLoaiDV, 'Chưa phân loại') AS LoaiDV,
            d.DonGia,
            d.SoLuong,
            d.TrangThaiDV,
            d.HinhAnh
        FROM DICHVU d
        LEFT JOIN LOAIDICHVU l ON d.MaLoaiDV = l.MaLoaiDV
        WHERE (LOWER(d.TenDV) LIKE v_Keyword
               OR LOWER(d.MaDV) LIKE v_Keyword)
        ORDER BY
            CASE WHEN d.TenDV = 'Thuê thêm giờ' THEN 0 ELSE 1 END ASC,
            d.TenDV ASC;
END;
/
