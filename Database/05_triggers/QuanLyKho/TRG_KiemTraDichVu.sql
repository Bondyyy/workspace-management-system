CREATE OR REPLACE TRIGGER TRG_KiemTraDichVu
BEFORE INSERT OR UPDATE ON DICHVU
FOR EACH ROW
DECLARE
    v_TenLoai NVARCHAR2(100);
BEGIN
    SELECT TenLoaiDV INTO v_TenLoai FROM LOAIDICHVU WHERE MaLoaiDV = :NEW.MaLoaiDV;

    IF (UPPER(v_TenLoai) NOT LIKE UPPER('%đồ ăn%')
        AND UPPER(v_TenLoai) NOT LIKE UPPER('%thức uống%'))
    THEN
        :NEW.SoLuong := NULL;
    ELSE
        IF :NEW.SoLuong IS NULL THEN
            :NEW.SoLuong := 0;
        END IF;
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        :NEW.SoLuong := NULL;
END;
/