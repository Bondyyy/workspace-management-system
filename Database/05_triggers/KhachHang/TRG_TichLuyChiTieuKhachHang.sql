CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuKhachHang
AFTER UPDATE OF TrangThaiThanhToan ON HOADON
FOR EACH ROW
DECLARE
    v_MaKH VARCHAR2(50);
BEGIN
    IF NVL(:OLD.TrangThaiThanhToan, ' ') <> 'Đã thanh toán thành công'
       AND :NEW.TrangThaiThanhToan = 'Đã thanh toán thành công'
       AND :NEW.MaPhien IS NOT NULL
       AND NVL(:NEW.ThanhTien, 0) + NVL(:NEW.DaTraTruoc, 0) > 0 THEN

        BEGIN
            SELECT MaKH
            INTO v_MaKH
            FROM PHIENLAMVIEC
            WHERE MaPhien = :NEW.MaPhien;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_MaKH := NULL;
        END;

        IF v_MaKH IS NOT NULL THEN
            UPDATE KHACHHANG
            SET TongChiTieu = NVL(TongChiTieu, 0) + NVL(:NEW.ThanhTien, 0) + NVL(:NEW.DaTraTruoc, 0)
            WHERE MaKH = v_MaKH;
        END IF;
    END IF;
END TRG_TichLuyChiTieuKhachHang;
/
