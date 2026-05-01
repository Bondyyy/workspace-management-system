CREATE OR REPLACE PROCEDURE SP_NhapKhoDichVu (
    p_TenNhanVien IN VARCHAR2,
    p_TenLoaiDV IN VARCHAR2,
    p_TenDV IN VARCHAR2,
    p_SoLuong IN NUMBER,
    p_TenFile IN VARCHAR2
) AS
    v_MaLoaiDV VARCHAR2(50);
    v_MaDV VARCHAR2(50);
    v_MaChungTu VARCHAR2(50);
    v_SoLuongLuu NUMBER;
BEGIN
    -- Logic: Tiện ích thì không quản lý số lượng
    IF LOWER(p_TenLoaiDV) = 'tiện ích' THEN
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
        -- Nếu đã có thì cộng dồn số lượng
        IF v_SoLuongLuu IS NOT NULL THEN
            UPDATE DICHVU SET SoLuong = NVL(SoLuong, 0) + v_SoLuongLuu, MaLoaiDV = v_MaLoaiDV WHERE MaDV = v_MaDV;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_MaDV := 'DV' || SEQ_DICHVU_NEW.NEXTVAL;
            -- Tạm set Đơn giá = 0, Hình ảnh default để sau này Admin cập nhật thêm
            INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
            VALUES (v_MaDV, p_TenDV, 'default.png', 'Đang hoạt động', 0, v_MaLoaiDV, v_SoLuongLuu);
    END;

    -- C. Xử lý CHỨNG TỪ NHẬP KHO
    -- SỬA LỖI Ở ĐÂY: Trong Oracle, '' và NULL là một.
    -- Chỉ cần kiểm tra IS NOT NULL và LENGTH() > 0 là đủ an toàn.
    IF p_TenFile IS NOT NULL AND LENGTH(TRIM(p_TenFile)) > 0 THEN
        v_MaChungTu := 'CT' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '_' || SEQ_CHUNGTU.NEXTVAL;
        INSERT INTO CHUNGTUNHAPKHO (MaChungTu, MaDV, TenFile, NgayNhap, NhanVienNhap, SoLuongNhap)
        VALUES (v_MaChungTu, v_MaDV, p_TenFile, SYSDATE, p_TenNhanVien, v_SoLuongLuu);
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/