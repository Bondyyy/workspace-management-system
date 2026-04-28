CREATE OR REPLACE PROCEDURE SP_KetThucPhien(
    p_MaPhien IN VARCHAR2,
    p_MaNV IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_TrangThaiPhien VARCHAR2(50);
    v_MaHoaDon VARCHAR2(50);
    v_TongTien NUMBER(18, 2);
    v_ThanhTien NUMBER(18, 2);
BEGIN
    -- 1. Kiểm tra trạng thái phiên
    BEGIN
        SELECT TrangThaiPhien
        INTO v_TrangThaiPhien
        FROM PHIENLAMVIEC
        WHERE MaPhien = p_MaPhien;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20300, 'Không tìm thấy phiên làm việc [' || p_MaPhien || ']!');
    END;
    
    -- 2. Kiểm tra phiên đang hoạt động
    IF v_TrangThaiPhien != 'Đang hoạt động' THEN
        RAISE_APPLICATION_ERROR(-20301, 
            'Phiên làm việc [' || p_MaPhien || '] đã kết thúc trước đó! '
            || 'Trạng thái hiện tại: ' || v_TrangThaiPhien);
    END IF;
    
    -- 3. Cập nhật thời gian kết thúc phiên
    UPDATE PHIENLAMVIEC
    SET ThoiGianKetThuc = SYSTIMESTAMP,
        TrangThaiPhien = 'Đã kết thúc',
        CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaPhien = p_MaPhien;
    
    -- 4. Tính toán lại hóa đơn với thời gian kết thúc thực tế
    v_TongTien := FN_TinhTongTien(p_MaPhien);
    v_ThanhTien := FN_TinhThanhTien(p_MaPhien, NULL); -- Chưa có phiếu giảm giá
    
    -- 5. Cập nhật hóa đơn
    UPDATE HOADON
    SET TongTien = v_TongTien,
        ThanhTien = v_ThanhTien,
        MaNV = p_MaNV,
        NgayLapHoaDon = SYSTIMESTAMP
    WHERE MaPhien = p_MaPhien
    RETURNING MaHoaDon INTO v_MaHoaDon;
    
    -- 6. Cập nhật trạng thái không gian về "Dọn dẹp"
    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Dọn dẹp'
    WHERE MaKG = (SELECT MaKG FROM PHIENLAMVIEC WHERE MaPhien = p_MaPhien);
    
    COMMIT;
    
    p_outMessage := 'Kết thúc phiên làm việc thành công! ' 
                    || 'Mã hóa đơn: ' || v_MaHoaDon || '. '
                    || 'Tổng tiền: ' || TO_CHAR(v_TongTien, 'FM999,999,999') || ' VNĐ';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi kết thúc phiên: ' || SQLERRM;
        RAISE;
END SP_KetThucPhien;
/
