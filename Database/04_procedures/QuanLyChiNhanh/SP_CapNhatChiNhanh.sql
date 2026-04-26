CREATE OR REPLACE PROCEDURE SP_CapNhatChiNhanh(
    p_MaCN IN VARCHAR2,
    p_TenCN IN VARCHAR2,
    p_DiaChi IN VARCHAR2,
    p_ThoiGianMoCua IN VARCHAR2,
    p_ThoiGianDongCua IN VARCHAR2,
    p_DuongDayNong IN VARCHAR2,
    p_MaNV_QuanLy IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_LoaiNV VARCHAR2(50);
    v_TrangThaiNV VARCHAR2(50);
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

    IF p_MaNV_QuanLy IS NOT NULL THEN
        BEGIN
            SELECT LoaiNV, TrangThaiLamViec
            INTO v_LoaiNV, v_TrangThaiNV
            FROM NHANVIEN WHERE MaNV = p_MaNV_QuanLy;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20102,
                    'Nhân viên [' || p_MaNV_QuanLy || '] không tồn tại!');
        END;

        IF v_LoaiNV != 'Quản lý' OR v_TrangThaiNV != 'Đang làm việc' THEN
            RAISE_APPLICATION_ERROR(-20103,
                'Nhân viên [' || p_MaNV_QuanLy || '] không hợp lệ để làm quản lý chi nhánh!');
        END IF;
    END IF;

    UPDATE CHINHANH
    SET TenCN = p_TenCN,
        DiaChi = p_DiaChi,
        ThoiGianMoCua = p_ThoiGianMoCua,
        ThoiGianDongCua = p_ThoiGianDongCua,
        DuongDayNong = p_DuongDayNong
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