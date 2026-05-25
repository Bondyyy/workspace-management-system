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
    v_SoTienDaTraTruoc NUMBER(18, 2) := 0;
    v_TongTien NUMBER(18, 2);
    v_ThanhTien NUMBER(18, 2);
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

    SELECT MaHoaDon, MaPGG, NVL(DaTraTruoc, 0), TrangThaiThanhToan
    INTO v_MaHoaDon, v_MaPGG, v_DaTraTruoc, v_TrangThaiThanhToan
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

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

    v_TongTien := FN_TinhTongTien(TRIM(p_MaPhien));
    v_SoTienDaTraTruoc := NVL(v_DaTraTruoc, 0);
    v_ThanhTien := FN_TinhThanhTien(TRIM(p_MaPhien), v_MaPGG);
    v_ThanhTien := GREATEST(0, v_ThanhTien - v_SoTienDaTraTruoc);

    IF v_ThanhTien <= 0 THEN
        v_TrangThaiThanhToan := 'Đã thanh toán thành công';
        v_ThanhTien := 0;
    ELSE
        IF v_MaDatCho IS NOT NULL AND v_SoTienDaTraTruoc > 0 THEN
            v_TrangThaiThanhToan := 'Đã trả trước';
        ELSE
            v_TrangThaiThanhToan := 'Đang chờ thanh toán';
        END IF;
    END IF;

    UPDATE HOADON
    SET TongTien = v_TongTien,
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

    COMMIT;

    p_outMessage := 'Kết thúc phiên làm việc thành công! Mã hóa đơn: ' || v_MaHoaDon
        || '. Tổng tiền: ' || TO_CHAR(v_TongTien, 'FM999G999G999G990') || ' VNĐ';

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
