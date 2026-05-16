CREATE OR REPLACE TRIGGER TRG_KiemTraDichVu
BEFORE INSERT OR UPDATE ON DICHVU
FOR EACH ROW
DECLARE
    v_TenLoai NVARCHAR2(100);
BEGIN
    SELECT TenLoaiDV INTO v_TenLoai FROM LOAIDICHVU WHERE MaLoaiDV = :NEW.MaLoaiDV;

    -- Chỉ loại "Tiện ích" mới không quản lý số lượng tồn kho
    -- Tất cả loại khác (đồ ăn, bim bim, thức uống, v.v.) đều được phép có SoLuong
    IF LOWER(v_TenLoai) LIKE '%tiện ích%' THEN
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