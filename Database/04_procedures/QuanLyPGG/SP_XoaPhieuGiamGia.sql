CREATE OR REPLACE PROCEDURE SP_XoaPhieuGiamGia(
    p_MaPGG IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_SLDaDung NUMBER;
    v_MaChuSoPGG VARCHAR2(100);
    v_SoHDLienQuan NUMBER;
BEGIN
    BEGIN
        SELECT MaChuSoPGG, NVL(SLDaDung, 0)
        INTO v_MaChuSoPGG, v_SLDaDung
        FROM PHIEUGIAMGIA WHERE MaPGG = p_MaPGG;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20160, 'Không tìm thấy phiếu [' || p_MaPGG || ']!');
    END;

    SELECT COUNT(*) INTO v_SoHDLienQuan
    FROM HOADON
    WHERE MaPGG = p_MaPGG AND TrangThaiThanhToan != 'Thành công';

    IF v_SoHDLienQuan > 0 THEN
        RAISE_APPLICATION_ERROR(-20161,
            'Không thể xóa: Phiếu [' || v_MaChuSoPGG || '] đang áp dụng trong '
            || v_SoHDLienQuan || ' hóa đơn chưa thanh toán!');
    END IF;

    DELETE FROM PHIEUGIAMGIA WHERE MaPGG = p_MaPGG;

    COMMIT;
    p_outMessage := 'Xóa phiếu [' || v_MaChuSoPGG || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi xóa phiếu giảm giá: ' || SQLERRM;
        RAISE;
END SP_XoaPhieuGiamGia;
/
