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
            RAISE_APPLICATION_ERROR(-20002, 'Lỗi: Mã vé không tồn tại hoặc không hợp lệ.');
    END;

    v_TrangThaiLower := LOWER(v_TrangThai);

    IF v_TrangThaiLower LIKE '%sử%dụng%' OR v_TrangThaiLower LIKE '%su%dung%' THEN
        RAISE_APPLICATION_ERROR(-20003, 'Lỗi: Vé này đã được sử dụng.');
    ELSIF NOT (
        (v_TrangThaiLower LIKE '%thanh%to%' OR v_TrangThaiLower LIKE '%thanh toán%')
        AND v_TrangThaiLower NOT LIKE '%không%'
        AND v_TrangThaiLower NOT LIKE '%khong%'
    ) THEN
        RAISE_APPLICATION_ERROR(-20004, 'Lỗi: Vé chưa thanh toán hoặc giao dịch thất bại.');
    END IF;

    IF LOWER(:NEW.TrangThaiPhien) NOT LIKE '%t tr%' THEN
        IF SYSTIMESTAMP > (v_ThoiGianToi + NUMTODSINTERVAL(NVL(v_KhoangThoiGianSuDung, 1), 'HOUR')) THEN
            RAISE_APPLICATION_ERROR(-20005, 'Lỗi: Vé đã quá hạn chờ.');
        ELSIF SYSTIMESTAMP < v_ThoiGianToi THEN
            RAISE_APPLICATION_ERROR(-20006, 'Lỗi: Quá sớm, chưa đến giờ nhận chỗ hợp lệ.');
        END IF;
    END IF;
END;
/
