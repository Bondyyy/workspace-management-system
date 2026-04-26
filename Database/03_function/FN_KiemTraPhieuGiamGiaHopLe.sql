CREATE OR REPLACE FUNCTION FN_KiemTraPhieuGiamGiaHopLe(
    p_MaPGG IN VARCHAR2,
    p_ThanhTien IN NUMBER
) RETURN VARCHAR2
AS
    v_GiaTriGiamGia NUMBER;
    v_GiaTriToiThieu NUMBER;
    v_NgayBatDau TIMESTAMP;
    v_NgayKetThuc TIMESTAMP;
    v_SLDaDung NUMBER;
    v_SLToiDa NUMBER;
BEGIN
    SELECT GiaTriGiamGia, GiaTriApDungToiThieu,
           NgayBatDauApDung, NgayKetThucApDung,
           NVL(SLDaDung, 0), SLToiDa
    INTO v_GiaTriGiamGia, v_GiaTriToiThieu,
         v_NgayBatDau, v_NgayKetThuc,
         v_SLDaDung, v_SLToiDa
    FROM PHIEUGIAMGIA
    WHERE MaPGG = p_MaPGG;

    IF SYSTIMESTAMP < v_NgayBatDau THEN
        RETURN 'Lỗi: Phiếu giảm giá chưa đến ngày áp dụng!';
    END IF;

    IF SYSTIMESTAMP > v_NgayKetThuc THEN
        RETURN 'Lỗi: Phiếu giảm giá đã hết hạn sử dụng!';
    END IF;

    IF v_SLDaDung >= v_SLToiDa THEN
        RETURN 'Lỗi: Phiếu giảm giá đã hết lượt sử dụng!';
    END IF;

    IF p_ThanhTien < v_GiaTriToiThieu THEN
        RETURN 'Lỗi: Giá trị hóa đơn (' || TO_CHAR(p_ThanhTien, 'FM999,999,999')
               || ' VND) chưa đạt mức tối thiểu ('
               || TO_CHAR(v_GiaTriToiThieu, 'FM999,999,999') || ' VND)!';
    END IF;

    RETURN 'OK';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 'Lỗi: Mã phiếu giảm giá [' || p_MaPGG || '] không tồn tại!';
    WHEN OTHERS THEN
        RETURN 'Lỗi hệ thống: ' || SQLERRM;
END FN_KiemTraPhieuGiamGiaHopLe;
/
