CREATE OR REPLACE TRIGGER TRG_TaoHoaDonKhiMoPhien
AFTER INSERT ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.TrangThaiPhien = 'Đang hoạt động')
DECLARE
    v_MaHoaDon VARCHAR2(50);
BEGIN
    v_MaHoaDon := 'HD-' || :NEW.MaPhien;

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
        0,
        0,
        SYSTIMESTAMP,
        'Đang chờ thanh toán',
        :NEW.MaPhien,
        NULL
    );
END TRG_TaoHoaDonKhiMoPhien;
/
