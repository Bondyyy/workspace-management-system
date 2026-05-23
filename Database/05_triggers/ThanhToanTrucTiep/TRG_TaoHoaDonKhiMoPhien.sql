CREATE OR REPLACE TRIGGER TRG_TaoHoaDonKhiMoPhien
AFTER INSERT ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.TrangThaiPhien = 'Đang hoạt động')
DECLARE
BEGIN
    INSERT INTO HOADON (
        TongTien,
        ThanhTien,
        NgayLapHoaDon,
        TrangThaiThanhToan,
        MaPhien,
        MaNV
    ) VALUES (
        0,
        0,
        CURRENT_TIMESTAMP,
        'Đang chờ thanh toán',
        :NEW.MaPhien,
        NULL
    );
END TRG_TaoHoaDonKhiMoPhien;
/
