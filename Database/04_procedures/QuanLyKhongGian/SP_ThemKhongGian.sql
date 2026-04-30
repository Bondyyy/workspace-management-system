CREATE OR REPLACE PROCEDURE SP_ThemKhongGian(
    p_MaKG IN VARCHAR2,
    p_TenKG IN VARCHAR2,
    p_ViTri IN VARCHAR2,
    p_MaLoaiKG IN VARCHAR2,
    p_MaCN IN VARCHAR2,
    p_ToaDoX IN NUMBER DEFAULT 0,
    p_ToaDoY IN NUMBER DEFAULT 0,
    p_ChieuDai IN NUMBER DEFAULT 0,
    p_ChieuRong IN NUMBER DEFAULT 0,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_TrangThaiCN VARCHAR2(50);
    v_MaQR VARCHAR2(500);
BEGIN
    BEGIN
        SELECT TrangThai INTO v_TrangThaiCN
        FROM CHINHANH WHERE MaCN = p_MaCN;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20110, 'Chi nhánh [' || p_MaCN || '] không tồn tại!');
    END;

    IF v_TrangThaiCN != 'Đang hoạt động' THEN
        RAISE_APPLICATION_ERROR(-20111,
            'Chi nhánh [' || p_MaCN || '] đang ngừng hoạt động!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM KHONGGIAN WHERE MaKG = p_MaKG;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20112, 'Mã không gian [' || p_MaKG || '] đã tồn tại!');
    END IF;

    SELECT COUNT(*) INTO v_Count
    FROM KHONGGIAN WHERE TenKG = p_TenKG AND MaCN = p_MaCN;
    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20113,
            'Không gian "' || p_TenKG || '" đã tồn tại trong chi nhánh [' || p_MaCN || ']!');
    END IF;

    SELECT COUNT(*) INTO v_Count FROM LOAIKHONGGIAN WHERE MaLoaiKG = p_MaLoaiKG;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20114, 'Loại không gian [' || p_MaLoaiKG || '] không hợp lệ!');
    END IF;

    v_MaQR := 'QR-' || p_MaKG || '-' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');

    INSERT INTO KHONGGIAN (MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN, ToaDoX, ToaDoY, ChieuDai, ChieuRong)
    VALUES (p_MaKG, p_TenKG, 'Trống', p_ViTri, p_MaLoaiKG, p_MaCN, p_ToaDoX, p_ToaDoY, p_ChieuDai, p_ChieuRong);

    COMMIT;
    p_outMessage := 'Thêm không gian "' || p_TenKG || '" thành công! Mã QR: ' || v_MaQR;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thêm không gian: ' || SQLERRM;
        RAISE;
END SP_ThemKhongGian;
/