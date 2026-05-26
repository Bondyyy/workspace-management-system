CREATE OR REPLACE PROCEDURE SP_KetThucPhien(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien%TYPE;
    v_MaKG PHIENLAMVIEC.MaKG%TYPE;
    v_MaHoaDon HOADON.MaHoaDon%TYPE;
    v_MaPGG HOADON.MaPGG%TYPE;
    v_DaTraTruoc HOADON.DaTraTruoc%TYPE;
    v_TongTienGoc NUMBER(18, 2) := 0;
    v_TienGocDatTruoc NUMBER(18, 2) := 0;
    v_TienGocPhatSinh NUMBER(18, 2) := 0;
    v_ThanhTien NUMBER(18, 2) := 0;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE;
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
    v_ThoiGianBatDau TIMESTAMP;
    v_KhoangThoiGianSuDung NUMBER;
    v_ThoiGianKetThuc TIMESTAMP;
    v_TongPhut NUMBER;
    v_GioNguyen NUMBER;
    v_PhutLe NUMBER;
    v_SoGioThucTe NUMBER;
    v_SoGioQua NUMBER;
    v_CountDV NUMBER;
    v_MaDatCho VARCHAR2(50);
    v_SoHoaDon NUMBER;
    v_MaPGGDatTruoc VARCHAR2(50);
    v_TienGiamVoucherDatTruoc NUMBER(18, 2) := 0;
    v_PhanTramGiamHangTVDatTruoc NUMBER(5, 2) := 0;
    v_TienGiamHangTVDatTruoc NUMBER(18, 2) := 0;
    v_TienGiamVoucherTaiQuay NUMBER(18, 2) := 0;
    v_PhanTramGiamHangTVTaiQuay NUMBER(5, 2) := 0;
    v_TienGiamHangTVTaiQuay NUMBER(18, 2) := 0;
    v_TongTienGiam NUMBER(18, 2) := 0;
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
        p_outMessage := 'Phiên làm việc [' || p_MaPhien || '] không ở trạng thái đang hoạt động. Trạng thái hiện tại: '
            || v_TrangThaiPhien;
        RETURN;
    END IF;

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
        p_outMessage := 'Dữ liệu lỗi: phiên [' || TRIM(p_MaPhien) || '] có nhiều hóa đơn, cần cleanup.';
        RETURN;
    END IF;

    SELECT MaHoaDon,
           MaPGG,
           NVL(DaTraTruoc, 0),
           TrangThaiThanhToan,
           MaPGGDatTruoc,
           NVL(TienGiamVoucherDatTruoc, 0),
           NVL(PhanTramGiamHangTVDatTruoc, 0),
           NVL(TienGiamHangTVDatTruoc, 0)
    INTO v_MaHoaDon,
         v_MaPGG,
         v_DaTraTruoc,
         v_TrangThaiThanhToan,
         v_MaPGGDatTruoc,
         v_TienGiamVoucherDatTruoc,
         v_PhanTramGiamHangTVDatTruoc,
         v_TienGiamHangTVDatTruoc
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
            SELECT KhoangThoiGianSuDung
            INTO v_KhoangThoiGianSuDung
            FROM DATCHO
            WHERE MaDatCho = v_MaDatCho;

            v_TongPhut := EXTRACT(DAY FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 24 * 60 +
                          EXTRACT(HOUR FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 60 +
                          EXTRACT(MINUTE FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) +
                          EXTRACT(SECOND FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) / 60;

            v_GioNguyen := TRUNC(v_TongPhut / 60);
            v_PhutLe := MOD(v_TongPhut, 60);

            IF v_PhutLe <= 15 THEN
                v_SoGioThucTe := v_GioNguyen;
            ELSE
                v_SoGioThucTe := v_GioNguyen + 1;
            END IF;

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

    IF v_MaDatCho IS NOT NULL THEN
        BEGIN
            SELECT NVL(TongTienGoc, 0),
                   NVL(ThanhTienSauGiam, NVL(ThanhTien, 0)),
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
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL;
        END;

        IF v_TienGocDatTruoc <= 0 THEN
            v_TienGocDatTruoc := FN_TinhTienKhongGian(TRIM(p_MaPhien));
        END IF;
        v_TienGocPhatSinh := FN_TinhTienDichVu(TRIM(p_MaPhien));
    ELSE
        v_TienGocDatTruoc := 0;
        v_DaTraTruoc := 0;
        v_TienGocPhatSinh := FN_TinhTongTien(TRIM(p_MaPhien));
    END IF;

    SELECT NVL(MAX(HTV.PhanTramTienGiam), 0)
    INTO v_PhanTramGiamHangTVTaiQuay
    FROM PHIENLAMVIEC PLV
    LEFT JOIN KHACHHANG KH ON PLV.MaKH = KH.MaKH
    LEFT JOIN HANGTHANHVIEN HTV ON KH.MaHangThanhVien = HTV.MaHangThanhVien
    WHERE PLV.MaPhien = TRIM(p_MaPhien);

    IF v_MaPGG IS NOT NULL THEN
        BEGIN
            SELECT LEAST(NVL(GiaTriGiamGia, 0), GREATEST(0, v_TienGocPhatSinh))
            INTO v_TienGiamVoucherTaiQuay
            FROM PHIEUGIAMGIA
            WHERE MaPGG = v_MaPGG;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_TienGiamVoucherTaiQuay := 0;
        END;
    END IF;

    v_PhanTramGiamHangTVTaiQuay := LEAST(100, GREATEST(0, NVL(v_PhanTramGiamHangTVTaiQuay, 0)));
    v_TienGiamHangTVTaiQuay := ROUND(GREATEST(0, v_TienGocPhatSinh - v_TienGiamVoucherTaiQuay)
        * v_PhanTramGiamHangTVTaiQuay / 100, 0);
    v_ThanhTien := GREATEST(0, v_TienGocPhatSinh - v_TienGiamVoucherTaiQuay - v_TienGiamHangTVTaiQuay);
    v_TongTienGoc := GREATEST(0, v_TienGocDatTruoc) + GREATEST(0, v_TienGocPhatSinh);
    v_TongTienGiam := GREATEST(0, v_TienGiamVoucherDatTruoc)
        + GREATEST(0, v_TienGiamHangTVDatTruoc)
        + GREATEST(0, v_TienGiamVoucherTaiQuay)
        + GREATEST(0, v_TienGiamHangTVTaiQuay);

    IF v_MaDatCho IS NOT NULL THEN
        v_TrangThaiThanhToan := 'Đã trả trước';
    ELSE
        v_TrangThaiThanhToan := 'Đang chờ thanh toán';
    END IF;

    UPDATE HOADON
    SET TongTienGoc = v_TongTienGoc,
        TongTien = v_TongTienGoc,
        TienGocDatTruoc = v_TienGocDatTruoc,
        TienGocPhatSinh = v_TienGocPhatSinh,
        DaTraTruoc = v_DaTraTruoc,
        MaPGGDatTruoc = v_MaPGGDatTruoc,
        TienGiamVoucherDatTruoc = v_TienGiamVoucherDatTruoc,
        PhanTramGiamHangTVDatTruoc = v_PhanTramGiamHangTVDatTruoc,
        TienGiamHangTVDatTruoc = v_TienGiamHangTVDatTruoc,
        MaPGGTaiQuay = v_MaPGG,
        TienGiamVoucherTaiQuay = v_TienGiamVoucherTaiQuay,
        PhanTramGiamHangTVTaiQuay = v_PhanTramGiamHangTVTaiQuay,
        TienGiamHangTVTaiQuay = v_TienGiamHangTVTaiQuay,
        TongTienGiam = v_TongTienGiam,
        ThanhTien = v_ThanhTien,
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
        || '. Còn phải thanh toán: ' || TO_CHAR(v_ThanhTien, 'FM999G999G999G990') || ' VNĐ';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_outMessage := 'Không tìm thấy phiên hoặc hóa đơn của phiên [' || p_MaPhien || '].';
    WHEN ex_resource_busy THEN
        ROLLBACK;
        p_outMessage := 'Phiên hoặc hóa đơn đang được nhân viên khác xử lý. Vui lòng thử lại sau.';
    WHEN TOO_MANY_ROWS THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu lỗi: phiên [' || p_MaPhien || '] có nhiều hóa đơn, cần cleanup.';
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi kết thúc phiên: ' || SQLERRM;
END SP_KetThucPhien;
/
