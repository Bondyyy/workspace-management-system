CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuKhachHang
AFTER INSERT OR UPDATE ON HOADON
FOR EACH ROW
DECLARE
    v_MaKH VARCHAR2(50);

    -- Đọc PHIENLAMVIEC trong transaction riêng để tránh ORA-04091
    -- khi HOADON được tạo bởi TRG_TaoHoaDonKhiMoPhien (cùng lúc INSERT phiên).
    FUNCTION lay_ma_kh_tu_phien(p_ma_phien VARCHAR2) RETURN VARCHAR2 IS
        PRAGMA AUTONOMOUS_TRANSACTION;
        v_result PHIENLAMVIEC.MaKH%TYPE;
    BEGIN
        SELECT MaKH
        INTO v_result
        FROM PHIENLAMVIEC
        WHERE MaPhien = p_ma_phien;
        COMMIT;
        RETURN v_result;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            ROLLBACK;
            RETURN NULL;
    END lay_ma_kh_tu_phien;

BEGIN
    IF :NEW.TrangThaiThanhToan IN ('Đã thanh toán thành công', 'Đã thanh toán')
       AND (INSERTING
            OR (UPDATING
                AND NVL(:OLD.TrangThaiThanhToan, '') NOT IN ('Đã thanh toán thành công', 'Đã thanh toán'))) THEN

        v_MaKH := lay_ma_kh_tu_phien(:NEW.MaPhien);

        IF v_MaKH IS NOT NULL THEN
            UPDATE KHACHHANG
            SET TongChiTieu = NVL(TongChiTieu, 0) + NVL(:NEW.ThanhTien, 0) + NVL(:NEW.DaTraTruoc, 0)
            WHERE MaKH = v_MaKH;
        END IF;
    END IF;
END TRG_TichLuyChiTieuKhachHang;
/
