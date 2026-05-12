CREATE OR REPLACE PROCEDURE SP_NhapKhoDichVu (
    p_TenNhanVien IN VARCHAR2,
    p_TenLoaiDV IN VARCHAR2,
    p_TenDV IN VARCHAR2,
    p_SoLuong IN NUMBER,
    p_TenFile IN VARCHAR2,
    p_GiaNhap IN NUMBER,
    p_NoiDungFile IN BLOB
) AS
    v_MaLoaiDV VARCHAR2(50);
    v_MaDV VARCHAR2(50);
    v_MaChungTu VARCHAR2(50);
    v_SoLuongLuu NUMBER;
BEGIN
    -- Logic: Tiện ích thì không quản lý số lượng
    IF LOWER(p_TenLoaiDV) LIKE '%tiện ích%' THEN
        v_SoLuongLuu := NULL;
    ELSE
        v_SoLuongLuu := p_SoLuong;
    END IF;

    -- A. Tìm hoặc thêm mới LOẠI DỊCH VỤ
    BEGIN
        SELECT MaLoaiDV INTO v_MaLoaiDV FROM LOAIDICHVU WHERE LOWER(TenLoaiDV) = LOWER(p_TenLoaiDV) AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_MaLoaiDV := 'LDV' || SEQ_LOAIDV_NEW.NEXTVAL;
            INSERT INTO LOAIDICHVU (MaLoaiDV, TenLoaiDV, TrangThaiLDV) VALUES (v_MaLoaiDV, p_TenLoaiDV, 'Đang hoạt động');
    END;

    -- B. Tìm hoặc thêm mới DỊCH VỤ
    BEGIN
        SELECT MaDV INTO v_MaDV FROM DICHVU WHERE LOWER(TenDV) = LOWER(p_TenDV) AND ROWNUM = 1;
        UPDATE DICHVU 
        SET SoLuong = NVL(SoLuong, 0) + NVL(v_SoLuongLuu, 0), 
            MaLoaiDV = v_MaLoaiDV,
            GiaNhap = p_GiaNhap
        WHERE MaDV = v_MaDV;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_MaDV := 'DV' || SEQ_DICHVU_NEW.NEXTVAL;
            INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong, GiaNhap)
            VALUES (v_MaDV, p_TenDV, NULL, 'Đang hoạt động', 0, v_MaLoaiDV, v_SoLuongLuu, p_GiaNhap);
    END;

    -- C. Xử lý CHỨNG TỪ NHẬP KHO
    IF p_TenFile IS NOT NULL AND LENGTH(TRIM(p_TenFile)) > 0 THEN
        v_MaChungTu := 'CT' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '_' || SEQ_CHUNGTU.NEXTVAL;
        INSERT INTO CHUNGTUNHAPKHO (MaChungTu, MaDV, TenFile, NoiDungFile, NgayNhap, NhanVienNhap, SoLuongNhap)
        VALUES (v_MaChungTu, v_MaDV, p_TenFile, p_NoiDungFile, SYSDATE, p_TenNhanVien, v_SoLuongLuu);
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/