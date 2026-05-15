CREATE OR REPLACE TRIGGER TRG_KiemTraVeDatCho
BEFORE INSERT ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.MaDatCho IS NOT NULL)
DECLARE
    v_TrangThai VARCHAR2(50);
    v_ThoiGianToi TIMESTAMP;
    v_KhoangThoiGianSuDung NUMBER;
    v_TrangThaiLower VARCHAR2(50);
BEGIN
    BEGIN
        SELECT TrangThaiDatTruoc, ThoiGianDuKienToi, KhoangThoiGianSuDung
        INTO v_TrangThai, v_ThoiGianToi, v_KhoangThoiGianSuDung
        FROM DATCHO
        WHERE MaDatCho = :NEW.MaDatCho;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20002, 'Loi: Ma ve khong ton tai hoac khong hop le.');
    END;

    v_TrangThaiLower := LOWER(v_TrangThai);

    IF v_TrangThaiLower LIKE '%su%dung%' THEN
        RAISE_APPLICATION_ERROR(-20003, 'Loi: Ve nay da duoc su dung.');
    ELSIF NOT (
        v_TrangThaiLower LIKE '%thanh%to%'
        AND v_TrangThaiLower NOT LIKE '%kh%ng%'
    ) THEN
        RAISE_APPLICATION_ERROR(-20004, 'Loi: Ve chua thanh toan hoac giao dich that bai.');
    END IF;

    IF LOWER(:NEW.TrangThaiPhien) NOT LIKE '%t tr%' THEN
        IF SYSTIMESTAMP > (v_ThoiGianToi + NUMTODSINTERVAL(NVL(v_KhoangThoiGianSuDung, 1), 'HOUR')) THEN
            RAISE_APPLICATION_ERROR(-20005, 'Loi: Ve da qua han cho.');
        ELSIF SYSTIMESTAMP < v_ThoiGianToi THEN
            RAISE_APPLICATION_ERROR(-20006, 'Loi: Qua som, chua den gio nhan cho hop le.');
        END IF;
    END IF;
END;
/
