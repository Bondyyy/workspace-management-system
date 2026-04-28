CREATE OR REPLACE TRIGGER TRG_CapNhatHoaDonKhiKetThucPhien
AFTER UPDATE OF TrangThaiPhien ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.TrangThaiPhien = 'Đã kết thúc' AND OLD.TrangThaiPhien != 'Đã kết thúc')
BEGIN
    -- Cập nhật lại hóa đơn để trigger TRG_TinhToanHoaDon tính toán lại
    -- TongTien và ThanhTien dựa trên thời gian kết thúc thực tế
    UPDATE HOADON
    SET NgayLapHoaDon = :NEW.ThoiGianKetThuc
    WHERE MaPhien = :NEW.MaPhien;
END TRG_CapNhatHoaDonKhiKetThucPhien;
/
