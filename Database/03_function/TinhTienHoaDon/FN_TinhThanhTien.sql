CREATE OR REPLACE FUNCTION FN_TinhThanhTien(
    p_MaPhien IN VARCHAR2,
    p_MaPGG IN VARCHAR2 DEFAULT NULL
) RETURN NUMBER
AS
    v_TongTien NUMBER(18, 2);
    v_GiaTriGiamGia NUMBER(18, 2) := 0;
    v_PhanTramGiam NUMBER(5, 2) := 0;
    v_ThanhTien NUMBER(18, 2);
    v_MaKH VARCHAR2(50);
BEGIN
    -- 1. Tính tổng tiền
    v_TongTien := FN_TinhTongTien(p_MaPhien);
    
    -- 2. Lấy giá trị giảm giá từ phiếu (nếu có)
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
    
    -- 3. Lấy phần trăm giảm giá từ hạng thành viên (nếu có)
    BEGIN
        SELECT PLV.MaKH INTO v_MaKH
        FROM PHIENLAMVIEC PLV
        WHERE PLV.MaPhien = p_MaPhien;
        
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
    
    -- 4. Tính thành tiền
    v_ThanhTien := (v_TongTien - v_GiaTriGiamGia) * (1 - v_PhanTramGiam / 100);
    
    -- 5. Đảm bảo không âm
    IF v_ThanhTien < 0 THEN
        v_ThanhTien := 0;
    END IF;
    
    RETURN ROUND(v_ThanhTien, 2);
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_TinhThanhTien;
/
