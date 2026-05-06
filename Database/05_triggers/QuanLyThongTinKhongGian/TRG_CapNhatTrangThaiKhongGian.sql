CREATE OR REPLACE TRIGGER trg_CapNhatTrangThaiKhongGian
AFTER INSERT OR UPDATE ON PHIENLAMVIEC
FOR EACH ROW
BEGIN
    -- 1. Trường hợp có khách mới vào (Bắt đầu phiên)
    IF INSERTING OR (:NEW.TrangThaiPhien = 'Đang hoạt động' AND :OLD.TrangThaiPhien = 'Đã đặt trước') THEN
        UPDATE KHONGGIAN
        SET TrangThaiKG = 'Đang hoạt động'
        WHERE MaKG = :NEW.MaKG;
    END IF;

    -- 2. Trường hợp khách trả bàn (Kết thúc phiên)
    IF UPDATING AND :NEW.TrangThaiPhien = 'Đã kết thúc' AND :OLD.TrangThaiPhien != 'Đã kết thúc' THEN
        UPDATE KHONGGIAN
        SET TrangThaiKG = 'Trống'
        WHERE MaKG = :NEW.MaKG;
    END IF;
END;
/
