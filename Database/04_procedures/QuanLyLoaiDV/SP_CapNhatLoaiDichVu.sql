CREATE OR REPLACE PROCEDURE SP_CapNhatLoaiDichVu(
    p_MaLoaiDV IN VARCHAR2,
    p_TenLoaiDV IN VARCHAR2,
    p_TrangThaiLDV IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_TrangThaiHienTai VARCHAR2(50);
BEGIN
    BEGIN
        SELECT TrangThaiLDV INTO v_TrangThaiHienTai
        FROM LOAIDICHVU WHERE MaLoaiDV = p_MaLoaiDV;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20220,
                'Không tìm thấy loại dịch vụ [' || p_MaLoaiDV || ']!');
    END;

    SELECT COUNT(*) INTO v_Count
    FROM LOAIDICHVU WHERE TenLoaiDV = p_TenLoaiDV AND MaLoaiDV != p_MaLoaiDV;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20221,
            'Tên "' || p_TenLoaiDV || '" đã được dùng bởi loại dịch vụ khác!');
    END IF;

    IF p_TrangThaiLDV NOT IN ('Đang hoạt động', 'Tạm ngưng', 'Ngừng kinh doanh') THEN
        RAISE_APPLICATION_ERROR(-20222, 'Trạng thái không hợp lệ!');
    END IF;

    IF p_TrangThaiLDV IN ('Tạm ngưng', 'Ngừng kinh doanh')
       AND v_TrangThaiHienTai = 'Đang hoạt động' THEN

        SELECT COUNT(*) INTO v_Count
        FROM DICHVU
        WHERE MaLoaiDV = p_MaLoaiDV
          AND TrangThaiDV = 'Đang hoạt động';

        IF v_Count > 0 THEN
            RAISE_APPLICATION_ERROR(-20223,
                'Không thể vô hiệu hóa: Loại dịch vụ [' || p_MaLoaiDV
                || '] còn ' || v_Count || ' dịch vụ đang hoạt động!');
        END IF;
    END IF;

    UPDATE LOAIDICHVU
    SET TenLoaiDV = p_TenLoaiDV,
        TrangThaiLDV = p_TrangThaiLDV
    WHERE MaLoaiDV = p_MaLoaiDV;

    COMMIT;
    p_outMessage := 'Cập nhật loại dịch vụ [' || p_MaLoaiDV || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi cập nhật loại dịch vụ: ' || SQLERRM;
        RAISE;
END SP_CapNhatLoaiDichVu;
/
