CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuKhachHang
AFTER UPDATE OF TrangThaiThanhToan ON HOADON
FOR EACH ROW
DECLARE
    v_MaKH VARCHAR2(50);
    v_MaDatCho VARCHAR2(50);
    v_TienThucTra NUMBER(18, 2);
BEGIN
    IF NVL(:OLD.TrangThaiThanhToan, ' ') <> 'Đã thanh toán thành công'
       AND :NEW.TrangThaiThanhToan = 'Đã thanh toán thành công'
       AND :NEW.MaPhien IS NOT NULL THEN

        BEGIN
            SELECT MaKH, MaDatCho
            INTO v_MaKH, v_MaDatCho
            FROM PHIENLAMVIEC
            WHERE MaPhien = :NEW.MaPhien;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_MaKH := NULL;
                v_MaDatCho := NULL;
        END;

        IF v_MaKH IS NOT NULL THEN
            IF v_MaDatCho IS NOT NULL THEN
                -- Tiền trả trước của DATCHO đã được cộng khi thanh toán web thành công.
                v_TienThucTra := NVL(:NEW.SoTienThanhToanTaiQuay, 0);
            ELSE
                v_TienThucTra := NVL(:NEW.SoTienThanhToanTaiQuay, 0);
                IF v_TienThucTra = 0 THEN
                    v_TienThucTra := NVL(:NEW.ThanhTien, 0);
                END IF;
                IF v_TienThucTra = 0 THEN
                    v_TienThucTra := NVL(:NEW.DaTraTruoc, 0);
                END IF;
            END IF;

            IF NVL(v_TienThucTra, 0) > 0 THEN
                UPDATE KHACHHANG
                SET TongChiTieu = NVL(TongChiTieu, 0) + v_TienThucTra,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaKH = v_MaKH;
            END IF;
        END IF;
    END IF;
END TRG_TichLuyChiTieuKhachHang;
/
