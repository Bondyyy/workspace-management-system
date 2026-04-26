CREATE OR REPLACE PROCEDURE SP_CapNhatPhieuGiamGia(
    p_MaPGG IN VARCHAR2,
    p_GiaTriGiamGia IN NUMBER,
    p_GiaTriApDungToiThieu IN NUMBER,
    p_NgayBatDauApDung IN TIMESTAMP,
    p_NgayKetThucApDung IN TIMESTAMP,
    p_SLToiDa IN NUMBER,
    p_outMessage OUT VARCHAR2
) AS
    v_Count NUMBER;
    v_SLDaDung NUMBER;
BEGIN
    BEGIN
        SELECT NVL(SLDaDung, 0) INTO v_SLDaDung
        FROM PHIEUGIAMGIA WHERE MaPGG = p_MaPGG;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20150, 'Không tìm thấy phiếu [' || p_MaPGG || ']!');
    END;

    IF p_SLToiDa < v_SLDaDung THEN
        RAISE_APPLICATION_ERROR(-20151,
            'Số lượng tối đa (' || p_SLToiDa
            || ') không được nhỏ hơn số đã dùng (' || v_SLDaDung || ')!');
    END IF;

    IF p_NgayKetThucApDung <= p_NgayBatDauApDung THEN
        RAISE_APPLICATION_ERROR(-20152, 'Ngày kết thúc phải sau ngày bắt đầu!');
    END IF;

    IF p_GiaTriGiamGia <= 0 THEN
        RAISE_APPLICATION_ERROR(-20153, 'Giá trị giảm giá phải lớn hơn 0!');
    END IF;

    UPDATE PHIEUGIAMGIA
    SET GiaTriGiamGia = p_GiaTriGiamGia,
        GiaTriApDungToiThieu = p_GiaTriApDungToiThieu,
        NgayBatDauApDung = p_NgayBatDauApDung,
        NgayKetThucApDung = p_NgayKetThucApDung,
        SLToiDa = p_SLToiDa
    WHERE MaPGG = p_MaPGG;

    COMMIT;
    p_outMessage := 'Cập nhật phiếu [' || p_MaPGG || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi cập nhật phiếu giảm giá: ' || SQLERRM;
        RAISE;
END SP_CapNhatPhieuGiamGia;
/
