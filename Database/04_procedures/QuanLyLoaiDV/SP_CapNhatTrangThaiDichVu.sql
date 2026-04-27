CREATE OR REPLACE PROCEDURE SP_CapNhatTrangThaiDichVu(
    p_MaDV IN VARCHAR2,
    p_TrangThaiMoi IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_TrangThaiHienTai VARCHAR2(50);
    v_TenDV VARCHAR2(100);
BEGIN
    BEGIN
        SELECT TrangThaiDV, TenDV
        INTO v_TrangThaiHienTai, v_TenDV
        FROM DICHVU WHERE MaDV = p_MaDV;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20230, 'Không tìm thấy dịch vụ [' || p_MaDV || ']!');
    END;

    IF p_TrangThaiMoi NOT IN ('Đang hoạt động', 'Tạm ngưng', 'Ngừng kinh doanh') THEN
        RAISE_APPLICATION_ERROR(-20231,
            'Trạng thái "' || p_TrangThaiMoi
            || '" không hợp lệ! Chỉ chấp nhận: Đang hoạt động / Tạm ngưng / Ngừng kinh doanh');
    END IF;

    IF p_TrangThaiMoi = v_TrangThaiHienTai THEN
        RAISE_APPLICATION_ERROR(-20232,
            'Dịch vụ đã ở trạng thái "' || v_TrangThaiHienTai || '" rồi!');
    END IF;

    IF p_TrangThaiMoi = 'Đang hoạt động' THEN
        SELECT COUNT(*) INTO v_Count
        FROM DICHVU DV
        JOIN LOAIDICHVU LDV ON DV.MaLoaiDV = LDV.MaLoaiDV
        WHERE DV.MaDV = p_MaDV
          AND LDV.TrangThaiLDV != 'Đang hoạt động';

        IF v_Count > 0 THEN
            RAISE_APPLICATION_ERROR(-20233,
                'Không thể kích hoạt: Loại dịch vụ cha của ['
                || p_MaDV || '] đang không hoạt động!');
        END IF;
    END IF;

    UPDATE DICHVU
    SET TrangThaiDV = p_TrangThaiMoi
    WHERE MaDV = p_MaDV;

    COMMIT;
    p_outMessage := 'Cập nhật trạng thái dịch vụ "' || v_TenDV
                    || '" → "' || p_TrangThaiMoi || '" thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi cập nhật trạng thái dịch vụ: ' || SQLERRM;
        RAISE;
END SP_CapNhatTrangThaiDichVu;
/