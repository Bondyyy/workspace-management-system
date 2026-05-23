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
    v_TongTien NUMBER(18, 2);
    v_ThanhTien NUMBER(18, 2);
    v_SoLuongDaDung NUMBER;
    v_SoLuongToiDa NUMBER;
    v_KetQuaPGG VARCHAR2(4000);
    v_CoPGG BOOLEAN := FALSE;
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
BEGIN
    IF p_MaPhien IS NULL OR LENGTH(TRIM(p_MaPhien)) = 0 THEN
        p_outMessage := 'Lỗi: Thiếu mã phiên cần thanh toán.';
        RETURN;
    END IF;

    IF p_PhuongThucTT IS NULL OR p_PhuongThucTT NOT IN ('Tiền mặt', 'Chuyển khoản') THEN
        p_outMessage := 'Lỗi: Phương thức thanh toán không hợp lệ.';
        RETURN;
    END IF;

    SELECT TrangThaiPhien, MaKG
    INTO v_TrangThaiPhien, v_MaKG
    FROM PHIENLAMVIEC
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    SELECT MaHoaDon, TrangThaiThanhToan
    INTO v_MaHoaDon, v_TrangThaiThanhToan
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    IF v_TrangThaiThanhToan = 'Đã thanh toán thành công' THEN
        p_outMessage := 'Lỗi: Hóa đơn đã được thanh toán trước đó.';
        RETURN;
    END IF;

    v_TongTien := FN_TinhTongTien(TRIM(p_MaPhien));
    v_ThanhTien := FN_TinhThanhTien(TRIM(p_MaPhien), NULL);

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
    END IF;

    UPDATE PHIENLAMVIEC
    SET ThoiGianKetThuc = CURRENT_TIMESTAMP,
        TrangThaiPhien = 'Đã kết thúc',
        CapNhatLanCuoi = CURRENT_TIMESTAMP
    WHERE MaPhien = TRIM(p_MaPhien);

    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Dọn dẹp'
    WHERE MaKG = v_MaKG;

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
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thanh toán: ' || SQLERRM;
END SP_ThanhToanVoiPhieuGiamGia;
/
