CREATE OR REPLACE PROCEDURE SP_LayDanhSachKho (
    p_Keyword IN VARCHAR2,
    p_Cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_Cursor FOR
        SELECT
            d.MaDV,
            d.TenDV,
            l.TenLoaiDV AS LoaiDV,
            d.DonGia,
            d.SoLuong,
            d.TrangThaiDV
        FROM DICHVU d
        JOIN LOAIDICHVU l ON d.MaLoaiDV = l.MaLoaiDV
        WHERE (p_Keyword IS NULL
               OR LOWER(d.TenDV) LIKE '%' || LOWER(p_Keyword) || '%'
               OR LOWER(d.MaDV) LIKE '%' || LOWER(p_Keyword) || '%')
        ORDER BY
            CASE WHEN d.TenDV = 'Thuê thêm giờ' THEN 0 ELSE 1 END ASC,
            d.TenDV ASC;
END;