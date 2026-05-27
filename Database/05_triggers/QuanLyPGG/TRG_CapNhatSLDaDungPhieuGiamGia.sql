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
