CREATE OR REPLACE PROCEDURE SP_CapNhatKhachHang(
    p_MaKH IN VARCHAR2,
    p_HoTenKH IN VARCHAR2,
    p_MaHangThanhVien IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_Count FROM KHACHHANG WHERE MaKH = p_MaKH;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20130, 'Không tìm thấy hội viên [' || p_MaKH || ']!');
    END IF;

    SELECT COUNT(*) INTO v_Count
    FROM HANGTHANHVIEN WHERE MaHangThanhVien = p_MaHangThanhVien;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20131,
            'Hạng thành viên [' || p_MaHangThanhVien || '] không hợp lệ!');
    END IF;

    -- Cập nhật họ tên ở bảng NGUOIDUNG
    UPDATE NGUOIDUNG
    SET HoTen = p_HoTenKH
    WHERE MaND = (SELECT MaND FROM KHACHHANG WHERE MaKH = p_MaKH);

    -- Cập nhật thông tin hội viên ở bảng KHACHHANG
    UPDATE KHACHHANG
    SET MaHangThanhVien = p_MaHangThanhVien,
        CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaKH = p_MaKH;

    COMMIT;
    p_outMessage := 'Cập nhật hội viên [' || p_MaKH || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi cập nhật hội viên: ' || SQLERRM;
        RAISE;
END SP_CapNhatKhachHang;
/