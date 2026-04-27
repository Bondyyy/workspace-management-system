CREATE OR REPLACE TRIGGER trg_CapNhatTrangThaiKhongGian
AFTER INSERT OR UPDATE ON PHIENLAMVIEC
FOR EACH ROW
BEGIN
    -- 1. Trường hợp có khách mới vào
    IF INSERTING THEN
        UPDATE KHONGGIAN
        SET TrangThaiKG = 'Hoạt động'
        WHERE MaKG = :NEW.MaKG;
    END IF;

    -- 2. Trường hợp khách trả bàn
    IF UPDATING AND :NEW.TrangThaiPhien = 'Đã kết thúc' THEN
        UPDATE KHONGGIAN
        SET TrangThaiKG = 'Dọn dẹp'
        WHERE MaKG = :NEW.MaKG;
    END IF;
END;
/
