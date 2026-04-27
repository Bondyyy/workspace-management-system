-- =====================================================================
-- Trigger: TRG_TinhToanHoaDon
-- Mục đích: Tự động tính TongTien và ThanhTien cho hóa đơn
-- Công thức:
--   TongTien = (DonGiaTheoGio * SoGioSuDung) + SUM(DonGiaDichVu * SoLuong)
--   ThanhTien = TongTien - GiaTriGiamGia (hoặc TongTien * (1 - PhanTramGiam))
-- Thời điểm: BEFORE INSERT OR UPDATE
-- =====================================================================

CREATE OR REPLACE TRIGGER TRG_TinhToanHoaDon
BEFORE INSERT OR UPDATE ON HOADON
FOR EACH ROW
DECLARE
    v_DonGiaTheoGio NUMBER(18, 2);
    v_SoGioSuDung NUMBER;
    v_TienKhongGian NUMBER(18, 2);
    v_TienDichVu NUMBER(18, 2);
    v_GiaTriGiamGia NUMBER(18, 2) := 0;
    v_PhanTramGiam NUMBER(5, 2) := 0;
    v_MaKH VARCHAR2(50);
BEGIN
    -- 1. Tính tiền không gian (Dựa trên phiên làm việc)
    BEGIN
        SELECT 
            LKG.DonGiaTheoGio,
            ROUND((EXTRACT(HOUR FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) + 
                   EXTRACT(MINUTE FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) / 60), 2)
        INTO v_DonGiaTheoGio, v_SoGioSuDung
        FROM PHIENLAMVIEC PLV
        JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
        JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
        WHERE PLV.MaPhien = :NEW.MaPhien;
        
        v_TienKhongGian := v_DonGiaTheoGio * v_SoGioSuDung;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_TienKhongGian := 0;
    END;
    
    -- 2. Tính tổng tiền dịch vụ
    BEGIN
        SELECT NVL(SUM(DV.DonGia * CTDV.SoLuong), 0)
        INTO v_TienDichVu
        FROM CHITIETDICHVU CTDV
        JOIN DICHVU DV ON CTDV.MaDV = DV.MaDV
        WHERE CTDV.MaPhien = :NEW.MaPhien;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_TienDichVu := 0;
    END;
    
    -- 3. Tính tổng tiền
    :NEW.TongTien := v_TienKhongGian + v_TienDichVu;
    
    -- 4. Tính giá trị giảm giá (nếu có phiếu giảm giá)
    IF :NEW.MaPGG IS NOT NULL THEN
        BEGIN
            SELECT GiaTriGiamGia
            INTO v_GiaTriGiamGia
            FROM PHIEUGIAMGIA
            WHERE MaPGG = :NEW.MaPGG;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_GiaTriGiamGia := 0;
        END;
    END IF;
    
    -- 5. Áp dụng phần trăm giảm giá của hạng thành viên (nếu có)
    BEGIN
        SELECT PLV.MaKH INTO v_MaKH
        FROM PHIENLAMVIEC PLV
        WHERE PLV.MaPhien = :NEW.MaPhien;
        
        IF v_MaKH IS NOT NULL THEN
            SELECT NVL(HTV.PhanTramTienGiam, 0)
            INTO v_PhanTramGiam
            FROM KHACHHANG KH
            LEFT JOIN HANGTHANHVIEN HTV ON KH.MaHangThanhVien = HTV.MaHangThanhVien
            WHERE KH.MaKH = v_MaKH;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_PhanTramGiam := 0;
    END;
    
    -- 6. Tính thành tiền cuối cùng
    -- ThanhTien = (TongTien - GiaTriGiamGia) * (1 - PhanTramGiam/100)
    :NEW.ThanhTien := (:NEW.TongTien - v_GiaTriGiamGia) * (1 - v_PhanTramGiam / 100);
    
    -- Đảm bảo ThanhTien không âm
    IF :NEW.ThanhTien < 0 THEN
        :NEW.ThanhTien := 0;
    END IF;
END TRG_TinhToanHoaDon;
/
