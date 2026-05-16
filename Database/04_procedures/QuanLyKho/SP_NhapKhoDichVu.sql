CREATE OR REPLACE PROCEDURE SP_NhapKhoDichVu (
    p_MaDV IN VARCHAR2,
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
    v_MaNV VARCHAR2(50);
    v_MaCN VARCHAR2(50);
BEGIN
    -- 1. Lấy thông tin nhân viên nhập kho
    BEGIN
        SELECT nv.MaNV, nv.MaCN INTO v_MaNV, v_MaCN 
        FROM NHANVIEN nv 
        JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND 
        WHERE LOWER(TRIM(nd.HoTen)) = LOWER(TRIM(p_TenNhanVien)) AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_MaNV := NULL;
            v_MaCN := NULL;
    END;

    -- 2. Logic: Tiện ích thì không quản lý số lượng tồn kho
    IF LOWER(TRIM(p_TenLoaiDV)) LIKE '%tiện ích%' THEN
        v_SoLuongLuu := NULL;
    ELSE
        v_SoLuongLuu := p_SoLuong;
    END IF;

    -- 3. Xác định MaDV cần cập nhật (Ưu tiên theo p_MaDV truyền từ UI)
    -- Lưu ý: Trong Oracle, '' = NULL nên phải dùng LENGTH() > 0 thay vì != ''
    IF p_MaDV IS NOT NULL AND LENGTH(TRIM(p_MaDV)) > 0 THEN
        v_MaDV := TRIM(p_MaDV);
    ELSE
        BEGIN
            SELECT MaDV INTO v_MaDV FROM DICHVU WHERE LOWER(TRIM(TenDV)) = LOWER(TRIM(p_TenDV)) AND ROWNUM = 1;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_MaDV := NULL; -- Sẽ tạo mới ở bước sau
        END;
    END IF;

    -- 4. Tìm hoặc thêm mới LOẠI DỊCH VỤ
    BEGIN
        SELECT MaLoaiDV INTO v_MaLoaiDV FROM LOAIDICHVU WHERE LOWER(TRIM(TenLoaiDV)) = LOWER(TRIM(p_TenLoaiDV)) AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_MaLoaiDV := 'LDV' || SEQ_LOAIDV_NEW.NEXTVAL;
            INSERT INTO LOAIDICHVU (MaLoaiDV, TenLoaiDV, TrangThaiLDV) VALUES (v_MaLoaiDV, TRIM(p_TenLoaiDV), 'Đang hoạt động');
    END;

    -- 5. Cập nhật hoặc thêm mới DỊCH VỤ
    IF v_MaDV IS NOT NULL THEN
        -- Trường hợp đã có Dịch vụ: Cập nhật cộng dồn số lượng và giá nhập mới
        UPDATE DICHVU 
        SET SoLuong = NVL(SoLuong, 0) + NVL(v_SoLuongLuu, 0), 
            MaLoaiDV = v_MaLoaiDV,
            GiaNhap = p_GiaNhap
        WHERE MaDV = v_MaDV;
    ELSE
        -- Trường hợp chưa có: Thêm mới hoàn toàn
        v_MaDV := 'DV' || SEQ_DICHVU_NEW.NEXTVAL;
        INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong, GiaNhap)
        VALUES (v_MaDV, TRIM(p_TenDV), NULL, 'Đang hoạt động', 0, v_MaLoaiDV, v_SoLuongLuu, p_GiaNhap);
    END IF;

    -- 6. Xử lý CHỨNG TỪ NHẬP KHO (Nếu có đính kèm file)
    IF p_TenFile IS NOT NULL AND LENGTH(TRIM(p_TenFile)) > 0 THEN
        v_MaChungTu := 'CT' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '_' || SEQ_CHUNGTU.NEXTVAL;
        INSERT INTO CHUNGTUNHAPKHO (MaChungTu, MaDV, MaNV, MaCN, TenFile, NoiDungFile, NgayNhap, SoLuongNhap)
        VALUES (v_MaChungTu, v_MaDV, v_MaNV, v_MaCN, TRIM(p_TenFile), p_NoiDungFile, SYSDATE, v_SoLuongLuu);
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/