CREATE OR REPLACE PROCEDURE SP_TraCuuDichVu (
    p_MaLoaiDV    IN VARCHAR2,
    p_TrangThaiDV IN VARCHAR2,
    p_Keyword     IN VARCHAR2,
    p_Cursor      OUT SYS_REFCURSOR,
    p_Message     OUT VARCHAR2
) AS
    v_Keyword VARCHAR2(100);
BEGIN
    -- Xử lý từ khóa tìm kiếm
    IF p_Keyword IS NOT NULL AND TRIM(p_Keyword) != '' THEN
        v_Keyword := '%' || LOWER(TRIM(p_Keyword)) || '%';
    ELSE
        v_Keyword := '%';
    END IF;

    OPEN p_Cursor FOR
    SELECT MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong, GiaGoc
    FROM DICHVU
    WHERE (p_MaLoaiDV IS NULL OR MaLoaiDV = p_MaLoaiDV OR p_MaLoaiDV = '')
      AND (p_TrangThaiDV IS NULL OR TrangThaiDV = p_TrangThaiDV OR p_TrangThaiDV = '')
      AND (LOWER(TenDV) LIKE v_Keyword OR LOWER(MaDV) LIKE v_Keyword)
    ORDER BY MaDV DESC;
    
    p_Message := 'Lấy danh sách dịch vụ thành công';
EXCEPTION
    WHEN OTHERS THEN
        p_Message := 'Lỗi tra cứu: ' || SQLERRM;
END;
/
