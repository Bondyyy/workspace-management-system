CREATE OR REPLACE PROCEDURE SP_CapNhatKhongGian(
    p_MaKG IN VARCHAR2,
    p_TenKG IN VARCHAR2,
    p_ViTri IN VARCHAR2,
    p_MaLoaiKG IN VARCHAR2,
    p_TrangThaiKG IN VARCHAR2,
    p_ToaDoX IN NUMBER,
    p_ToaDoY IN NUMBER,
    p_ChieuDai IN NUMBER,
    p_ChieuRong IN NUMBER,
    p_outMessage OUT VARCHAR2
) AS
BEGIN
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

    IF SQL%ROWCOUNT = 0 THEN
        ROLLBACK;
        p_outMessage := 'Không tìm thấy không gian [' || p_MaKG || '] để cập nhật!';
        RETURN;
    END IF;

    COMMIT;
    p_outMessage := 'Cập nhật không gian [' || p_MaKG || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi cập nhật không gian: ' || SQLERRM;
        RAISE;
END SP_CapNhatKhongGian;
/
