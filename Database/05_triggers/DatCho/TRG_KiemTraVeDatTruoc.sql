CREATE OR REPLACE TRIGGER TRG_KiemTraVeDatCho
BEFORE INSERT ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.MaDatCho IS NOT NULL)
DECLARE
    v_TrangThai VARCHAR2(50);
    v_ThoiGianToi TIMESTAMP;
    v_khoangthoigiansudung number;
BEGIN
    BEGIN
        SELECT TrangThaiDatTruoc, ThoiGianDuKienToi, KhoangThoiGianSuDung
        INTO v_TrangThai, v_ThoiGianToi, v_khoangthoigiansudung
        FROM DATCHO
        WHERE MaDatCho = :NEW.MaDatCho;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20002, 'Lỗi: Mã vé không tồn tại hoặc làm giả!');
    END;

    IF v_TrangThai = 'Đã sử dụng' THEN
        RAISE_APPLICATION_ERROR(-20003, 'Lỗi: Vé QR này đã được xài trước đó!');
    ELSIF v_TrangThai NOT IN ('Đã thanh toán thành công', 'Đang chờ thanh toán') THEN
        RAISE_APPLICATION_ERROR(-20004, 'Lỗi: Vé chưa thanh toán hoặc giao dịch thất bại!');
    END IF;

    IF SYSTIMESTAMP > (v_ThoiGianToi + v_khoangthoigiansudung) THEN
        RAISE_APPLICATION_ERROR(-20005, 'Lỗi: Vé đã quá hạn chờ');
    ELSIF SYSTIMESTAMP < (v_ThoiGianToi) AND :NEW.TrangThaiPhien != 'Đã đặt trước' THEN
        RAISE_APPLICATION_ERROR(-20006, 'Lỗi: Quá sớm, chưa đến giờ nhận chỗ hợp lệ!');
    END IF;
END;
/
