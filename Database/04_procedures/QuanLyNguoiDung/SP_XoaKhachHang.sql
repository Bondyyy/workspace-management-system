CREATE OR REPLACE PROCEDURE SP_XoaKhachHang(
    p_MaKH IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_MaND VARCHAR2(50);
BEGIN
    BEGIN
        SELECT MaND INTO v_MaND FROM KHACHHANG WHERE MaKH = p_MaKH;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20140, 'Không tìm thấy hội viên [' || p_MaKH || ']!');
    END;

    SELECT COUNT(*) INTO v_Count FROM PHIENLAMVIEC WHERE MaKH = p_MaKH;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20141,
            'Không thể xóa: Hội viên [' || p_MaKH || '] có '
            || v_Count || ' phiên làm việc trong lịch sử!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM DATCHO WHERE MaKH = p_MaKH;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20142,
            'Không thể xóa: Hội viên [' || p_MaKH || '] có '
            || v_Count || ' lịch sử đặt chỗ liên quan!');
    END IF;

    DELETE FROM KHACHHANG WHERE MaKH = p_MaKH;

    COMMIT;
    p_outMessage := 'Xóa hội viên [' || p_MaKH || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi xóa hội viên: ' || SQLERRM;
        RAISE;
END SP_XoaKhachHang;
/