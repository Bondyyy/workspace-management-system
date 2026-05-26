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
    v_MaDatCho VARCHAR2(50);
    v_TienGocDatCho NUMBER(18, 2);
    v_SoGioDatCho NUMBER;
BEGIN
    BEGIN
        SELECT 
            LKG.DonGiaTheoGio,
            PLV.ThoiGianBatDau,
            NVL(PLV.ThoiGianKetThuc, SYSTIMESTAMP),
            PLV.MaDatCho,
            NVL(DC.TongTienGoc, 0),
            NVL(DC.KhoangThoiGianSuDung, 0)
        INTO 
            v_DonGiaTheoGio,
            v_ThoiGianBatDau,
            v_ThoiGianKetThuc,
            v_MaDatCho,
            v_TienGocDatCho,
            v_SoGioDatCho
        FROM PHIENLAMVIEC PLV
        JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
        JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
        LEFT JOIN DATCHO DC ON PLV.MaDatCho = DC.MaDatCho
        WHERE PLV.MaPhien = p_MaPhien;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 0;
    END;

    -- Phiên đặt trước phải trả về tiền gốc thuê không gian, không dùng số đã giảm/đã trả trước.
    IF v_MaDatCho IS NOT NULL THEN
        IF NVL(v_TienGocDatCho, 0) > 0 THEN
            RETURN ROUND(v_TienGocDatCho, 2);
        END IF;
        RETURN ROUND(NVL(v_DonGiaTheoGio, 0) * NVL(v_SoGioDatCho, 0), 2);
    END IF;

    -- Nếu là phiên trực tiếp, tính theo thời gian thực tế
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
