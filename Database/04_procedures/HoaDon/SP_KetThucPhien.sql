CREATE OR REPLACE PROCEDURE SP_KetThucPhien(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien%TYPE;
    v_MaKG PHIENLAMVIEC.MaKG%TYPE;
    v_MaHoaDon HOADON.MaHoaDon%TYPE;
    v_TongTien NUMBER(18, 2);
    v_ThanhTien NUMBER(18, 2);
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
BEGIN
    SELECT TrangThaiPhien, MaKG
    INTO v_TrangThaiPhien, v_MaKG
    FROM PHIENLAMVIEC
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    IF v_TrangThaiPhien <> 'Đang hoạt động' THEN
        p_outMessage := 'Phiên làm việc [' || p_MaPhien || '] không ở trạng thái đang hoạt động. Trạng thái hiện tại: '
            || v_TrangThaiPhien;
        RETURN;
    END IF;

    SELECT MaHoaDon
    INTO v_MaHoaDon
    FROM HOADON
    WHERE MaPhien = TRIM(p_MaPhien)
    FOR UPDATE NOWAIT;

    UPDATE PHIENLAMVIEC
    SET ThoiGianKetThuc = SYSTIMESTAMP,
        TrangThaiPhien = 'Đã kết thúc',
        CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaPhien = TRIM(p_MaPhien);

    v_TongTien := FN_TinhTongTien(TRIM(p_MaPhien));
    v_ThanhTien := FN_TinhThanhTien(TRIM(p_MaPhien), NULL);

    UPDATE HOADON
    SET TongTien = v_TongTien,
        ThanhTien = v_ThanhTien,
        MaNV = p_MaNV,
        NgayLapHoaDon = SYSTIMESTAMP
    WHERE MaHoaDon = v_MaHoaDon;

    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Dọn dẹp'
    WHERE MaKG = v_MaKG;

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
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi kết thúc phiên: ' || SQLERRM;
END SP_KetThucPhien;
/
