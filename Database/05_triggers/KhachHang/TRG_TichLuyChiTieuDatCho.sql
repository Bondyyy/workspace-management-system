CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuDatCho
AFTER UPDATE OF TrangThaiDatTruoc ON DATCHO
FOR EACH ROW
WHEN (
    OLD.TrangThaiDatTruoc = 'Đang chờ thanh toán'
    AND NEW.TrangThaiDatTruoc = 'Đã thanh toán thành công'
)
DECLARE
    v_TienThucTra NUMBER(18, 2);
BEGIN
    v_TienThucTra := NVL(:NEW.ThanhTien, 0);

    IF :NEW.MaKH IS NOT NULL AND NVL(v_TienThucTra, 0) > 0 THEN
        UPDATE KHACHHANG
        SET TongChiTieu = NVL(TongChiTieu, 0) + v_TienThucTra,
            CapNhatLanCuoi = CURRENT_TIMESTAMP
        WHERE MaKH = :NEW.MaKH;
    END IF;
END TRG_TichLuyChiTieuDatCho;
/
