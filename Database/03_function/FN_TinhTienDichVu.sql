CREATE OR REPLACE FUNCTION FN_TinhTienDichVu(
    p_MaPhien IN VARCHAR2
) RETURN NUMBER
AS
    v_TienDichVu NUMBER(18, 2);
BEGIN
    -- Tính tổng tiền dịch vụ
    SELECT NVL(SUM(DV.DonGia * CTDV.SoLuong), 0)
    INTO v_TienDichVu
    FROM CHITIETDICHVU CTDV
    JOIN DICHVU DV ON CTDV.MaDV = DV.MaDV
    WHERE CTDV.MaPhien = p_MaPhien;
    
    RETURN ROUND(v_TienDichVu, 2);
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END FN_TinhTienDichVu;
/
