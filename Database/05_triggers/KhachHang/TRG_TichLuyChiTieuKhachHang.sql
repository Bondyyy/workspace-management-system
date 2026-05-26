CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuKhachHang
AFTER UPDATE OF TrangThaiThanhToan ON HOADON
FOR EACH ROW
DECLARE
    v_MaKH VARCHAR2(50);
    v_TienThucTra NUMBER(18, 2);
BEGIN
    IF NVL(:OLD.TrangThaiThanhToan, ' ') <> 'Đã thanh toán thành công'
       AND :NEW.TrangThaiThanhToan = 'Đã thanh toán thành công'
       AND :NEW.MaPhien IS NOT NULL
       AND GREATEST(NVL(:NEW.TongTien, 0), NVL(:NEW.DaTraTruoc, 0)) > 0 THEN

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
            v_TienThucTra := GREATEST(NVL(:NEW.TongTien, 0), NVL(:NEW.DaTraTruoc, 0));
            UPDATE KHACHHANG
            SET TongChiTieu = NVL(TongChiTieu, 0) + v_TienThucTra,
                CapNhatLanCuoi = CURRENT_TIMESTAMP
            WHERE MaKH = v_MaKH;
        END IF;
    END IF;
END TRG_TichLuyChiTieuKhachHang;
/
