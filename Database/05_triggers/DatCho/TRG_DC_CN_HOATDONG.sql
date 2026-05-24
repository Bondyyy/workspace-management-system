CREATE OR REPLACE TRIGGER TRG_DC_CN_HOATDONG
BEFORE INSERT OR UPDATE OF MaKG ON DATCHO
FOR EACH ROW
DECLARE
    v_TrangThaiCN CHINHANH.TrangThai%TYPE;
BEGIN
    IF :NEW.MaKG IS NULL THEN
        RETURN;
    END IF;

    SELECT cn.TrangThai
    INTO v_TrangThaiCN
    FROM KHONGGIAN kg
    JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
    WHERE kg.MaKG = :NEW.MaKG;

    IF v_TrangThaiCN <> 'Đang hoạt động' THEN
        RAISE_APPLICATION_ERROR(
            -20224,
            'Không thể đặt chỗ tại không gian thuộc chi nhánh không hoạt động.'
        );
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20225,
            'Không tìm thấy không gian hoặc chi nhánh tương ứng khi đặt chỗ.'
        );
END;
/