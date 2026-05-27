CREATE OR REPLACE PROCEDURE SP_CapNhatKhachHang(
    p_MaKH IN VARCHAR2,
    p_HoTenKH IN VARCHAR2,
    p_MaHangThanhVien IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_MaHangMacDinh HANGTHANHVIEN.MaHangThanhVien%TYPE;
    v_MaHangCanDung HANGTHANHVIEN.MaHangThanhVien%TYPE;
BEGIN
    SELECT COUNT(*) INTO v_Count FROM KHACHHANG WHERE MaKH = p_MaKH;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20130, 'Không tìm thấy hội viên [' || p_MaKH || ']!');
    END IF;

    BEGIN
        SELECT MaHangThanhVien INTO v_MaHangMacDinh
        FROM HANGTHANHVIEN
        WHERE TenHangThanhVien = 'Đồng'
          AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            SELECT MaHangThanhVien INTO v_MaHangMacDinh
            FROM (
                SELECT MaHangThanhVien
                FROM HANGTHANHVIEN
                WHERE TenHangThanhVien <> 'Không có'
                ORDER BY NVL(TongChiTieuToiThieu, 0), MaHangThanhVien
            )
            WHERE ROWNUM = 1;
    END;

    v_MaHangCanDung := NULLIF(TRIM(p_MaHangThanhVien), '');
    IF v_MaHangCanDung IS NULL THEN
        v_MaHangCanDung := v_MaHangMacDinh;
    END IF;

    SELECT COUNT(*) INTO v_Count
    FROM HANGTHANHVIEN
    WHERE MaHangThanhVien = v_MaHangCanDung
      AND TenHangThanhVien <> 'Không có';
    IF v_Count = 0 THEN
        v_MaHangCanDung := v_MaHangMacDinh;
    END IF;

    -- Cập nhật họ tên ở bảng NGUOIDUNG
    UPDATE NGUOIDUNG
    SET HoTen = p_HoTenKH
    WHERE MaND = (SELECT MaND FROM KHACHHANG WHERE MaKH = p_MaKH);

    -- Cập nhật thông tin hội viên ở bảng KHACHHANG
    UPDATE KHACHHANG
    SET MaHangThanhVien = v_MaHangCanDung,
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
