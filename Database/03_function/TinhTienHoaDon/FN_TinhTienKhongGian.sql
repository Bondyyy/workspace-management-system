CREATE OR REPLACE FUNCTION FN_TinhTienKhongGian(
    p_MaPhien IN VARCHAR2
) RETURN NUMBER
AS
    v_DonGiaTheoGio NUMBER(18, 2);
    v_ThoiGianBatDau TIMESTAMP;
    v_ThoiGianKetThuc TIMESTAMP;
    v_TongPhut NUMBER;
    v_GioNguyen NUMBER;
    v_PhutLe NUMBER;
    v_SoGioSuDung NUMBER;
    v_TienKhongGian NUMBER(18, 2);
BEGIN
    BEGIN
        SELECT 
            LKG.DonGiaTheoGio,
            PLV.ThoiGianBatDau,
            NVL(PLV.ThoiGianKetThuc, SYSTIMESTAMP) 
        INTO 
            v_DonGiaTheoGio,
            v_ThoiGianBatDau,
            v_ThoiGianKetThuc
        FROM PHIENLAMVIEC PLV
        JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
        JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
        WHERE PLV.MaPhien = p_MaPhien;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 0;
    END;

    v_TongPhut := EXTRACT(DAY FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 24 * 60 +
                  EXTRACT(HOUR FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 60 +
                  EXTRACT(MINUTE FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) +
                  EXTRACT(SECOND FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) / 60;
        
    v_GioNguyen := TRUNC(v_TongPhut / 60);
    v_PhutLe := MOD(v_TongPhut, 60);

    IF v_PhutLe <= 15 THEN
        v_SoGioSuDung := v_GioNguyen;
    ELSE
        v_SoGioSuDung := v_GioNguyen + 1;
    END IF;
    
    v_TienKhongGian := v_DonGiaTheoGio * v_SoGioSuDung;
    
    RETURN ROUND(v_TienKhongGian, 2);
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_TinhTienKhongGian;
/
