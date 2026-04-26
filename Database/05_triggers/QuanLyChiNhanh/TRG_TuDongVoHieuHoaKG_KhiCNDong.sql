CREATE OR REPLACE TRIGGER TRG_TuDongVoHieuHoaKG_KhiCNDong
AFTER UPDATE OF TrangThai ON CHINHANH
FOR EACH ROW
WHEN (NEW.TrangThai = 'Ngừng hoạt động' AND OLD.TrangThai = 'Đang hoạt động')
BEGIN
    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Bảo trì'
    WHERE MaCN = :OLD.MaCN
      AND TrangThaiKG != 'Bảo trì';
END TRG_TuDongVoHieuHoaKG_KhiCNDong;
/