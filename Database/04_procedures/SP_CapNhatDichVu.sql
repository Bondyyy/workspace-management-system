CREATE OR REPLACE PROCEDURE sp_CapNhatDichVu (
    p_MaDV        IN VARCHAR2,
    p_TenDV       IN VARCHAR2,
    p_DonGia      IN NUMBER,
    p_TrangThaiDV IN VARCHAR2,
    p_outMessage  OUT VARCHAR2
)
AS
    v_countDV NUMBER;
BEGIN
    -- 1. Kiểm tra điều kiện đơn giá
    IF p_DonGia <= 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Đơn giá dịch vụ cập nhật phải lớn hơn 0!');
    END IF;

    -- 2. Kiểm tra sự tồn tại của dịch vụ trong hệ thống
    SELECT COUNT(*) INTO v_countDV
    FROM DICHVU
    WHERE MaDV = p_MaDV;

    IF v_countDV = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Không tìm thấy dịch vụ tương ứng với mã cung cấp!');
    END IF;

    -- 3. Cập nhật thông tin vào bảng DICHVU
    UPDATE DICHVU
    SET TenDV = p_TenDV,
        DonGia = p_DonGia,
        TrangThaiDV = p_TrangThaiDV
    WHERE MaDV = p_MaDV;

    COMMIT;
    p_outMessage := 'Đã cập nhật thông tin dịch vụ thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi trong quá trình cập nhật dịch vụ: ' || SQLERRM;
        RAISE;
END sp_CapNhatDichVu;
/