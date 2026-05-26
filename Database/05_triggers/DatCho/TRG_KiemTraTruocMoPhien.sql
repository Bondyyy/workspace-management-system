CREATE OR REPLACE TRIGGER TRG_KiemTraTruocMoPhien
BEFORE INSERT ON PHIENLAMVIEC
FOR EACH ROW
DECLARE
    v_TrangThaiKG VARCHAR2(50);
BEGIN
    SELECT TrangThaiKG INTO v_TrangThaiKG
    FROM KHONGGIAN
    WHERE MaKG = :NEW.MaKG;

    IF v_TrangThaiKG = 'Bảo trì' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Không gian này đang được bảo trì, không thể đặt chỗ!');
    END IF;

    IF :NEW.TrangThaiPhien = 'Đang hoạt động' THEN
        IF :NEW.MaDatCho IS NULL AND v_TrangThaiKG != 'Trống' THEN
            RAISE_APPLICATION_ERROR(
                -20001,
                'Lỗi: Mở phiên trực tiếp chỉ được phép khi không gian đang Trống (Trạng thái: '
                || v_TrangThaiKG || ').'
            );
        ELSIF :NEW.MaDatCho IS NOT NULL AND v_TrangThaiKG NOT IN ('Trống', 'Đã đặt trước', 'Tạm khoá', 'Tạm khóa') THEN
            RAISE_APPLICATION_ERROR(
                -20001,
                'Lỗi: Không gian của đặt chỗ chưa sẵn sàng để nhận khách (Trạng thái: '
                || v_TrangThaiKG || ').'
            );
        END IF;
    END IF;
END;
/
