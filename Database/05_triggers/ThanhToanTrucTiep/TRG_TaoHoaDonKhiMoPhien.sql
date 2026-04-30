CREATE OR REPLACE TRIGGER TRG_TaoHoaDonKhiMoPhien
AFTER INSERT ON PHIENLAMVIEC
FOR EACH ROW
DECLARE
    v_MaHoaDon VARCHAR2(50);
BEGIN
    -- Tạo mã hóa đơn duy nhất
    v_MaHoaDon := 'HD-' || :NEW.MaPhien;
    
    -- Tạo hóa đơn mới với trạng thái "Đang chờ thanh toán"
    INSERT INTO HOADON (
        MaHoaDon,
        TongTien,
        ThanhTien,
        NgayLapHoaDon,
        TrangThaiThanhToan,
        MaPhien,
        MaNV
    ) VALUES (
        v_MaHoaDon,
        0,  -- TongTien sẽ được tính lại bởi trigger TRG_TinhToanHoaDon
        0,  -- ThanhTien sẽ được tính lại bởi trigger TRG_TinhToanHoaDon
        SYSTIMESTAMP,
        'Đang chờ thanh toán',
        :NEW.MaPhien,
        NULL  -- MaNV sẽ được cập nhật sau khi lễ tân xác nhận thanh toán
    );
END TRG_TaoHoaDonKhiMoPhien;
/
