CREATE OR REPLACE PROCEDURE SP_ThemPhieuGiamGia(
    p_MaPGG IN VARCHAR2,
    p_MaChuSoPGG IN VARCHAR2,
    p_GiaTriGiamGia IN NUMBER,
    p_GiaTriApDungToiThieu IN NUMBER,
    p_NgayBatDauApDung IN TIMESTAMP,
    p_NgayKetThucApDung IN TIMESTAMP,
    p_SLToiDa IN NUMBER,
    p_MaNV IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_Count FROM PHIEUGIAMGIA WHERE MaPGG = p_MaPGG;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20040, 'Mã phiếu [' || p_MaPGG || '] đã tồn tại!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM PHIEUGIAMGIA WHERE MaChuSoPGG = p_MaChuSoPGG;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20041,
            'Mã chữ số [' || p_MaChuSoPGG || '] đã được sử dụng. Vui lòng nhập mã khác!');
    END IF;

    IF p_NgayKetThucApDung <= p_NgayBatDauApDung THEN
        RAISE_APPLICATION_ERROR(-20042, 'Ngày kết thúc phải sau ngày bắt đầu áp dụng!');
    END IF;

    IF p_GiaTriGiamGia <= 0 THEN
        RAISE_APPLICATION_ERROR(-20043, 'Giá trị giảm giá phải lớn hơn 0!');
    END IF;

    IF p_SLToiDa <= 0 THEN
        RAISE_APPLICATION_ERROR(-20044, 'Số lượng phát hành phải lớn hơn 0!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM NHANVIEN WHERE MaNV = p_MaNV;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20045, 'Nhân viên [' || p_MaNV || '] không tồn tại!');
    END IF;

    INSERT INTO PHIEUGIAMGIA (
        MaPGG, MaChuSoPGG, GiaTriGiamGia,
        GiaTriApDungToiThieu, NgayBatDauApDung, NgayKetThucApDung,
        SLDaDung, SLToiDa, NgayTaoPGG, MaNV
    ) VALUES (
        p_MaPGG, p_MaChuSoPGG, p_GiaTriGiamGia,
        p_GiaTriApDungToiThieu, p_NgayBatDauApDung, p_NgayKetThucApDung,
        0, p_SLToiDa, SYSTIMESTAMP, p_MaNV
    );

    COMMIT;
    p_outMessage := 'Tạo phiếu giảm giá [' || p_MaChuSoPGG || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi tạo phiếu giảm giá: ' || SQLERRM;
        RAISE;
END SP_ThemPhieuGiamGia;
/
