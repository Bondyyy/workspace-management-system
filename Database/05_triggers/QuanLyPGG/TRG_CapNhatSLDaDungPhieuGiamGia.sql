CREATE OR REPLACE TRIGGER TRG_CapNhatSLDaDungPhieuGiamGia
AFTER INSERT ON HOADON
FOR EACH ROW
BEGIN
    -- Lượt dùng phiếu giảm giá được cộng trong SP_ThanhToanVoiPhieuGiamGia.
    -- Trigger này giữ lại tên cũ để script triển khai không lỗi, nhưng không cộng lần hai.
    NULL;
END TRG_CapNhatSLDaDungPhieuGiamGia;
/

CREATE OR REPLACE TRIGGER TRG_CapNhatSLDaDungPGG_DatCho
AFTER UPDATE OF TrangThaiDatTruoc ON DATCHO
FOR EACH ROW
WHEN (
    OLD.TrangThaiDatTruoc = 'Đang chờ thanh toán'
    AND NEW.TrangThaiDatTruoc = 'Đã thanh toán thành công'
)
BEGIN
    UPDATE PHIEUGIAMGIA pgg
    SET SLDaDung = NVL(SLDaDung, 0) + 1
    WHERE NVL(SLDaDung, 0) < NVL(SLToiDa, 0)
      AND EXISTS (
          SELECT 1
          FROM CHITIETAPDUNGPGG ct
          WHERE ct.MaDatCho = :NEW.MaDatCho
            AND ct.MaPGG = pgg.MaPGG
            AND ct.NguonApDung = 'DAT_TRUOC'
            AND NVL(ct.SoTienGiam, 0) > 0
      );
END TRG_CapNhatSLDaDungPGG_DatCho;
/
