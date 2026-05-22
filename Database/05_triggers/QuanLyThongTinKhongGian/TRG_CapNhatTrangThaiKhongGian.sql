CREATE OR REPLACE TRIGGER trg_CapNhatTrangThaiKhongGian
AFTER INSERT OR UPDATE ON PHIENLAMVIEC
FOR EACH ROW
BEGIN
    IF (INSERTING AND :NEW.TrangThaiPhien = 'Đang hoạt động')
       OR (UPDATING AND :NEW.TrangThaiPhien = 'Đang hoạt động' AND :OLD.TrangThaiPhien = 'Đã đặt trước') THEN
        UPDATE KHONGGIAN
        SET TrangThaiKG = 'Đang hoạt động'
        WHERE MaKG = :NEW.MaKG;
    END IF;

    IF UPDATING AND :NEW.TrangThaiPhien = 'Đã kết thúc' AND :OLD.TrangThaiPhien != 'Đã kết thúc' THEN
        UPDATE KHONGGIAN
        SET TrangThaiKG = 'Trống'
        WHERE MaKG = :NEW.MaKG;
    END IF;
END;
/
