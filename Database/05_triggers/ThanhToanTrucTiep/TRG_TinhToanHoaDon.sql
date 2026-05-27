CREATE OR REPLACE TRIGGER TRG_TinhToanHoaDon
BEFORE INSERT OR UPDATE ON HOADON
FOR EACH ROW
BEGIN
    -- Chỉ điền số tiền khi chưa được procedure/service tính rõ.
    IF (:NEW.TongTien IS NULL OR :NEW.TongTien = 0) AND :NEW.MaPhien IS NOT NULL THEN
        BEGIN
            EXECUTE IMMEDIATE 'SELECT FN_TinhTongTien(:1) FROM DUAL' INTO :NEW.TongTien USING :NEW.MaPhien;
            EXECUTE IMMEDIATE 'SELECT FN_TinhThanhTien(:1, :2) FROM DUAL' INTO :NEW.ThanhTien USING :NEW.MaPhien, :NEW.MaPGG;
            
            IF (:NEW.TongTien = 0 OR :NEW.TongTien IS NULL) AND :NEW.ThanhTien > 0 THEN
                :NEW.TongTien := :NEW.ThanhTien;
            END IF;
        EXCEPTION
            WHEN OTHERS THEN
                NULL;
        END;
    END IF;

    IF :NEW.TongTien IS NULL THEN
        :NEW.TongTien := 0;
    END IF;

    IF :NEW.ThanhTien IS NULL THEN
        :NEW.ThanhTien := GREATEST(0, NVL(:NEW.TongTien, 0));
    END IF;
END;
/

