CREATE OR REPLACE PROCEDURE SP_DangKy(
    p_MaND IN VARCHAR2,
    p_TenTaiKhoan IN VARCHAR2,
    p_MatKhauMaHoa IN VARCHAR2,
    p_Email IN VARCHAR2,
    p_SDT IN VARCHAR2,
    p_GioiTinh IN VARCHAR2,
    p_NgaySinh IN DATE,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_Count FROM NGUOIDUNG WHERE TenTaiKhoan = p_TenTaiKhoan;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20020, 'Tên tài khoản đã được sử dụng!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM NGUOIDUNG WHERE Email = p_Email;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20021, 'Email đã được sử dụng!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM NGUOIDUNG WHERE SDT = p_SDT;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20022, 'Số điện thoại đã được sử dụng!');
    END IF;

    INSERT INTO NGUOIDUNG (
        MaND, TenTaiKhoan, MatKhauMaHoa,
        Email, SDT, GioiTinh, NgaySinh,
        ThoiGianTao, CapNhatLanCuoi, TrangThaiND
    ) VALUES (
        p_MaND, p_TenTaiKhoan, p_MatKhauMaHoa,
        p_Email, p_SDT, p_GioiTinh, p_NgaySinh,
        SYSTIMESTAMP, SYSTIMESTAMP, 'Đang hoạt động'
    );

    COMMIT;
    p_outMessage := 'Đăng ký tài khoản thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi đăng ký: ' || SQLERRM;
        RAISE;
END SP_DangKy;
/