CREATE OR REPLACE TRIGGER TRG_KiemTraTruocMoPhien
BEFORE INSERT ON PHIENLAMVIEC
FOR EACH ROW
DECLARE
    v_TrangThaiKG VARCHAR2(50);
BEGIN
    SELECT TrangThaiKG INTO v_TrangThaiKG
    FROM KHONGGIAN
    WHERE MaKG = :NEW.MaKG;

    IF :NEW.TrangThaiPhien = 'Đang hoạt động' AND v_TrangThaiKG != 'Trống' THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'Lỗi: Không gian này hiện đang có người sử dụng hoặc chưa sẵn sàng (Trạng thái: '
            || v_TrangThaiKG || ').'
        );
    END IF;

    IF v_TrangThaiKG = 'Bảo trì' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Không gian này đang được bảo trì, không thể đặt chỗ!');
    END IF;
END;
/
