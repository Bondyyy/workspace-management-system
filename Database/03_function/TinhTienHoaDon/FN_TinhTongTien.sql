CREATE OR REPLACE FUNCTION FN_TinhTongTien(
    p_MaPhien IN VARCHAR2
) RETURN NUMBER
AS
    v_TienKhongGian NUMBER(18, 2);
    v_TienDichVu NUMBER(18, 2);
    v_TongTien NUMBER(18, 2);
BEGIN
    -- Tính tiền không gian
    v_TienKhongGian := FN_TinhTienKhongGian(p_MaPhien);
    
    -- Tính tiền dịch vụ
    v_TienDichVu := FN_TinhTienDichVu(p_MaPhien);
    
    -- Tổng tiền
    v_TongTien := v_TienKhongGian + v_TienDichVu;
    
    RETURN ROUND(v_TongTien, 2);
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_TinhTongTien;
/
