SET SERVEROUTPUT ON

-- Thay bằng một phiên đã có hóa đơn.
DEFINE MA_PHIEN = 'PLV000001'

DECLARE
    v_SoHoaDon NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_SoHoaDon
    FROM HOADON
    WHERE MaPhien = '&&MA_PHIEN';

    IF v_SoHoaDon = 0 THEN
        DBMS_OUTPUT.PUT_LINE('SKIP: phiên &&MA_PHIEN chưa có hóa đơn để test unique constraint.');
        RETURN;
    END IF;

    SAVEPOINT before_duplicate_invoice_test;

    BEGIN
        INSERT INTO HOADON (
            TongTien,
            ThanhTien,
            NgayLapHoaDon,
            TrangThaiThanhToan,
            DaTraTruoc,
            MaPhien
        ) VALUES (
            0,
            0,
            CURRENT_TIMESTAMP,
            'Đang chờ thanh toán',
            0,
            '&&MA_PHIEN'
        );

        ROLLBACK TO before_duplicate_invoice_test;
        RAISE_APPLICATION_ERROR(-20992, 'FAILED: UQ_HOADON_MAPHIEN chưa chặn hóa đơn trùng MaPhien.');
    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            ROLLBACK TO before_duplicate_invoice_test;
            DBMS_OUTPUT.PUT_LINE('OK: UQ_HOADON_MAPHIEN đã chặn hóa đơn trùng MaPhien.');
    END;
END;
/
