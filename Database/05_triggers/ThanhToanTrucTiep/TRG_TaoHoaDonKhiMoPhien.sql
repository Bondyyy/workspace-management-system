CREATE OR REPLACE TRIGGER TRG_TaoHoaDonKhiMoPhien
AFTER INSERT ON PHIENLAMVIEC
FOR EACH ROW
WHEN (NEW.TrangThaiPhien = 'Đang hoạt động')
DECLARE
    v_DaTraTruoc NUMBER(18, 2) := 0;
    v_TongTienGoc NUMBER(18, 2) := 0;
    v_TienGocDatTruoc NUMBER(18, 2) := 0;
    v_MaPGGDatTruoc DATCHO.MaPGG%TYPE;
    v_TienGiamVoucherDatTruoc NUMBER(18, 2) := 0;
    v_PhanTramGiamHangTVDatTruoc NUMBER(5, 2) := 0;
    v_TienGiamHangTVDatTruoc NUMBER(18, 2) := 0;
    v_TongTienGiam NUMBER(18, 2) := 0;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE := 'Đang chờ thanh toán';
    v_PhuongThucThanhToan HOADON.PhuongThucThanhToan%TYPE := NULL;
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
                   NVL(NULLIF(TongTienGoc, 0), 0),
                   NVL(NULLIF(ThanhTienSauGiam, 0), NVL(ThanhTien, 0)),
                   MaPGG,
                   NVL(TienGiamVoucher, 0),
                   NVL(PhanTramGiamHangTV, 0),
                   NVL(TienGiamHangTV, 0)
            INTO v_TrangThaiDatTruoc,
                 v_TienGocDatTruoc,
                 v_DaTraTruoc,
                 v_MaPGGDatTruoc,
                 v_TienGiamVoucherDatTruoc,
                 v_PhanTramGiamHangTVDatTruoc,
                 v_TienGiamHangTVDatTruoc
            FROM DATCHO
            WHERE MaDatCho = :NEW.MaDatCho;

            IF NVL(v_TienGocDatTruoc, 0) = 0 THEN
                SELECT NVL(LKG.DonGiaTheoGio, 0) * NVL(DC.KhoangThoiGianSuDung, 0)
                INTO v_TienGocDatTruoc
                FROM DATCHO DC
                JOIN KHONGGIAN KG ON DC.MaKG = KG.MaKG
                JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
                WHERE DC.MaDatCho = :NEW.MaDatCho;
            END IF;

            v_TongTienGoc := GREATEST(0, NVL(v_TienGocDatTruoc, 0));
            v_TongTienGiam := GREATEST(0, NVL(v_TienGiamVoucherDatTruoc, 0))
                + GREATEST(0, NVL(v_TienGiamHangTVDatTruoc, 0));

            IF v_TrangThaiDatTruoc = 'Đã thanh toán thành công' THEN
                v_TrangThaiThanhToan := 'Đã trả trước';
                v_PhuongThucThanhToan := 'Đặt trước';
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
        TongTienGoc,
        TienGocDatTruoc,
        TienGocPhatSinh,
        MaPGGDatTruoc,
        TienGiamVoucherDatTruoc,
        PhanTramGiamHangTVDatTruoc,
        TienGiamHangTVDatTruoc,
        TongTienGiam,
        NgayLapHoaDon,
        TrangThaiThanhToan,
        PhuongThucThanhToan,
        MaPhien,
        MaNV
    ) VALUES (
        v_DaTraTruoc,
        v_TongTienGoc,
        0,
        v_TongTienGoc,
        v_TienGocDatTruoc,
        0,
        v_MaPGGDatTruoc,
        v_TienGiamVoucherDatTruoc,
        v_PhanTramGiamHangTVDatTruoc,
        v_TienGiamHangTVDatTruoc,
        v_TongTienGiam,
        CURRENT_TIMESTAMP,
        v_TrangThaiThanhToan,
        v_PhuongThucThanhToan,
        :NEW.MaPhien,
        NULL
    );
END TRG_TaoHoaDonKhiMoPhien;
/
