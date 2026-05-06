CREATE OR REPLACE TRIGGER TRG_TaoHoaDonKhiMoPhien
AFTER INSERT ON PHIENLAMVIEC
FOR EACH ROW
-- Chỉ tự động tạo hóa đơn khi mở phiên trực tiếp (Đang hoạt động)
-- Đối với khách đặt trước, hóa đơn sẽ được xử lý bởi code ứng dụng hoặc procedure thanh toán
WHEN (NEW.TrangThaiPhien = 'Đang hoạt động')
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
        0,  -- Sẽ được cập nhật khi kết thúc phiên
        0,  -- Sẽ được cập nhật khi kết thúc phiên
        SYSTIMESTAMP,
        'Đang chờ thanh toán',
        :NEW.MaPhien,
        NULL
    );
END TRG_TaoHoaDonKhiMoPhien;
/
