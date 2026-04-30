CREATE OR REPLACE PROCEDURE SP_CapNhatKhongGian(
    p_MaKG IN VARCHAR2,
    p_TenKG IN VARCHAR2,
    p_ViTri IN VARCHAR2,
    p_MaLoaiKG IN VARCHAR2,
    p_TrangThaiKG IN VARCHAR2,
    p_ToaDoX IN NUMBER DEFAULT 0,
    p_ToaDoY IN NUMBER DEFAULT 0,
    p_ChieuDai IN NUMBER DEFAULT 0,
    p_ChieuRong IN NUMBER DEFAULT 0,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_MaCN VARCHAR2(50);
BEGIN
    -- 1. Lấy mã chi nhánh để kiểm tra trùng tên
    BEGIN
        SELECT MaCN INTO v_MaCN
        FROM KHONGGIAN WHERE MaKG = p_MaKG;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20200, 'Không tìm thấy không gian [' || p_MaKG || ']!');
    END;

    -- 2. Kiểm tra trùng tên (trừ chính nó)
    SELECT COUNT(*) INTO v_Count
    FROM KHONGGIAN
    WHERE TenKG = p_TenKG
      AND MaCN = v_MaCN
      AND MaKG != p_MaKG;

    IF v_Count > 0 THEN
        RAISE_APPLICATION_ERROR(-20201,
            'Tên "' || p_TenKG || '" đã tồn tại trong chi nhánh [' || v_MaCN || ']!');
    END IF;

    -- 3. Kiểm tra loại không gian hợp lệ
    SELECT COUNT(*) INTO v_Count FROM LOAIKHONGGIAN WHERE MaLoaiKG = p_MaLoaiKG;
    IF v_Count = 0 THEN
        RAISE_APPLICATION_ERROR(-20202, 'Loại không gian [' || p_MaLoaiKG || '] không hợp lệ!');
    END IF;

    -- 4. Thực hiện cập nhật
    UPDATE KHONGGIAN
    SET TenKG = p_TenKG,
        ViTri = p_ViTri,
        MaLoaiKG = p_MaLoaiKG,
        TrangThaiKG = p_TrangThaiKG,
        ToaDoX = p_ToaDoX,
        ToaDoY = p_ToaDoY,
        ChieuDai = p_ChieuDai,
        ChieuRong = p_ChieuRong
    WHERE MaKG = p_MaKG;

    COMMIT;
    p_outMessage := 'Cập nhật không gian [' || p_MaKG || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi cập nhật không gian: ' || SQLERRM;
        RAISE;
END SP_CapNhatKhongGian;
/
