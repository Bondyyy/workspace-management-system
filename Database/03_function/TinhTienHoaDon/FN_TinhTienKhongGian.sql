CREATE OR REPLACE FUNCTION FN_TinhTienKhongGian(
    p_MaPhien IN VARCHAR2
) RETURN NUMBER
AS
    v_DonGiaTheoGio NUMBER(18, 2);
    v_ThoiGianBatDau TIMESTAMP;
    v_ThoiGianKetThuc TIMESTAMP;
    v_SoGioSuDung NUMBER;
    v_TienKhongGian NUMBER(18, 2);
BEGIN
    -- Lấy thông tin phiên và loại không gian
    BEGIN
        SELECT 
            LKG.DonGiaTheoGio,
            PLV.ThoiGianBatDau,
            NVL(PLV.ThoiGianKetThuc, SYSTIMESTAMP) -- Nếu chưa kết thúc thì tính đến hiện tại
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
    
    -- Tính số giờ sử dụng (làm tròn đến 2 chữ số thập phân)
    v_SoGioSuDung := ROUND(
        EXTRACT(DAY FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 24 +
        EXTRACT(HOUR FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) +
        EXTRACT(MINUTE FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) / 60 +
        EXTRACT(SECOND FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) / 3600,
        2
    );
    
    -- Tính tiền không gian
    v_TienKhongGian := v_DonGiaTheoGio * v_SoGioSuDung;
    
    RETURN ROUND(v_TienKhongGian, 2);
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_TinhTienKhongGian;
/
