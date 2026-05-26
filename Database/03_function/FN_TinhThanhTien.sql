CREATE OR REPLACE FUNCTION FN_TinhThanhTien(
    p_MaPhien IN VARCHAR2,
    p_MaPGG IN VARCHAR2 DEFAULT NULL
) RETURN NUMBER
AS
    v_TongTien NUMBER(18, 2);
    v_GiaTriGiamGia NUMBER(18, 2) := 0;
    v_TienGiamVoucher NUMBER(18, 2) := 0;
    v_PhanTramGiam NUMBER(5, 2) := 0;
    v_TienGiamHang NUMBER(18, 2) := 0;
    v_ThanhTien NUMBER(18, 2);
    v_MaKH VARCHAR2(50);
    v_MaDatCho VARCHAR2(50);
    v_CoSoTinhGiam NUMBER(18, 2);
BEGIN
    BEGIN
        SELECT PLV.MaKH, PLV.MaDatCho
        INTO v_MaKH, v_MaDatCho
        FROM PHIENLAMVIEC PLV
        WHERE PLV.MaPhien = p_MaPhien;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 0;
    END;

    -- Phiên đặt trước đã giảm/trả trước phần đặt chỗ, nên chỉ tính phần phát sinh tại quầy.
    IF v_MaDatCho IS NOT NULL THEN
        v_CoSoTinhGiam := FN_TinhTienDichVu(p_MaPhien);
    ELSE
        v_TongTien := FN_TinhTongTien(p_MaPhien);
        v_CoSoTinhGiam := v_TongTien;
    END IF;

    v_CoSoTinhGiam := GREATEST(0, NVL(v_CoSoTinhGiam, 0));
    
    -- 1. Lấy giá trị giảm giá tại quầy từ phiếu (nếu có), chỉ áp trên cơ sở còn phải thu.
    IF p_MaPGG IS NOT NULL THEN
        BEGIN
            SELECT GiaTriGiamGia
            INTO v_GiaTriGiamGia
            FROM PHIEUGIAMGIA
            WHERE MaPGG = p_MaPGG;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_GiaTriGiamGia := 0;
        END;
    END IF;
    v_TienGiamVoucher := LEAST(GREATEST(0, NVL(v_GiaTriGiamGia, 0)), v_CoSoTinhGiam);
    
    -- 2. Lấy phần trăm giảm giá từ hạng thành viên hiện tại cho phần tại quầy.
    BEGIN
        IF v_MaKH IS NOT NULL THEN
            SELECT NVL(HTV.PhanTramTienGiam, 0)
            INTO v_PhanTramGiam
            FROM KHACHHANG KH
            LEFT JOIN HANGTHANHVIEN HTV ON KH.MaHangThanhVien = HTV.MaHangThanhVien
            WHERE KH.MaKH = v_MaKH;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_PhanTramGiam := 0;
    END;
    
    v_PhanTramGiam := LEAST(100, GREATEST(0, NVL(v_PhanTramGiam, 0)));
    v_TienGiamHang := ROUND(GREATEST(0, v_CoSoTinhGiam - v_TienGiamVoucher) * v_PhanTramGiam / 100, 0);
    v_ThanhTien := GREATEST(0, v_CoSoTinhGiam - v_TienGiamVoucher - v_TienGiamHang);
    
    RETURN ROUND(v_ThanhTien, 0);
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_TinhThanhTien;
/
