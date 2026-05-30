CREATE OR REPLACE PROCEDURE SP_ThanhToanVoiPhieuGiamGia(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_MaPGG IN VARCHAR2,
    p_PhuongThucTT IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_MaHoaDon HOADON.MaHoaDon%TYPE;
    v_MaKG PHIENLAMVIEC.MaKG%TYPE;
    v_MaDatCho PHIENLAMVIEC.MaDatCho%TYPE;
    v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien%TYPE;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE;
    v_TienDatChoDaTra NUMBER(18, 2) := 0;
    v_TongTien NUMBER(18, 2) := 0;
    v_TienVoucherCu NUMBER(18, 2) := 0;
    v_TienVoucherMoi NUMBER(18, 2) := 0;
    v_TienVoucherTong NUMBER(18, 2) := 0;
    v_PhanTramHang NUMBER(5, 2) := 0;
    v_TienGiamHang NUMBER(18, 2) := 0;
    v_ThanhTienCuoi NUMBER(18, 2) := 0;
    v_ConPhaiThu NUMBER(18, 2) := 0;
    v_CoSoVoucherTaiQuay NUMBER(18, 2) := 0;
    v_PhuongThucThanhToan VARCHAR2(50);
    v_CoPGG CHAR(1) := 'N';
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

    SELECT TrangThaiPhien, MaKG, MaDatCho
    INTO v_TrangThaiPhien, v_MaKG, v_MaDatCho
    FROM PHIENLAMVIEC
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    IF v_TrangThaiPhien <> 'Đã kết thúc' THEN
        ROLLBACK;
        p_outMessage := 'Phiên chưa kết thúc. Vui lòng kết thúc phiên trước khi thanh toán.';
        RETURN;
    END IF;

    SELECT MaHoaDon, TrangThaiThanhToan
    INTO v_MaHoaDon, v_TrangThaiThanhToan
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    IF v_TrangThaiThanhToan = 'Đã thanh toán thành công' THEN
        ROLLBACK;
        p_outMessage := 'Lỗi: Hóa đơn đã được thanh toán trước đó.';
        RETURN;
    END IF;

    IF v_MaDatCho IS NOT NULL THEN
        BEGIN
            SELECT NVL(ThanhTien, 0)
            INTO v_TienDatChoDaTra
            FROM DATCHO
            WHERE MaDatCho = v_MaDatCho;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_TienDatChoDaTra := 0;
        END;
    END IF;

    v_TongTien := GREATEST(0, FN_TinhTongTien(TRIM(p_MaPhien)));

    SELECT NVL(SUM(NVL(ct.SoTienGiam, 0)), 0)
    INTO v_TienVoucherCu
    FROM CHITIETAPDUNGPGG ct
    WHERE ct.NguonApDung = 'DAT_TRUOC'
      AND (ct.MaHoaDon = v_MaHoaDon OR (v_MaDatCho IS NOT NULL AND ct.MaDatCho = v_MaDatCho));

    v_CoSoVoucherTaiQuay := GREATEST(0, v_TongTien - v_TienVoucherCu - NVL(v_TienDatChoDaTra, 0));

    IF p_MaPGG IS NOT NULL AND LENGTH(TRIM(p_MaPGG)) > 0 AND v_CoSoVoucherTaiQuay > 0 THEN
        v_CoPGG := 'Y';

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

        v_KetQuaPGG := FN_KiemTraPhieuGiamGiaHopLe(TRIM(p_MaPGG), v_CoSoVoucherTaiQuay);
        IF v_KetQuaPGG <> 'OK' THEN
            p_outMessage := 'Lỗi: Phiếu giảm giá không hợp lệ (' || v_KetQuaPGG || ').';
            ROLLBACK;
            RETURN;
        END IF;

        SELECT LEAST(NVL(GiaTriGiamGia, 0), v_CoSoVoucherTaiQuay)
        INTO v_TienVoucherMoi
        FROM PHIEUGIAMGIA
        WHERE MaPGG = TRIM(p_MaPGG);

        DELETE FROM CHITIETAPDUNGPGG
        WHERE MaHoaDon = v_MaHoaDon
          AND NguonApDung = 'TAI_QUAY';

        INSERT INTO CHITIETAPDUNGPGG (
            MaApDung, MaPGG, MaDatCho, MaHoaDon, NguonApDung, SoTienGiam, ThoiGianApDung
        ) VALUES (
            RAWTOHEX(SYS_GUID()), TRIM(p_MaPGG), NULL, v_MaHoaDon, 'TAI_QUAY',
            v_TienVoucherMoi, CURRENT_TIMESTAMP
        );
    ELSE
        DELETE FROM CHITIETAPDUNGPGG
        WHERE MaHoaDon = v_MaHoaDon
          AND NguonApDung = 'TAI_QUAY';
    END IF;

    SELECT NVL(SUM(NVL(ct.SoTienGiam, 0)), 0)
    INTO v_TienVoucherTong
    FROM CHITIETAPDUNGPGG ct
    WHERE ct.MaHoaDon = v_MaHoaDon
       OR (v_MaDatCho IS NOT NULL AND ct.MaDatCho = v_MaDatCho);

    SELECT NVL(MAX(htv.PhanTramTienGiam), 0)
    INTO v_PhanTramHang
    FROM PHIENLAMVIEC plv
    LEFT JOIN KHACHHANG kh ON kh.MaKH = plv.MaKH
    LEFT JOIN HANGTHANHVIEN htv ON htv.MaHangThanhVien = kh.MaHangThanhVien
    WHERE plv.MaPhien = TRIM(p_MaPhien);

    v_PhanTramHang := LEAST(100, GREATEST(0, NVL(v_PhanTramHang, 0)));
    v_TienGiamHang := ROUND(GREATEST(0, v_TongTien - v_TienVoucherTong) * v_PhanTramHang / 100, 0);
    v_ThanhTienCuoi := GREATEST(0, v_TongTien - v_TienVoucherTong - v_TienGiamHang);
    v_ConPhaiThu := GREATEST(0, v_ThanhTienCuoi - NVL(v_TienDatChoDaTra, 0));

    IF v_ConPhaiThu > 0 AND p_PhuongThucTT = 'Đặt trước' THEN
        p_outMessage := 'Lỗi: Hóa đơn còn tiền cần thu tại quầy, vui lòng chọn Tiền mặt hoặc Chuyển khoản.';
        ROLLBACK;
        RETURN;
    END IF;

    v_PhuongThucThanhToan := CASE
        WHEN v_ConPhaiThu <= 0 AND NVL(v_TienDatChoDaTra, 0) > 0 THEN 'Đặt trước'
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

    UPDATE HOADON
    SET TongTien = v_TongTien,
        ThanhTien = 0,
        MaPGG = CASE WHEN v_CoPGG = 'Y' THEN TRIM(p_MaPGG) ELSE NULL END,
        TrangThaiThanhToan = 'Đã thanh toán thành công',
        PhuongThucThanhToan = v_PhuongThucThanhToan,
        NgayLapHoaDon = CURRENT_TIMESTAMP,
        MaNV = p_MaNV
    WHERE MaHoaDon = v_MaHoaDon;

    IF v_CoPGG = 'Y' AND v_TienVoucherMoi > 0 THEN
        UPDATE PHIEUGIAMGIA
        SET SLDaDung = NVL(SLDaDung, 0) + 1
        WHERE MaPGG = TRIM(p_MaPGG);
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
/
