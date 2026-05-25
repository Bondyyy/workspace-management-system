CREATE OR REPLACE PROCEDURE SP_ThanhToanVoiPhieuGiamGia(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_MaPGG IN VARCHAR2,
    p_PhuongThucTT IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_MaHoaDon HOADON.MaHoaDon%TYPE;
    v_MaKG PHIENLAMVIEC.MaKG%TYPE;
    v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien%TYPE;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE;
    v_DaTraTruoc HOADON.DaTraTruoc%TYPE;
    v_SoTienDaTraTruoc NUMBER(18, 2) := 0;
    v_TongTien NUMBER(18, 2);
    v_ThanhTien NUMBER(18, 2);
    v_SoLuongDaDung NUMBER;
    v_SoLuongToiDa NUMBER;
    v_KetQuaPGG VARCHAR2(4000);
    v_CoPGG BOOLEAN := FALSE;
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
    v_MaDatCho VARCHAR2(50);
    v_ThoiGianBatDau TIMESTAMP;
    v_KhoangThoiGianSuDung NUMBER;
    v_ThoiGianKetThuc TIMESTAMP;
    v_TongPhut NUMBER;
    v_GioNguyen NUMBER;
    v_PhutLe NUMBER;
    v_SoGioThucTe NUMBER;
    v_SoGioQua NUMBER;
    v_CountDV NUMBER;
    v_SoHoaDon NUMBER;
BEGIN
    IF p_MaPhien IS NULL OR LENGTH(TRIM(p_MaPhien)) = 0 THEN
        p_outMessage := 'Lỗi: Thiếu mã phiên cần thanh toán.';
        RETURN;
    END IF;

    IF p_PhuongThucTT IS NULL OR p_PhuongThucTT NOT IN ('Tiền mặt', 'Chuyển khoản') THEN
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
        p_outMessage := 'Dữ liệu lỗi: phiên [' || TRIM(p_MaPhien) || '] có nhiều hóa đơn, cần cleanup.';
        RETURN;
    END IF;

    SELECT MaHoaDon, TrangThaiThanhToan, NVL(DaTraTruoc, 0)
    INTO v_MaHoaDon, v_TrangThaiThanhToan, v_DaTraTruoc
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    -- Nếu phiên chưa kết thúc, thực hiện chốt thời gian và tính phụ thu tự động
    IF v_TrangThaiPhien <> 'Đã kết thúc' THEN
        v_ThoiGianKetThuc := CURRENT_TIMESTAMP;
        
        UPDATE PHIENLAMVIEC
        SET ThoiGianKetThuc = v_ThoiGianKetThuc,
            TrangThaiPhien = 'Đã kết thúc',
            CapNhatLanCuoi = CURRENT_TIMESTAMP
        WHERE MaPhien = TRIM(p_MaPhien);

        -- Xử lý gia hạn giờ tự động cho phiên đặt trước
        IF v_MaDatCho IS NOT NULL THEN
            BEGIN
                SELECT KhoangThoiGianSuDung INTO v_KhoangThoiGianSuDung
                FROM DATCHO WHERE MaDatCho = v_MaDatCho;
                
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
                    
                    SELECT COUNT(*) INTO v_CountDV FROM CHITIETDICHVU 
                    WHERE MaPhien = TRIM(p_MaPhien) AND MaDV = 'DV0000';
                    
                    IF v_CountDV > 0 THEN
                        UPDATE CHITIETDICHVU
                        SET SoLuong = GREATEST(SoLuong, v_SoGioQua)
                        WHERE MaPhien = TRIM(p_MaPhien) AND MaDV = 'DV0000';
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

    v_TongTien := FN_TinhTongTien(TRIM(p_MaPhien));
    v_ThanhTien := FN_TinhThanhTien(TRIM(p_MaPhien), NULL);

    v_SoTienDaTraTruoc := NVL(v_DaTraTruoc, 0);

    IF v_SoTienDaTraTruoc > 0 THEN
        v_ThanhTien := GREATEST(0, v_ThanhTien - v_SoTienDaTraTruoc);
    END IF;

    IF v_ThanhTien <= 0 THEN
        p_outMessage := 'Hóa đơn này không có số tiền cần thanh toán.';
        ROLLBACK;
        RETURN;
    END IF;

    IF v_TrangThaiThanhToan = 'Đã thanh toán thành công' THEN
        IF v_SoTienDaTraTruoc > 0 AND v_ThanhTien <= 0 THEN
            p_outMessage := 'Hóa đơn này đã được thanh toán trước qua đặt chỗ.';
        ELSE
            p_outMessage := 'Lỗi: Hóa đơn đã được thanh toán trước đó.';
        END IF;
        ROLLBACK;
        RETURN;
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

        v_KetQuaPGG := FN_KiemTraPhieuGiamGiaHopLe(TRIM(p_MaPGG), v_TongTien);
        IF v_KetQuaPGG <> 'OK' THEN
            p_outMessage := 'Lỗi: Phiếu giảm giá không hợp lệ (' || v_KetQuaPGG || ').';
            ROLLBACK;
            RETURN;
        END IF;

        v_ThanhTien := FN_TinhThanhTien(TRIM(p_MaPhien), TRIM(p_MaPGG));
        IF v_SoTienDaTraTruoc > 0 THEN
            v_ThanhTien := GREATEST(0, v_ThanhTien - v_SoTienDaTraTruoc);
        END IF;
    END IF;

    -- Removed duplicate UPDATE PHIENLAMVIEC here to avoid overwriting ThoiGianKetThuc.

    UPDATE KHONGGIAN kg
    SET TrangThaiKG =
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM PHIENLAMVIEC p
                WHERE p.MaKG = kg.MaKG
                  AND p.TrangThaiPhien = 'Đang hoạt động'
            ) THEN 'Đang hoạt động'
            WHEN EXISTS (
                SELECT 1
                FROM DATCHO dc
                WHERE dc.MaKG = kg.MaKG
                  AND dc.TrangThaiDatTruoc = 'Đã thanh toán thành công'
                  AND SYSTIMESTAMP <= dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')
            ) THEN 'Đã đặt trước'
            ELSE 'Trống'
        END
    WHERE kg.MaKG = v_MaKG;

    UPDATE HOADON
    SET TongTien = v_TongTien,
        ThanhTien = v_ThanhTien,
        MaPGG = CASE
            WHEN p_MaPGG IS NOT NULL AND LENGTH(TRIM(p_MaPGG)) > 0 THEN TRIM(p_MaPGG)
            ELSE NULL
        END,
        TrangThaiThanhToan = 'Đã thanh toán thành công',
        PhuongThucThanhToan = p_PhuongThucTT,
        NgayLapHoaDon = CURRENT_TIMESTAMP,
        MaNV = p_MaNV
    WHERE MaHoaDon = v_MaHoaDon;

    IF v_CoPGG THEN
        UPDATE PHIEUGIAMGIA
        SET SLDaDung = NVL(SLDaDung, 0) + 1
        WHERE MaPGG = TRIM(p_MaPGG);
    END IF;

    COMMIT;
    p_outMessage := 'Thanh toán thành công. Mã hóa đơn: ' || v_MaHoaDon
        || '. Thành tiền: ' || TO_CHAR(v_ThanhTien, 'FM999G999G999G990') || ' VNĐ';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_outMessage := 'Lỗi: Không tìm thấy phiên, hóa đơn hoặc phiếu giảm giá cần xử lý.';
    WHEN ex_resource_busy THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu thanh toán đang được nhân viên khác xử lý. Vui lòng thử lại sau.';
    WHEN TOO_MANY_ROWS THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu lỗi: phiên [' || p_MaPhien || '] có nhiều hóa đơn, cần cleanup.';
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thanh toán: ' || SQLERRM;
END SP_ThanhToanVoiPhieuGiamGia;
/
