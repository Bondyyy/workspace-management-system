CREATE OR REPLACE PROCEDURE SP_KetThucPhien(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien%TYPE;
    v_MaKG PHIENLAMVIEC.MaKG%TYPE;
    v_MaDatCho PHIENLAMVIEC.MaDatCho%TYPE;
    v_ThoiGianBatDau TIMESTAMP;
    v_ThoiGianKetThuc TIMESTAMP;
    v_KhoangThoiGianSuDung NUMBER;
    v_TongPhut NUMBER;
    v_GioNguyen NUMBER;
    v_PhutLe NUMBER;
    v_SoGioThucTe NUMBER;
    v_SoGioQua NUMBER;
    v_CountDV NUMBER;
    v_MaHoaDon HOADON.MaHoaDon%TYPE;
    v_MaPGGLegacy HOADON.MaPGG%TYPE;
    v_MaPGGTaiQuay HOADON.MaPGGTaiQuay%TYPE;
    v_DaTraTruoc NUMBER(18, 2) := 0;
    v_TienGocDatTruoc NUMBER(18, 2) := 0;
    v_TienGocPhatSinh NUMBER(18, 2) := 0;
    v_TongTienGoc NUMBER(18, 2) := 0;
    v_ConPhaiThanhToan NUMBER(18, 2) := 0;
    v_MaPGGDatTruoc HOADON.MaPGGDatTruoc%TYPE;
    v_TienGiamVoucherDatTruoc NUMBER(18, 2) := 0;
    v_PhanTramGiamHangTVDatTruoc NUMBER(5, 2) := 0;
    v_TienGiamHangTVDatTruoc NUMBER(18, 2) := 0;
    v_TienGiamVoucherTaiQuay NUMBER(18, 2) := 0;
    v_PhanTramGiamHangTVTaiQuay NUMBER(5, 2) := 0;
    v_TienGiamHangTVTaiQuay NUMBER(18, 2) := 0;
    v_TongTienGiam NUMBER(18, 2) := 0;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE;
    v_SoHoaDon NUMBER;
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
BEGIN
    IF p_MaPhien IS NULL OR LENGTH(TRIM(p_MaPhien)) = 0 THEN
        p_outMessage := 'Lỗi: Thiếu mã phiên cần kết thúc.';
        RETURN;
    END IF;

    SELECT TrangThaiPhien, MaKG, MaDatCho, ThoiGianBatDau
    INTO v_TrangThaiPhien, v_MaKG, v_MaDatCho, v_ThoiGianBatDau
    FROM PHIENLAMVIEC
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    IF v_TrangThaiPhien <> 'Đang hoạt động' THEN
        ROLLBACK;
        p_outMessage := 'Phiên làm việc [' || p_MaPhien || '] không ở trạng thái đang hoạt động.';
        RETURN;
    END IF;

    SELECT COUNT(*)
    INTO v_SoHoaDon
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien);

    IF v_SoHoaDon = 0 THEN
        INSERT INTO HOADON (
            DaTraTruoc, TongTien, ThanhTien, TongTienGoc, TienGocDatTruoc, TienGocPhatSinh,
            NgayLapHoaDon, TrangThaiThanhToan, PhuongThucThanhToan, MaPhien, MaNV
        ) VALUES (
            0, 0, 0, 0, 0, 0,
            CURRENT_TIMESTAMP, 'Đang chờ thanh toán', NULL, TRIM(p_MaPhien), p_MaNV
        );
    ELSIF v_SoHoaDon > 1 THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu lỗi: phiên [' || TRIM(p_MaPhien) || '] có nhiều hóa đơn.';
        RETURN;
    END IF;

    SELECT MaHoaDon, MaPGG, MaPGGTaiQuay, NVL(DaTraTruoc, 0), TrangThaiThanhToan
    INTO v_MaHoaDon, v_MaPGGLegacy, v_MaPGGTaiQuay, v_DaTraTruoc, v_TrangThaiThanhToan
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    v_ThoiGianKetThuc := CURRENT_TIMESTAMP;

    UPDATE PHIENLAMVIEC
    SET ThoiGianKetThuc = v_ThoiGianKetThuc,
        TrangThaiPhien = 'Đã kết thúc',
        CapNhatLanCuoi = CURRENT_TIMESTAMP
    WHERE MaPhien = TRIM(p_MaPhien);

    IF v_MaDatCho IS NOT NULL THEN
        BEGIN
            SELECT NVL(KhoangThoiGianSuDung, 0),
                   NVL(NULLIF(TongTienGoc, 0), 0),
                   NVL(NULLIF(ThanhTienSauGiam, 0), NVL(ThanhTien, 0)),
                   MaPGG,
                   NVL(TienGiamVoucher, 0),
                   NVL(PhanTramGiamHangTV, 0),
                   NVL(TienGiamHangTV, 0)
            INTO v_KhoangThoiGianSuDung,
                 v_TienGocDatTruoc,
                 v_DaTraTruoc,
                 v_MaPGGDatTruoc,
                 v_TienGiamVoucherDatTruoc,
                 v_PhanTramGiamHangTVDatTruoc,
                 v_TienGiamHangTVDatTruoc
            FROM DATCHO
            WHERE MaDatCho = v_MaDatCho;

            IF NVL(v_TienGocDatTruoc, 0) = 0 THEN
                SELECT NVL(LKG.DonGiaTheoGio, 0) * NVL(DC.KhoangThoiGianSuDung, 0)
                INTO v_TienGocDatTruoc
                FROM DATCHO DC
                JOIN KHONGGIAN KG ON DC.MaKG = KG.MaKG
                JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
                WHERE DC.MaDatCho = v_MaDatCho;
            END IF;

            v_TongPhut := EXTRACT(DAY FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 24 * 60
                + EXTRACT(HOUR FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 60
                + EXTRACT(MINUTE FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau))
                + EXTRACT(SECOND FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) / 60;
            v_GioNguyen := TRUNC(v_TongPhut / 60);
            v_PhutLe := MOD(v_TongPhut, 60);
            v_SoGioThucTe := CASE WHEN v_PhutLe <= 15 THEN v_GioNguyen ELSE v_GioNguyen + 1 END;

            IF v_SoGioThucTe > v_KhoangThoiGianSuDung THEN
                v_SoGioQua := v_SoGioThucTe - v_KhoangThoiGianSuDung;

                SELECT COUNT(*)
                INTO v_CountDV
                FROM CHITIETDICHVU
                WHERE MaPhien = TRIM(p_MaPhien)
                  AND MaDV = 'DV0000';

                IF v_CountDV > 0 THEN
                    UPDATE CHITIETDICHVU
                    SET SoLuong = GREATEST(SoLuong, v_SoGioQua)
                    WHERE MaPhien = TRIM(p_MaPhien)
                      AND MaDV = 'DV0000';
                ELSE
                    INSERT INTO CHITIETDICHVU (MaDV, MaPhien, SoLuong, GhiChu)
                    VALUES ('DV0000', TRIM(p_MaPhien), v_SoGioQua, 'Tự động thêm do dùng quá giờ');
                END IF;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL;
        END;

        v_TienGocPhatSinh := GREATEST(0, FN_TinhTienDichVu(TRIM(p_MaPhien)));
        v_TrangThaiThanhToan := 'Đã trả trước';
    ELSE
        v_DaTraTruoc := 0;
        v_TienGocDatTruoc := 0;
        v_TienGocPhatSinh := GREATEST(0, FN_TinhTongTien(TRIM(p_MaPhien)));
        v_TrangThaiThanhToan := 'Đang chờ thanh toán';
    END IF;

    IF v_MaDatCho IS NULL THEN
        v_MaPGGTaiQuay := COALESCE(v_MaPGGTaiQuay, v_MaPGGLegacy);
    END IF;
    IF v_MaPGGTaiQuay IS NOT NULL THEN
        BEGIN
            SELECT LEAST(NVL(GiaTriGiamGia, 0), GREATEST(0, v_TienGocPhatSinh))
            INTO v_TienGiamVoucherTaiQuay
            FROM PHIEUGIAMGIA
            WHERE MaPGG = v_MaPGGTaiQuay;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_TienGiamVoucherTaiQuay := 0;
        END;
    END IF;

    SELECT NVL(MAX(HTV.PhanTramTienGiam), 0)
    INTO v_PhanTramGiamHangTVTaiQuay
    FROM PHIENLAMVIEC PLV
    LEFT JOIN KHACHHANG KH ON PLV.MaKH = KH.MaKH
    LEFT JOIN HANGTHANHVIEN HTV ON KH.MaHangThanhVien = HTV.MaHangThanhVien
    WHERE PLV.MaPhien = TRIM(p_MaPhien);

    v_PhanTramGiamHangTVTaiQuay := LEAST(100, GREATEST(0, NVL(v_PhanTramGiamHangTVTaiQuay, 0)));
    v_TienGiamHangTVTaiQuay := ROUND(GREATEST(0, v_TienGocPhatSinh - v_TienGiamVoucherTaiQuay)
        * v_PhanTramGiamHangTVTaiQuay / 100, 0);

    v_TongTienGoc := GREATEST(0, NVL(v_TienGocDatTruoc, 0)) + GREATEST(0, NVL(v_TienGocPhatSinh, 0));
    v_TongTienGiam := GREATEST(0, NVL(v_TienGiamVoucherDatTruoc, 0))
        + GREATEST(0, NVL(v_TienGiamHangTVDatTruoc, 0))
        + GREATEST(0, NVL(v_TienGiamVoucherTaiQuay, 0))
        + GREATEST(0, NVL(v_TienGiamHangTVTaiQuay, 0));
    v_ConPhaiThanhToan := GREATEST(0, v_TienGocPhatSinh - v_TienGiamVoucherTaiQuay - v_TienGiamHangTVTaiQuay);

    UPDATE HOADON
    SET TongTien = v_TongTienGoc,
        TongTienGoc = v_TongTienGoc,
        TienGocDatTruoc = v_TienGocDatTruoc,
        TienGocPhatSinh = v_TienGocPhatSinh,
        ThanhTien = v_ConPhaiThanhToan,
        DaTraTruoc = v_DaTraTruoc,
        MaPGGDatTruoc = v_MaPGGDatTruoc,
        MaPGGTaiQuay = v_MaPGGTaiQuay,
        MaPGG = v_MaPGGTaiQuay,
        TienGiamVoucherDatTruoc = v_TienGiamVoucherDatTruoc,
        PhanTramGiamHangTVDatTruoc = v_PhanTramGiamHangTVDatTruoc,
        TienGiamHangTVDatTruoc = v_TienGiamHangTVDatTruoc,
        TienGiamVoucherTaiQuay = v_TienGiamVoucherTaiQuay,
        PhanTramGiamHangTVTaiQuay = v_PhanTramGiamHangTVTaiQuay,
        TienGiamHangTVTaiQuay = v_TienGiamHangTVTaiQuay,
        TongTienGiam = v_TongTienGiam,
        TrangThaiThanhToan = v_TrangThaiThanhToan,
        MaNV = p_MaNV,
        NgayLapHoaDon = CURRENT_TIMESTAMP
    WHERE MaHoaDon = v_MaHoaDon;

    UPDATE KHONGGIAN kg
    SET TrangThaiKG =
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM PHIENLAMVIEC p
                WHERE p.MaKG = kg.MaKG
                  AND p.TrangThaiPhien = 'Đang hoạt động'
            ) THEN 'Đang hoạt động'
            WHEN kg.TrangThaiKG = 'Bảo trì' THEN 'Bảo trì'
            ELSE 'Trống'
        END
    WHERE kg.MaKG = v_MaKG;

    COMMIT;
    p_outMessage := 'Kết thúc phiên làm việc thành công! Mã hóa đơn: ' || v_MaHoaDon
        || '. Tổng tiền gốc: ' || TO_CHAR(v_TongTienGoc, 'FM999G999G999G990') || ' VNĐ'
        || '. Còn phải thanh toán: ' || TO_CHAR(v_ConPhaiThanhToan, 'FM999G999G999G990') || ' VNĐ';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_outMessage := 'Không tìm thấy phiên hoặc hóa đơn của phiên [' || p_MaPhien || '].';
    WHEN ex_resource_busy THEN
        ROLLBACK;
        p_outMessage := 'Phiên hoặc hóa đơn đang được nhân viên khác xử lý. Vui lòng thử lại sau.';
    WHEN TOO_MANY_ROWS THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu lỗi: phiên [' || p_MaPhien || '] có nhiều hóa đơn.';
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi kết thúc phiên: ' || SQLERRM;
END SP_KetThucPhien;
/
