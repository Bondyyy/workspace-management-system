CREATE OR REPLACE PROCEDURE SP_CapNhatChiNhanh(
    p_MaCN IN VARCHAR2,
    p_TenCN IN VARCHAR2,
    p_DiaChi IN VARCHAR2,
    p_ThoiGianMoCua IN VARCHAR2,
    p_ThoiGianDongCua IN VARCHAR2,
    p_DuongDayNong IN VARCHAR2,
    p_TrangThai IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_Count FROM CHINHANH WHERE MaCN = p_MaCN;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20100, 'Không tìm thấy chi nhánh [' || p_MaCN || ']!');
    END IF;

    SELECT COUNT(*) INTO v_Count
    FROM CHINHANH WHERE TenCN = p_TenCN AND MaCN != p_MaCN;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20101,
            'Tên "' || p_TenCN || '" đã được dùng bởi chi nhánh khác!');
    END IF;

    UPDATE CHINHANH
    SET TenCN = p_TenCN,
        DiaChi = p_DiaChi,
        ThoiGianMoCua = p_ThoiGianMoCua,
        ThoiGianDongCua = p_ThoiGianDongCua,
        DuongDayNong = p_DuongDayNong,
        TrangThai = p_TrangThai
    WHERE MaCN = p_MaCN;

    COMMIT;
    p_outMessage := 'Cập nhật chi nhánh [' || p_MaCN || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi cập nhật chi nhánh: ' || SQLERRM;
        RAISE;
END SP_CapNhatChiNhanh;
/