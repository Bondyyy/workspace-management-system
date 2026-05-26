DECLARE
    PROCEDURE add_column_if_missing(
        p_table_name IN VARCHAR2,
        p_column_name IN VARCHAR2,
        p_column_ddl IN VARCHAR2
    ) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO v_count
        FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = UPPER(p_table_name)
          AND COLUMN_NAME = UPPER(p_column_name);

        IF v_count = 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table_name || ' ADD ' || p_column_ddl;
        END IF;
    END;
BEGIN
    add_column_if_missing('DATCHO', 'TongTienGoc', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('DATCHO', 'MaPGG', 'VARCHAR2(50)');
    add_column_if_missing('DATCHO', 'MaChuSoPGG', 'VARCHAR2(100)');
    add_column_if_missing('DATCHO', 'TienGiamVoucher', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('DATCHO', 'PhanTramGiamHangTV', 'NUMBER(5,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('DATCHO', 'TienGiamHangTV', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('DATCHO', 'ThanhTienSauGiam', 'NUMBER(18,2) DEFAULT 0 NOT NULL');

    add_column_if_missing('HOADON', 'TongTienGoc', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'TienGocDatTruoc', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'TienGocPhatSinh', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'MaPGGDatTruoc', 'VARCHAR2(50)');
    add_column_if_missing('HOADON', 'MaPGGTaiQuay', 'VARCHAR2(50)');
    add_column_if_missing('HOADON', 'TienGiamVoucherDatTruoc', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'TienGiamHangTVDatTruoc', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'PhanTramGiamHangTVDatTruoc', 'NUMBER(5,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'TienGiamVoucherTaiQuay', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'TienGiamHangTVTaiQuay', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'PhanTramGiamHangTVTaiQuay', 'NUMBER(5,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'TongTienGiam', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
    add_column_if_missing('HOADON', 'SoTienThanhToanTaiQuay', 'NUMBER(18,2) DEFAULT 0 NOT NULL');
END;
/

UPDATE DATCHO dc
SET TongTienGoc =
        CASE
            WHEN NVL(TongTienGoc, 0) > 0 THEN TongTienGoc
            ELSE NVL((
                SELECT NVL(lkg.DonGiaTheoGio, 0) * NVL(dc.KhoangThoiGianSuDung, 0)
                FROM KHONGGIAN kg
                JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
                WHERE kg.MaKG = dc.MaKG
            ), NVL(ThanhTien, 0))
        END,
    ThanhTienSauGiam =
        CASE
            WHEN NVL(ThanhTienSauGiam, 0) > 0 THEN ThanhTienSauGiam
            ELSE NVL(ThanhTien, 0)
        END,
    TienGiamVoucher = NVL(TienGiamVoucher, 0),
    PhanTramGiamHangTV = NVL(PhanTramGiamHangTV, 0),
    TienGiamHangTV = NVL(TienGiamHangTV, 0)
WHERE NVL(TongTienGoc, 0) = 0
   OR NVL(ThanhTienSauGiam, 0) = 0
   OR TienGiamVoucher IS NULL
   OR PhanTramGiamHangTV IS NULL
   OR TienGiamHangTV IS NULL;

UPDATE DATCHO
SET ThanhTien = ThanhTienSauGiam
WHERE NVL(ThanhTienSauGiam, 0) > 0
  AND NVL(ThanhTien, 0) <> NVL(ThanhTienSauGiam, 0);

MERGE INTO HOADON h
USING (
    SELECT h2.MaHoaDon,
           p.MaDatCho,
           NVL(dc.TongTienGoc, 0) AS DcTongTienGoc,
           NVL(dc.ThanhTienSauGiam, NVL(dc.ThanhTien, 0)) AS DcThanhTienSauGiam,
           dc.MaPGG AS DcMaPGG,
           NVL(dc.TienGiamVoucher, 0) AS DcTienGiamVoucher,
           NVL(dc.PhanTramGiamHangTV, 0) AS DcPhanTramGiamHangTV,
           NVL(dc.TienGiamHangTV, 0) AS DcTienGiamHangTV,
           NVL(h2.TongTien, 0) AS OldTongTien,
           NVL(h2.ThanhTien, 0) AS OldThanhTien,
           NVL(h2.DaTraTruoc, 0) AS OldDaTraTruoc
    FROM HOADON h2
    LEFT JOIN PHIENLAMVIEC p ON p.MaPhien = h2.MaPhien
    LEFT JOIN DATCHO dc ON dc.MaDatCho = p.MaDatCho
) src
ON (h.MaHoaDon = src.MaHoaDon)
WHEN MATCHED THEN UPDATE SET
    h.TienGocDatTruoc = CASE
        WHEN src.MaDatCho IS NOT NULL THEN src.DcTongTienGoc
        ELSE NVL(h.TienGocDatTruoc, 0)
    END,
    h.DaTraTruoc = CASE
        WHEN src.MaDatCho IS NOT NULL THEN GREATEST(src.OldDaTraTruoc, src.DcThanhTienSauGiam)
        ELSE NVL(h.DaTraTruoc, 0)
    END,
    h.MaPGGDatTruoc = CASE
        WHEN src.MaDatCho IS NOT NULL THEN COALESCE(h.MaPGGDatTruoc, src.DcMaPGG)
        ELSE h.MaPGGDatTruoc
    END,
    h.TienGiamVoucherDatTruoc = CASE
        WHEN src.MaDatCho IS NOT NULL THEN GREATEST(NVL(h.TienGiamVoucherDatTruoc, 0), src.DcTienGiamVoucher)
        ELSE NVL(h.TienGiamVoucherDatTruoc, 0)
    END,
    h.PhanTramGiamHangTVDatTruoc = CASE
        WHEN src.MaDatCho IS NOT NULL THEN GREATEST(NVL(h.PhanTramGiamHangTVDatTruoc, 0), src.DcPhanTramGiamHangTV)
        ELSE NVL(h.PhanTramGiamHangTVDatTruoc, 0)
    END,
    h.TienGiamHangTVDatTruoc = CASE
        WHEN src.MaDatCho IS NOT NULL THEN GREATEST(NVL(h.TienGiamHangTVDatTruoc, 0), src.DcTienGiamHangTV)
        ELSE NVL(h.TienGiamHangTVDatTruoc, 0)
    END,
    h.TongTienGoc = CASE
        WHEN NVL(h.TongTienGoc, 0) > 0 THEN h.TongTienGoc
        WHEN src.MaDatCho IS NOT NULL THEN src.DcTongTienGoc
        ELSE src.OldTongTien
    END,
    h.SoTienThanhToanTaiQuay = CASE
        WHEN NVL(h.SoTienThanhToanTaiQuay, 0) > 0 THEN h.SoTienThanhToanTaiQuay
        WHEN h.TrangThaiThanhToan = 'Đã thanh toán thành công' THEN GREATEST(0, src.OldThanhTien)
        ELSE 0
    END,
    h.TongTienGiam = CASE
        WHEN NVL(h.TongTienGiam, 0) > 0 THEN h.TongTienGiam
        ELSE GREATEST(0,
            CASE
                WHEN src.MaDatCho IS NOT NULL THEN src.DcTongTienGoc
                ELSE src.OldTongTien
            END - GREATEST(src.OldDaTraTruoc, src.DcThanhTienSauGiam) - src.OldThanhTien)
    END,
    h.TongTien = CASE
        WHEN src.MaDatCho IS NOT NULL THEN src.DcTongTienGoc
        ELSE src.OldTongTien
    END;

COMMIT;
