CREATE OR REPLACE PROCEDURE sp_ThemDichVu (
    p_MaDV        IN VARCHAR2,
    p_TenDV       IN VARCHAR2,
    p_MaLoaiDV    IN VARCHAR2,
    p_DonGia      IN NUMBER,
    p_TrangThaiDV IN VARCHAR2,
    p_HinhAnh     IN BLOB,
    p_SoLuong     IN NUMBER,
    p_GiaNhap     IN NUMBER,
    p_outMessage  OUT VARCHAR2
)
AS
    v_countLoaiDV NUMBER;
    v_countDV     NUMBER;
BEGIN
    -- 1. Kểm tra điều kiện đơn giá
    IF p_DonGia <= 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Đơn giá dịch vụ phải lớn hơn 0!');
    END IF;

    -- 2. Kiểm tra tính hợp lệ của Mã loại dịch vụ
    SELECT COUNT(*) INTO v_countLoaiDV
    FROM LOAIDICHVU
    WHERE MaLoaiDV = p_MaLoaiDV;

    IF v_countLoaiDV = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Loại dịch vụ không tồn tại trong hệ thống!');
    END IF;

    -- 3. Kiểm tra trùng lặp Mã Dịch Vụ
    SELECT COUNT(*) INTO v_countDV
    FROM DICHVU
    WHERE MaDV = p_MaDV;

    IF v_countDV > 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Mã dịch vụ này đã tồn tại, vui lòng sử dụng mã khác!');
    END IF;

    -- 4. Thêm dịch vụ vào bảng DICHVU
    INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, HinhAnh, SoLuong, GiaNhap)
    VALUES (p_MaDV, p_TenDV, p_TrangThaiDV, p_DonGia, p_MaLoaiDV, p_HinhAnh, p_SoLuong, p_GiaNhap);

    COMMIT;
    p_outMessage := 'Đã thêm dịch vụ mới thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi quá trình thêm dịch vụ: ' || SQLERRM;
        RAISE;
END sp_ThemDichVu;
/