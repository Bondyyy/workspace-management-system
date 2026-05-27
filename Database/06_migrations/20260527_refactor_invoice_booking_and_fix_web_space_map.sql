-- Refactor HOADON/DATCHO ve schema gon va sua luong tinh hoa don tu bang nguon.
-- Chay sau cac script 20260526. Migration nay backup cot cu truoc khi drop.

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM USER_TABLES WHERE TABLE_NAME = 'BACKUP_DATCHO_20260527';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE TABLE BACKUP_DATCHO_20260527 AS SELECT * FROM DATCHO';
    END IF;

    SELECT COUNT(*) INTO v_count FROM USER_TABLES WHERE TABLE_NAME = 'BACKUP_HOADON_20260527';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE TABLE BACKUP_HOADON_20260527 AS SELECT * FROM HOADON';
    END IF;

    SELECT COUNT(*) INTO v_count FROM USER_TABLES WHERE TABLE_NAME = 'CHITIETAPDUNGPGG';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE '
            CREATE TABLE CHITIETAPDUNGPGG (
                MaApDung VARCHAR2(50) PRIMARY KEY,
                MaPGG VARCHAR2(50),
                MaDatCho VARCHAR2(50),
                MaHoaDon VARCHAR2(50),
                NguonApDung VARCHAR2(50),
                SoTienGiam NUMBER(18,2),
                ThoiGianApDung TIMESTAMP
            )';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'DATCHO' AND COLUMN_NAME = 'MAPGG';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE q'[
            INSERT INTO CHITIETAPDUNGPGG (MaApDung, MaPGG, MaDatCho, MaHoaDon, NguonApDung, SoTienGiam, ThoiGianApDung)
            SELECT RAWTOHEX(SYS_GUID()), dc.MaPGG, dc.MaDatCho, NULL, 'DAT_TRUOC',
                   GREATEST(0, NVL(dc.TienGiamVoucher, 0)),
                   NVL(dc.CapNhatLanCuoi, dc.ThoiGianDat)
            FROM DATCHO dc
            WHERE dc.MaPGG IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1 FROM CHITIETAPDUNGPGG ct
                  WHERE ct.MaDatCho = dc.MaDatCho
                    AND ct.MaPGG = dc.MaPGG
                    AND ct.NguonApDung = 'DAT_TRUOC'
              )
        ]';
    END IF;

    SELECT COUNT(*) INTO v_count FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'HOADON' AND COLUMN_NAME = 'MAPGGDATTRUOC';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE q'[
            INSERT INTO CHITIETAPDUNGPGG (MaApDung, MaPGG, MaDatCho, MaHoaDon, NguonApDung, SoTienGiam, ThoiGianApDung)
            SELECT RAWTOHEX(SYS_GUID()), h.MaPGGDatTruoc, p.MaDatCho, h.MaHoaDon, 'DAT_TRUOC',
                   GREATEST(0, NVL(h.TienGiamVoucherDatTruoc, 0)),
                   NVL(h.NgayLapHoaDon, CURRENT_TIMESTAMP)
            FROM HOADON h
            LEFT JOIN PHIENLAMVIEC p ON p.MaPhien = h.MaPhien
            WHERE h.MaPGGDatTruoc IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1 FROM CHITIETAPDUNGPGG ct
                  WHERE ct.MaHoaDon = h.MaHoaDon
                    AND ct.MaPGG = h.MaPGGDatTruoc
                    AND ct.NguonApDung = 'DAT_TRUOC'
              )
        ]';
    END IF;

    SELECT COUNT(*) INTO v_count FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'HOADON' AND COLUMN_NAME = 'MAPGGTAIQUAY';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE q'[
            INSERT INTO CHITIETAPDUNGPGG (MaApDung, MaPGG, MaDatCho, MaHoaDon, NguonApDung, SoTienGiam, ThoiGianApDung)
            SELECT RAWTOHEX(SYS_GUID()), h.MaPGGTaiQuay, NULL, h.MaHoaDon, 'TAI_QUAY',
                   GREATEST(0, NVL(h.TienGiamVoucherTaiQuay, 0)),
                   NVL(h.NgayLapHoaDon, CURRENT_TIMESTAMP)
            FROM HOADON h
            WHERE h.MaPGGTaiQuay IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1 FROM CHITIETAPDUNGPGG ct
                  WHERE ct.MaHoaDon = h.MaHoaDon
                    AND ct.MaPGG = h.MaPGGTaiQuay
                    AND ct.NguonApDung = 'TAI_QUAY'
              )
        ]';
    END IF;
END;
/

MERGE INTO DATCHO dc
USING (
    SELECT src.MaDatCho,
           GREATEST(0, src.TienGoc - src.TienVoucher
               - ROUND(GREATEST(0, src.TienGoc - src.TienVoucher) * src.PhanTramHang / 100, 0)) AS ThanhTienMoi
    FROM (
        SELECT dc.MaDatCho,
               NVL(lkg.DonGiaTheoGio, 0) * NVL(dc.KhoangThoiGianSuDung, 1) AS TienGoc,
               NVL((
                   SELECT SUM(NVL(ct.SoTienGiam, 0))
                   FROM CHITIETAPDUNGPGG ct
                   WHERE ct.MaDatCho = dc.MaDatCho
                     AND ct.NguonApDung = 'DAT_TRUOC'
               ), 0) AS TienVoucher,
               NVL(htv.PhanTramTienGiam, 0) AS PhanTramHang
        FROM DATCHO dc
        JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
        LEFT JOIN LOAIKHONGGIAN lkg ON lkg.MaLoaiKG = kg.MaLoaiKG
        LEFT JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
        LEFT JOIN HANGTHANHVIEN htv ON htv.MaHangThanhVien = kh.MaHangThanhVien
    ) src
) calc
ON (dc.MaDatCho = calc.MaDatCho)
WHEN MATCHED THEN UPDATE SET dc.ThanhTien = calc.ThanhTienMoi;

MERGE INTO HOADON h
USING (
    SELECT h.MaHoaDon,
           NVL(FN_TinhTongTien(h.MaPhien), 0) AS TongTienMoi,
           GREATEST(0,
               NVL(FN_TinhTongTien(h.MaPhien), 0)
               - NVL((
                   SELECT SUM(NVL(ct.SoTienGiam, 0))
                   FROM CHITIETAPDUNGPGG ct
                   WHERE ct.MaHoaDon = h.MaHoaDon
                      OR (p.MaDatCho IS NOT NULL AND ct.MaDatCho = p.MaDatCho)
               ), 0)
               - ROUND(GREATEST(0,
                   NVL(FN_TinhTongTien(h.MaPhien), 0)
                   - NVL((
                       SELECT SUM(NVL(ct.SoTienGiam, 0))
                       FROM CHITIETAPDUNGPGG ct
                       WHERE ct.MaHoaDon = h.MaHoaDon
                          OR (p.MaDatCho IS NOT NULL AND ct.MaDatCho = p.MaDatCho)
                   ), 0)
                 ) * NVL(htv.PhanTramTienGiam, 0) / 100, 0)
           ) AS ThanhTienMoi
    FROM HOADON h
    LEFT JOIN PHIENLAMVIEC p ON p.MaPhien = h.MaPhien
    LEFT JOIN KHACHHANG kh ON kh.MaKH = p.MaKH
    LEFT JOIN HANGTHANHVIEN htv ON htv.MaHangThanhVien = kh.MaHangThanhVien
    WHERE h.MaPhien IS NOT NULL
) calc
ON (h.MaHoaDon = calc.MaHoaDon)
WHEN MATCHED THEN UPDATE
    SET h.TongTien = calc.TongTienMoi,
        h.ThanhTien = calc.ThanhTienMoi;

DECLARE
    PROCEDURE drop_constraint_if_exists(p_name VARCHAR2) IS
        v_count NUMBER;
        v_table USER_CONSTRAINTS.TABLE_NAME%TYPE;
    BEGIN
        SELECT COUNT(*), MAX(TABLE_NAME) INTO v_count, v_table
        FROM USER_CONSTRAINTS
        WHERE CONSTRAINT_NAME = UPPER(p_name);
        IF v_count > 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || v_table || ' DROP CONSTRAINT ' || p_name;
        END IF;
    END;

    PROCEDURE drop_column_if_exists(p_table VARCHAR2, p_column VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = UPPER(p_table)
          AND COLUMN_NAME = UPPER(p_column);
        IF v_count > 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table || ' DROP COLUMN ' || p_column;
        END IF;
    END;
BEGIN
    drop_constraint_if_exists('CHK_HD_TIENHOPLE');

    drop_column_if_exists('DATCHO', 'TongTienGoc');
    drop_column_if_exists('DATCHO', 'MaPGG');
    drop_column_if_exists('DATCHO', 'MaChuSoPGG');
    drop_column_if_exists('DATCHO', 'TienGiamVoucher');
    drop_column_if_exists('DATCHO', 'PhanTramGiamHangTV');
    drop_column_if_exists('DATCHO', 'TienGiamHangTV');
    drop_column_if_exists('DATCHO', 'ThanhTienSauGiam');

    drop_column_if_exists('HOADON', 'TongTienGoc');
    drop_column_if_exists('HOADON', 'TienGocDatTruoc');
    drop_column_if_exists('HOADON', 'TienGocPhatSinh');
    drop_column_if_exists('HOADON', 'MaPGGDatTruoc');
    drop_column_if_exists('HOADON', 'MaPGGTaiQuay');
    drop_column_if_exists('HOADON', 'TienGiamVoucherDatTruoc');
    drop_column_if_exists('HOADON', 'TienGiamHangTVDatTruoc');
    drop_column_if_exists('HOADON', 'PhanTramGiamHangTVDatTruoc');
    drop_column_if_exists('HOADON', 'TienGiamVoucherTaiQuay');
    drop_column_if_exists('HOADON', 'TienGiamHangTVTaiQuay');
    drop_column_if_exists('HOADON', 'PhanTramGiamHangTVTaiQuay');
    drop_column_if_exists('HOADON', 'TongTienGiam');
    drop_column_if_exists('HOADON', 'SoTienThanhToanTaiQuay');
    drop_column_if_exists('HOADON', 'DaTraTruoc');

    EXECUTE IMMEDIATE 'ALTER TABLE HOADON ADD CONSTRAINT CHK_HD_TienHopLe CHECK (TongTien >= 0 AND ThanhTien >= 0 AND ThanhTien <= TongTien)';
END;
/

DECLARE
    PROCEDURE add_fk_if_missing(p_name VARCHAR2, p_sql VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM USER_CONSTRAINTS WHERE CONSTRAINT_NAME = UPPER(p_name);
        IF v_count = 0 THEN
            EXECUTE IMMEDIATE p_sql;
        END IF;
    END;
BEGIN
    add_fk_if_missing('FK_CTADPGG_PGG',
        'ALTER TABLE CHITIETAPDUNGPGG ADD CONSTRAINT FK_CTADPGG_PGG FOREIGN KEY (MaPGG) REFERENCES PHIEUGIAMGIA(MaPGG)');
    add_fk_if_missing('FK_CTADPGG_DATCHO',
        'ALTER TABLE CHITIETAPDUNGPGG ADD CONSTRAINT FK_CTADPGG_DATCHO FOREIGN KEY (MaDatCho) REFERENCES DATCHO(MaDatCho)');
    add_fk_if_missing('FK_CTADPGG_HOADON',
        'ALTER TABLE CHITIETAPDUNGPGG ADD CONSTRAINT FK_CTADPGG_HOADON FOREIGN KEY (MaHoaDon) REFERENCES HOADON(MaHoaDon)');
    add_fk_if_missing('CHK_CTADPGG_NGUON',
        q'[ALTER TABLE CHITIETAPDUNGPGG ADD CONSTRAINT CHK_CTADPGG_Nguon CHECK (NguonApDung IN ('DAT_TRUOC', 'TAI_QUAY'))]');
    add_fk_if_missing('CHK_CTADPGG_TIEN',
        'ALTER TABLE CHITIETAPDUNGPGG ADD CONSTRAINT CHK_CTADPGG_Tien CHECK (SoTienGiam >= 0)');
    add_fk_if_missing('CHK_CTADPGG_DOITUONG',
        'ALTER TABLE CHITIETAPDUNGPGG ADD CONSTRAINT CHK_CTADPGG_DoiTuong CHECK (MaDatCho IS NOT NULL OR MaHoaDon IS NOT NULL)');
END;
/

COMMIT;
