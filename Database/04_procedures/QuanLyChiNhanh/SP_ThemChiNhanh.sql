CREATE OR REPLACE PROCEDURE SP_ThemChiNhanh(
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
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20090, 'Mã chi nhánh [' || p_MaCN || '] đã tồn tại!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM CHINHANH WHERE TenCN = p_TenCN;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20091, 'Tên chi nhánh "' || p_TenCN || '" đã được sử dụng!');
    END IF;

    INSERT INTO CHINHANH (
        MaCN, TenCN, DiaChi,
        ThoiGianMoCua, ThoiGianDongCua,
        DuongDayNong, TrangThai
    ) VALUES (
        p_MaCN, p_TenCN, p_DiaChi,
        p_ThoiGianMoCua, p_ThoiGianDongCua,
        p_DuongDayNong, p_TrangThai
    );

    COMMIT;
    p_outMessage := 'Thêm chi nhánh "' || p_TenCN || '" thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thêm chi nhánh: ' || SQLERRM;
        RAISE;
END SP_ThemChiNhanh;
/
