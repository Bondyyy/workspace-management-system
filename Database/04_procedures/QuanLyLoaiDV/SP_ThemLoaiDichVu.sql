CREATE OR REPLACE PROCEDURE SP_ThemLoaiDichVu(
    p_MaLoaiDV IN VARCHAR2,
    p_TenLoaiDV IN VARCHAR2,
    p_TrangThaiLDV IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
BEGIN
    IF p_MaLoaiDV IS NOT NULL AND LENGTH(TRIM(p_MaLoaiDV)) > 0 THEN
        SELECT COUNT(*) INTO v_Count FROM LOAIDICHVU WHERE MaLoaiDV = TRIM(p_MaLoaiDV);
        IF v_Count > 0 THEN
            RAISE_APPLICATION_ERROR(-20210, 'Mã loại dịch vụ [' || p_MaLoaiDV || '] đã tồn tại!');
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_Count FROM LOAIDICHVU WHERE TenLoaiDV = p_TenLoaiDV;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20211, 'Tên loại dịch vụ "' || p_TenLoaiDV || '" đã tồn tại!');
    END IF;

    IF p_TrangThaiLDV NOT IN ('Đang hoạt động', 'Tạm ngưng', 'Ngừng kinh doanh') THEN
        RAISE_APPLICATION_ERROR(-20212,
            'Trạng thái không hợp lệ! Chỉ chấp nhận: Đang hoạt động / Tạm ngưng / Ngừng kinh doanh');
    END IF;

    INSERT INTO LOAIDICHVU (MaLoaiDV, TenLoaiDV, TrangThaiLDV)
    VALUES (NULLIF(TRIM(p_MaLoaiDV), ''), p_TenLoaiDV, p_TrangThaiLDV);

    COMMIT;
    p_outMessage := 'Thêm loại dịch vụ "' || p_TenLoaiDV || '" thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thêm loại dịch vụ: ' || SQLERRM;
        RAISE;
END SP_ThemLoaiDichVu;
/
