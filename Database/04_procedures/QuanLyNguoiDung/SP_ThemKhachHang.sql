CREATE OR REPLACE PROCEDURE SP_ThemKhachHang(
    p_MaKH IN VARCHAR2,
    p_HoTenKH IN VARCHAR2,
    p_MaND IN VARCHAR2,
    p_MaHangThanhVien IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_MaKH KHACHHANG.MaKH%TYPE;
    v_MaHangMacDinh HANGTHANHVIEN.MaHangThanhVien%TYPE;
    v_MaHangCanDung HANGTHANHVIEN.MaHangThanhVien%TYPE;
BEGIN
    SELECT COUNT(*) INTO v_Count FROM NGUOIDUNG WHERE MaND = p_MaND;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20030, 'Tài khoản người dùng không tồn tại!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM KHACHHANG WHERE MaND = p_MaND;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20031, 'Tài khoản này đã được liên kết với một hội viên khác!');
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
        v_MaHangCanDung, p_MaND
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
