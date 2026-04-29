CREATE OR REPLACE TRIGGER TRG_CapNhatHoaDonKhiKetThucPhien
AFTER UPDATE OF TrangThaiPhien ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.TrangThaiPhien = 'Đã kết thúc' AND OLD.TrangThaiPhien != 'Đã kết thúc')
BEGIN
    UPDATE HOADON
    SET NgayLapHoaDon = :NEW.ThoiGianKetThuc
    WHERE MaPhien = :NEW.MaPhien;
END TRG_CapNhatHoaDonKhiKetThucPhien;
/
