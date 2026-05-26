CREATE OR REPLACE PROCEDURE SP_ThanhToanVoiPhieuGiamGia(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_MaPGG IN VARCHAR2,
    p_PhuongThucTT IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_MaHoaDon HOADON.MaHoaDon%TYPE;
    v_MaPGGTaiQuay HOADON.MaPGGTaiQuay%TYPE;
    v_MaKG PHIENLAMVIEC.MaKG%TYPE;
    v_MaDatCho PHIENLAMVIEC.MaDatCho%TYPE;
    v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien%TYPE;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE;
    v_ThoiGianBatDau TIMESTAMP;
    v_ThoiGianKetThuc TIMESTAMP;
    v_KhoangThoiGianSuDung NUMBER;
    v_TongPhut NUMBER;
    v_GioNguyen NUMBER;
    v_PhutLe NUMBER;
    v_SoGioThucTe NUMBER;
    v_SoGioQua NUMBER;
    v_CountDV NUMBER;
    v_SoHoaDon NUMBER;
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
    v_PhuongThucThanhToan VARCHAR2(50);
    v_CoPGG BOOLEAN := FALSE;
    v_SoLuongDaDung NUMBER;
    v_SoLuongToiDa NUMBER;
    v_KetQuaPGG VARCHAR2(4000);
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
BEGIN
    IF p_MaPhien IS NULL OR LENGTH(TRIM(p_MaPhien)) = 0 THEN
        p_outMessage := 'Lỗi: Thiếu mã phiên cần thanh toán.';
        RETURN;
    END IF;

    IF p_PhuongThucTT IS NULL
       OR p_PhuongThucTT NOT IN ('Tiền mặt', 'Chuyển khoản', 'Đặt trước') THEN
        p_outMessage := 'Lỗi: Phương thức thanh toán không hợp lệ.';
        RETURN;
    END IF;

    SELECT TrangThaiPhien, MaKG, MaDatCho, ThoiGianBatDau
    INTO v_TrangThaiPhien, v_MaKG, v_MaDatCho, v_ThoiGianBatDau
    FROM PHIENLAMVIEC
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    SELECT COUNT(*)
    INTO v_SoHoaDon
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien);

    IF v_SoHoaDon = 0 THEN
        ROLLBACK;
        p_outMessage := 'Không tìm thấy hóa đơn của phiên [' || TRIM(p_MaPhien) || '].';
        RETURN;
    ELSIF v_SoHoaDon > 1 THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu lỗi: phiên [' || TRIM(p_MaPhien) || '] có nhiều hóa đơn.';
        RETURN;
    END IF;

    SELECT MaHoaDon, TrangThaiThanhToan, NVL(DaTraTruoc, 0)
    INTO v_MaHoaDon, v_TrangThaiThanhToan, v_DaTraTruoc
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    IF v_TrangThaiThanhToan = 'Đã thanh toán thành công' THEN
        ROLLBACK;
        p_outMessage := 'Lỗi: Hóa đơn đã được thanh toán trước đó.';
        RETURN;
    END IF;

    IF v_TrangThaiPhien <> 'Đã kết thúc' THEN
        v_ThoiGianKetThuc := CURRENT_TIMESTAMP;

        UPDATE PHIENLAMVIEC
        SET ThoiGianKetThuc = v_ThoiGianKetThuc,
            TrangThaiPhien = 'Đã kết thúc',
            CapNhatLanCuoi = CURRENT_TIMESTAMP
        WHERE MaPhien = TRIM(p_MaPhien);

        IF v_MaDatCho IS NOT NULL THEN
            BEGIN
                SELECT NVL(KhoangThoiGianSuDung, 0)
                INTO v_KhoangThoiGianSuDung
                FROM DATCHO
                WHERE MaDatCho = v_MaDatCho;

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
        END IF;
    END IF;

    IF v_MaDatCho IS NOT NULL THEN
        SELECT NVL(NULLIF(TongTienGoc, 0), 0),
               NVL(NULLIF(ThanhTienSauGiam, 0), NVL(ThanhTien, 0)),
               MaPGG,
               NVL(TienGiamVoucher, 0),
               NVL(PhanTramGiamHangTV, 0),
               NVL(TienGiamHangTV, 0)
        INTO v_TienGocDatTruoc,
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

        v_TienGocPhatSinh := GREATEST(0, FN_TinhTienDichVu(TRIM(p_MaPhien)));
    ELSE
        v_DaTraTruoc := 0;
        v_TienGocDatTruoc := 0;
        v_TienGocPhatSinh := GREATEST(0, FN_TinhTongTien(TRIM(p_MaPhien)));
    END IF;

    IF p_MaPGG IS NOT NULL AND LENGTH(TRIM(p_MaPGG)) > 0 AND v_TienGocPhatSinh > 0 THEN
        v_CoPGG := TRUE;
        v_MaPGGTaiQuay := TRIM(p_MaPGG);

        SELECT NVL(SLDaDung, 0), NVL(SLToiDa, 0)
        INTO v_SoLuongDaDung, v_SoLuongToiDa
        FROM PHIEUGIAMGIA
        WHERE MaPGG = v_MaPGGTaiQuay
        FOR UPDATE NOWAIT;

        IF v_SoLuongDaDung >= v_SoLuongToiDa THEN
            p_outMessage := 'Lỗi: Phiếu giảm giá đã hết lượt sử dụng.';
            ROLLBACK;
            RETURN;
        END IF;

        v_KetQuaPGG := FN_KiemTraPhieuGiamGiaHopLe(v_MaPGGTaiQuay, v_TienGocPhatSinh);
        IF v_KetQuaPGG <> 'OK' THEN
            p_outMessage := 'Lỗi: Phiếu giảm giá không hợp lệ (' || v_KetQuaPGG || ').';
            ROLLBACK;
            RETURN;
        END IF;

        SELECT LEAST(NVL(GiaTriGiamGia, 0), GREATEST(0, v_TienGocPhatSinh))
        INTO v_TienGiamVoucherTaiQuay
        FROM PHIEUGIAMGIA
        WHERE MaPGG = v_MaPGGTaiQuay;
    ELSE
        v_MaPGGTaiQuay := NULL;
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
    v_ConPhaiThanhToan := GREATEST(0, v_TienGocPhatSinh - v_TienGiamVoucherTaiQuay - v_TienGiamHangTVTaiQuay);

    IF v_ConPhaiThanhToan > 0 AND p_PhuongThucTT = 'Đặt trước' THEN
        p_outMessage := 'Lỗi: Hóa đơn còn tiền cần thu tại quầy, vui lòng chọn Tiền mặt hoặc Chuyển khoản.';
        ROLLBACK;
        RETURN;
    END IF;

    v_PhuongThucThanhToan := CASE
        WHEN v_ConPhaiThanhToan <= 0 AND NVL(v_DaTraTruoc, 0) > 0 THEN 'Đặt trước'
        ELSE p_PhuongThucTT
    END;

    v_TongTienGoc := GREATEST(0, NVL(v_TienGocDatTruoc, 0)) + GREATEST(0, NVL(v_TienGocPhatSinh, 0));
    v_TongTienGiam := GREATEST(0, NVL(v_TienGiamVoucherDatTruoc, 0))
        + GREATEST(0, NVL(v_TienGiamHangTVDatTruoc, 0))
        + GREATEST(0, NVL(v_TienGiamVoucherTaiQuay, 0))
        + GREATEST(0, NVL(v_TienGiamHangTVTaiQuay, 0));

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

    UPDATE HOADON
    SET TongTien = v_TongTienGoc,
        TongTienGoc = v_TongTienGoc,
        TienGocDatTruoc = v_TienGocDatTruoc,
        TienGocPhatSinh = v_TienGocPhatSinh,
        ThanhTien = 0,
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
        SoTienThanhToanTaiQuay = v_ConPhaiThanhToan,
        TrangThaiThanhToan = 'Đã thanh toán thành công',
        PhuongThucThanhToan = v_PhuongThucThanhToan,
        NgayLapHoaDon = CURRENT_TIMESTAMP,
        MaNV = p_MaNV
    WHERE MaHoaDon = v_MaHoaDon;

    IF v_CoPGG AND v_MaPGGTaiQuay IS NOT NULL AND v_TienGiamVoucherTaiQuay > 0 THEN
        UPDATE PHIEUGIAMGIA
        SET SLDaDung = NVL(SLDaDung, 0) + 1
        WHERE MaPGG = v_MaPGGTaiQuay;
    END IF;

    COMMIT;
    IF v_ConPhaiThanhToan = 0 AND NVL(v_DaTraTruoc, 0) > 0 THEN
        p_outMessage := 'Hóa đơn đã được trả trước toàn bộ. Đã chốt thanh toán thành công.';
    ELSE
        p_outMessage := 'Thanh toán thành công. Mã hóa đơn: ' || v_MaHoaDon;
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_outMessage := 'Lỗi: Không tìm thấy phiên, hóa đơn hoặc phiếu giảm giá cần xử lý.';
    WHEN ex_resource_busy THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu thanh toán đang được nhân viên khác xử lý. Vui lòng thử lại sau.';
    WHEN TOO_MANY_ROWS THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu lỗi: phiên [' || p_MaPhien || '] có nhiều hóa đơn.';
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thanh toán: ' || SQLERRM;
END SP_ThanhToanVoiPhieuGiamGia;
/
