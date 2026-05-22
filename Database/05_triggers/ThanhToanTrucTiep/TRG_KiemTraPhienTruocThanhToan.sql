CREATE OR REPLACE TRIGGER TRG_KiemTraPhienTruocThanhToan
BEFORE UPDATE OF TrangThaiThanhToan, PhuongThucThanhToan ON HOADON
FOR EACH ROW
DECLARE
    v_TrangThaiPhien VARCHAR2(50);
    v_MaPhien VARCHAR2(50);
BEGIN
    IF ((:NEW.TrangThaiThanhToan IN ('Đã thanh toán thành công', 'Đã thanh toán'))
        AND (NVL(:OLD.TrangThaiThanhToan, '') NOT IN ('Đã thanh toán thành công', 'Đã thanh toán')))
       OR
       (:NEW.PhuongThucThanhToan IS NOT NULL AND :OLD.PhuongThucThanhToan IS NULL) THEN

        BEGIN
            SELECT TrangThaiPhien, MaPhien
            INTO v_TrangThaiPhien, v_MaPhien
            FROM PHIENLAMVIEC
            WHERE MaPhien = :NEW.MaPhien;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(
                    -20030,
                    'Lỗi: Không tìm thấy phiên làm việc [' || :NEW.MaPhien || ']!'
                );
        END;

        IF v_TrangThaiPhien != 'Đã kết thúc' THEN
            RAISE_APPLICATION_ERROR(
                -20031,
                'Lỗi: Không thể thanh toán! Phiên làm việc [' || v_MaPhien || '] '
                || 'chưa kết thúc (Trạng thái hiện tại: ' || v_TrangThaiPhien || '). '
                || 'Vui lòng kết thúc phiên trước khi thanh toán!'
            );
        END IF;
    END IF;
END TRG_KiemTraPhienTruocThanhToan;
/
