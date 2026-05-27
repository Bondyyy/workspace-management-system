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
    v_KhoangThoiGianSuDung NUMBER := 0;
    v_TongPhut NUMBER := 0;
    v_SoGioThucTe NUMBER := 0;
    v_SoGioQua NUMBER := 0;
    v_CountDV NUMBER := 0;
    v_MaHoaDon HOADON.MaHoaDon%TYPE;
    v_SoHoaDon NUMBER := 0;
    v_TongTien NUMBER(18, 2) := 0;
    v_TienVoucher NUMBER(18, 2) := 0;
    v_PhanTramHang NUMBER(5, 2) := 0;
    v_TienGiamHang NUMBER(18, 2) := 0;
    v_ThanhTienCuoi NUMBER(18, 2) := 0;
    v_TienDatChoDaTra NUMBER(18, 2) := 0;
    v_ConPhaiThu NUMBER(18, 2) := 0;
    v_TrangThaiThanhToan HOADON.TrangThaiThanhToan%TYPE;
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
            TongTien, ThanhTien, NgayLapHoaDon, TrangThaiThanhToan, PhuongThucThanhToan, MaPhien, MaNV
        ) VALUES (
            0, 0, CURRENT_TIMESTAMP, 'Đang chờ thanh toán', NULL, TRIM(p_MaPhien), p_MaNV
        );
    ELSIF v_SoHoaDon > 1 THEN
        ROLLBACK;
        p_outMessage := 'Dữ liệu lỗi: phiên [' || TRIM(p_MaPhien) || '] có nhiều hóa đơn.';
        RETURN;
    END IF;

    SELECT MaHoaDon
    INTO v_MaHoaDon
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
            SELECT NVL(KhoangThoiGianSuDung, 0), NVL(ThanhTien, 0)
            INTO v_KhoangThoiGianSuDung, v_TienDatChoDaTra
            FROM DATCHO
            WHERE MaDatCho = v_MaDatCho;

            v_TongPhut := EXTRACT(DAY FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 24 * 60
                + EXTRACT(HOUR FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) * 60
                + EXTRACT(MINUTE FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau))
                + EXTRACT(SECOND FROM (v_ThoiGianKetThuc - v_ThoiGianBatDau)) / 60;
            v_SoGioThucTe := CASE
                WHEN MOD(v_TongPhut, 60) <= 15 THEN TRUNC(v_TongPhut / 60)
                ELSE TRUNC(v_TongPhut / 60) + 1
            END;

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
                v_TienDatChoDaTra := 0;
        END;
    END IF;

    v_TongTien := GREATEST(0, FN_TinhTongTien(TRIM(p_MaPhien)));

    SELECT NVL(SUM(NVL(ct.SoTienGiam, 0)), 0)
    INTO v_TienVoucher
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
    v_TienGiamHang := ROUND(GREATEST(0, v_TongTien - v_TienVoucher) * v_PhanTramHang / 100, 0);
    v_ThanhTienCuoi := GREATEST(0, v_TongTien - v_TienVoucher - v_TienGiamHang);
    v_ConPhaiThu := GREATEST(0, v_ThanhTienCuoi - NVL(v_TienDatChoDaTra, 0));
    v_TrangThaiThanhToan := CASE
        WHEN v_MaDatCho IS NOT NULL AND v_ConPhaiThu = 0 THEN 'Đã thanh toán thành công'
        ELSE 'Đang chờ thanh toán'
    END;

    UPDATE HOADON
    SET TongTien = v_TongTien,
        ThanhTien = v_ConPhaiThu,
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
        || '. Tổng tiền: ' || TO_CHAR(v_TongTien, 'FM999G999G999G990') || ' VNĐ'
        || '. Còn phải thu: ' || TO_CHAR(v_ConPhaiThu, 'FM999G999G999G990') || ' VNĐ';

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
