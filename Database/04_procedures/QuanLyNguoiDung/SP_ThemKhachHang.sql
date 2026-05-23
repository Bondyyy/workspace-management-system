CREATE OR REPLACE PROCEDURE SP_ThemKhachHang(
    p_MaKH IN VARCHAR2,
    p_HoTenKH IN VARCHAR2,
    p_MaND IN VARCHAR2,
    p_MaHangThanhVien IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_MaKH KHACHHANG.MaKH%TYPE;
BEGIN
    SELECT COUNT(*) INTO v_Count FROM NGUOIDUNG WHERE MaND = p_MaND;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20030, 'Tài khoản người dùng không tồn tại!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM KHACHHANG WHERE MaND = p_MaND;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20031, 'Tài khoản này đã được liên kết với một hội viên khác!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM HANGTHANHVIEN WHERE MaHangThanhVien = p_MaHangThanhVien;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20032, 'Hạng thành viên [' || p_MaHangThanhVien || '] không hợp lệ!');
    END IF;

    IF p_MaKH IS NOT NULL AND LENGTH(TRIM(p_MaKH)) > 0 THEN
        SELECT COUNT(*) INTO v_Count FROM KHACHHANG WHERE MaKH = TRIM(p_MaKH);
        IF v_Count > 0 THEN
            RAISE_APPLICATION_ERROR(-20033, 'Mã hội viên [' || p_MaKH || '] đã tồn tại!');
        END IF;
    END IF;

    INSERT INTO KHACHHANG (
        MaKH, LoaiKH,
        TongChiTieu, CapNhatLanCuoi,
        MaHangThanhVien, MaND
    ) VALUES (
        NULLIF(TRIM(p_MaKH), ''), 'Hội viên',
        0, CURRENT_TIMESTAMP,
        p_MaHangThanhVien, p_MaND
    )
    RETURNING MaKH INTO v_MaKH;

    COMMIT;
    p_outMessage := 'Thêm hội viên thành công! Mã hội viên: ' || v_MaKH;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thêm hội viên: ' || SQLERRM;
        RAISE;
END SP_ThemKhachHang;
/
