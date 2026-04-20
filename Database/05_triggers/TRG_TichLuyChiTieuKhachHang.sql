CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuKhachHang
AFTER INSERT OR UPDATE ON HOADON
FOR EACH ROW
DECLARE
    v_MaKH VARCHAR2(50);
BEGIN
    IF :NEW.TrangThaiThanhToan = 'Thành công' AND
       (INSERTING OR (UPDATING AND NVL(:OLD.TrangThaiThanhToan, '') != 'Thành công')) THEN

        BEGIN
            SELECT MaKH INTO v_MaKH
            FROM PHIENLAMVIEC
            WHERE MaPhien = :NEW.MaPhien;

            IF v_MaKH IS NOT NULL THEN
                UPDATE KHACHHANG
                SET TongChiTieu = NVL(TongChiTieu, 0) + :NEW.ThanhTien
                WHERE MaKH = v_MaKH;
            END IF;

        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL;
        END;
    END IF;
END;
/