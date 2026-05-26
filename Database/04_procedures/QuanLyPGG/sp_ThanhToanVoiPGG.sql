CREATE OR REPLACE PROCEDURE SP_ThanhToanVoiPhieuGiamGia(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_MaPGG IN VARCHAR2,
    p_PhuongThucTT IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_MaHoaDon HOADON.MaHoaDon%TYPE;

v_MaPGGTaiQuay VARCHAR2 (50);

v_MaKG PHIENLAMVIEC.MaKG % TYPE;

v_MaDatCho PHIENLAMVIEC.MaDatCho % TYPE;

v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien % TYPE;

v_TrangThaiThanhToan HOADON.TrangThaiThanhToan % TYPE;

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

v_DaTraTruoc NUMBER (18, 2) := 0;

v_TienDichVu NUMBER (18, 2) := 0;

v_TongTien NUMBER (18, 2) := 0;

v_ConPhaiThanhToan NUMBER (18, 2) := 0;

v_TienGiamVoucher NUMBER (18, 2) := 0;

v_PhanTramGiamHangTV NUMBER (5, 2) := 0;

v_TienGiamHangTV NUMBER (18, 2) := 0;

v_PhuongThucThanhToan VARCHAR2 (50);

v_CoPGG BOOLEAN := FALSE;

v_SoLuongDaDung NUMBER;

v_SoLuongToiDa NUMBER;

v_KetQuaPGG VARCHAR2 (4000);

ex_resource_busy EXCEPTION;

PRAGMA EXCEPTION_INIT (ex_resource_busy, -54);

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

    v_MaPGGTaiQuay := NULL;

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
                SELECT KhoangThoiGianSuDung
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
        SELECT NVL(ThanhTien, 0)
        INTO v_DaTraTruoc
        FROM DATCHO
        WHERE MaDatCho = v_MaDatCho;
        v_TienDichVu := FN_TinhTienDichVu(TRIM(p_MaPhien));
        v_TongTien := GREATEST(0, v_DaTraTruoc) + GREATEST(0, v_TienDichVu);
        v_ConPhaiThanhToan := GREATEST(0, v_TienDichVu);
    ELSE
        v_DaTraTruoc := 0;
        v_TongTien := FN_TinhTongTien(TRIM(p_MaPhien));
        v_ConPhaiThanhToan := v_TongTien;
    END IF;

    IF p_MaPGG IS NOT NULL AND LENGTH(TRIM(p_MaPGG)) > 0 THEN
        v_CoPGG := TRUE;

        SELECT NVL(SLDaDung, 0), NVL(SLToiDa, 0)
        INTO v_SoLuongDaDung, v_SoLuongToiDa
        FROM PHIEUGIAMGIA
        WHERE MaPGG = TRIM(p_MaPGG)
        FOR UPDATE NOWAIT;

        IF v_SoLuongDaDung >= v_SoLuongToiDa THEN
            p_outMessage := 'Lỗi: Phiếu giảm giá đã hết lượt sử dụng.';
            ROLLBACK;
            RETURN;
        END IF;

        v_KetQuaPGG := FN_KiemTraPhieuGiamGiaHopLe(TRIM(p_MaPGG), v_ConPhaiThanhToan);
        IF v_KetQuaPGG <> 'OK' THEN
            p_outMessage := 'Lỗi: Phiếu giảm giá không hợp lệ (' || v_KetQuaPGG || ').';
            ROLLBACK;
            RETURN;
        END IF;

        SELECT LEAST(NVL(GiaTriGiamGia, 0), GREATEST(0, v_ConPhaiThanhToan))
        INTO v_TienGiamVoucher
        FROM PHIEUGIAMGIA
        WHERE MaPGG = TRIM(p_MaPGG);
    END IF;

    SELECT NVL(MAX(HTV.PhanTramTienGiam), 0)
    INTO v_PhanTramGiamHangTV
    FROM PHIENLAMVIEC PLV
    LEFT JOIN KHACHHANG KH ON PLV.MaKH = KH.MaKH
    LEFT JOIN HANGTHANHVIEN HTV ON KH.MaHangThanhVien = HTV.MaHangThanhVien
    WHERE PLV.MaPhien = TRIM(p_MaPhien);

    v_TienGiamHangTV := ROUND(GREATEST(0, v_ConPhaiThanhToan - v_TienGiamVoucher)
        * LEAST(100, GREATEST(0, NVL(v_PhanTramGiamHangTV, 0))) / 100, 0);
    v_ConPhaiThanhToan := GREATEST(0, v_ConPhaiThanhToan - v_TienGiamVoucher - v_TienGiamHangTV);

    IF v_ConPhaiThanhToan > 0 AND p_PhuongThucTT = 'Đặt trước' THEN
        p_outMessage := 'Lỗi: Hóa đơn còn tiền cần thu tại quầy, vui lòng chọn Tiền mặt hoặc Chuyển khoản.';
        ROLLBACK;
        RETURN;
    END IF;

    v_PhuongThucThanhToan := CASE
        WHEN v_ConPhaiThanhToan <= 0 AND NVL(v_DaTraTruoc, 0) > 0 THEN 'Đặt trước'
        ELSE p_PhuongThucTT
    END;

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

    v_MaPGGTaiQuay := CASE
        WHEN p_MaPGG IS NOT NULL AND LENGTH(TRIM(p_MaPGG)) > 0 THEN TRIM(p_MaPGG)
        ELSE NULL
    END;

    UPDATE HOADON
    SET TongTien = v_TongTien,
        ThanhTien = 0,
        DaTraTruoc = v_DaTraTruoc,
        MaPGGTaiQuay = v_MaPGGTaiQuay,
        TrangThaiThanhToan = 'Đã thanh toán thành công',
        PhuongThucThanhToan = v_PhuongThucThanhToan,
        NgayLapHoaDon = CURRENT_TIMESTAMP,
        MaNV = p_MaNV
    WHERE MaHoaDon = v_MaHoaDon;

    IF v_CoPGG AND v_MaPGGTaiQuay IS NOT NULL THEN
        UPDATE PHIEUGIAMGIA
        SET SLDaDung = NVL(SLDaDung, 0) + 1
        WHERE MaPGG = v_MaPGGTaiQuay;
    END IF;

    COMMIT;
    p_outMessage := 'Thanh toán thành công. Mã hóa đơn: ' || v_MaHoaDon;

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
;
/