-- =====================================================================
-- Trigger: TRG_TaoMaHoaDon
-- Mục đích: Tự động sinh mã hóa đơn (SoHD) khi tạo hóa đơn mới
-- Thời điểm: BEFORE INSERT
-- =====================================================================

CREATE OR REPLACE TRIGGER TRG_TaoMaHoaDon
BEFORE INSERT ON HOADON
FOR EACH ROW
DECLARE
    v_SoThuTu NUMBER;
    v_NgayHienTai VARCHAR2(8);
BEGIN
    -- Lấy ngày hiện tại theo format YYYYMMDD
    v_NgayHienTai := TO_CHAR(SYSDATE, 'YYYYMMDD');
    
    -- Đếm số hóa đơn trong ngày
    SELECT NVL(COUNT(*), 0) + 1
    INTO v_SoThuTu
    FROM HOADON
    WHERE TO_CHAR(NgayLapHoaDon, 'YYYYMMDD') = v_NgayHienTai;
    
    -- Tạo SoHD theo format: HD-YYYYMMDD-XXXX
    :NEW.SoHD := 'HD-' || v_NgayHienTai || '-' || LPAD(v_SoThuTu, 4, '0');
    
    -- Tự động gán NgayLapHoaDon nếu NULL
    IF :NEW.NgayLapHoaDon IS NULL THEN
        :NEW.NgayLapHoaDon := SYSTIMESTAMP;
    END IF;
    
    -- Mặc định trạng thái thanh toán
    IF :NEW.TrangThaiThanhToan IS NULL THEN
        :NEW.TrangThaiThanhToan := 'Đang chờ thanh toán';
    END IF;
END TRG_TaoMaHoaDon;
/
