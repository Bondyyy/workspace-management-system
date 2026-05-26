CREATE OR REPLACE TRIGGER TRG_TaoHoaDonKhiMoPhien
AFTER INSERT ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.TrangThaiPhien = 'Đang hoạt động')
DECLARE
    v_DaTraTruoc NUMBER(18, 2) := 0;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE := 'Đang chờ thanh toán';
    v_PhuongThucThanhToan HOADON.PhuongThucThanhToan%TYPE := NULL;
    v_TongTien NUMBER(18, 2) := 0;
    v_ThanhTien NUMBER(18, 2) := 0;
    v_TrangThaiDatTruoc DATCHO.TrangThaiDatTruoc%TYPE;
    v_SoHoaDon NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_SoHoaDon
    FROM HOADON
    WHERE MaPhien = :NEW.MaPhien;

    IF v_SoHoaDon > 0 THEN
        RETURN;
    END IF;

    IF :NEW.MaDatCho IS NOT NULL THEN
        BEGIN
            SELECT TrangThaiDatTruoc,
                   NVL(ThanhTien, 0)
            INTO v_TrangThaiDatTruoc,
                 v_DaTraTruoc
            FROM DATCHO
            WHERE MaDatCho = :NEW.MaDatCho;

            v_TongTien := v_DaTraTruoc;

            IF v_TrangThaiDatTruoc = 'Đã thanh toán thành công' THEN
                v_TrangThaiThanhToan := 'Đã trả trước';
                v_PhuongThucThanhToan := 'Đặt trước';
                v_ThanhTien := 0;
            ELSE
                v_ThanhTien := v_DaTraTruoc;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL;
        END;
    END IF;

    INSERT INTO HOADON (
        DaTraTruoc,
        TongTien,
        ThanhTien,
        NgayLapHoaDon,
        TrangThaiThanhToan,
        PhuongThucThanhToan,
        MaPhien,
        MaNV
    ) VALUES (
        v_DaTraTruoc,
        v_TongTien,
        v_ThanhTien,
        CURRENT_TIMESTAMP,
        v_TrangThaiThanhToan,
        v_PhuongThucThanhToan,
        :NEW.MaPhien,
        NULL
    );
END TRG_TaoHoaDonKhiMoPhien;
/
