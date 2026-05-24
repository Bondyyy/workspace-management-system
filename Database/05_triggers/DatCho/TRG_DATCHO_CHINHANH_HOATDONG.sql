CREATE OR REPLACE TRIGGER TRG_DATCHO_CHINHANH_HOATDONG
BEFORE INSERT OR UPDATE OF MaKG ON DATCHO
FOR EACH ROW
DECLARE
    v_TrangThai CHINHANH.TrangThai%TYPE;
BEGIN
    SELECT cn.TrangThai
    INTO v_TrangThai
    FROM KHONGGIAN kg
    JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
    WHERE kg.MaKG = :NEW.MaKG;

    IF v_TrangThai <> 'Đang hoạt động' THEN
        RAISE_APPLICATION_ERROR(-20201, 'Không thể đặt chỗ tại chi nhánh không hoạt động.');
    END IF;
END;
/