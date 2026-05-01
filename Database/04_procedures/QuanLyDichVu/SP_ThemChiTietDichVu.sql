CREATE OR REPLACE PROCEDURE sp_ThemChiTietDichVu (
    p_MaDV       IN VARCHAR2,
    p_MaPhien    IN VARCHAR2,
    p_SoLuong    IN NUMBER,
    p_GhiChu     IN VARCHAR2,
    p_outMessage OUT VARCHAR2
)
AS
    v_countPhien NUMBER;
    v_countDV    NUMBER;
    v_countCTDV  NUMBER;
BEGIN
    -- 1. Kiểm tra điều kiện số lượng
    IF p_SoLuong <= 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Số lượng dịch vụ gọi thêm phải lớn hơn 0!');
    END IF;

    -- 2. Kiểm tra sự tồn tại của phiên làm việc
    SELECT COUNT(*) INTO v_countPhien
    FROM PHIENLAMVIEC
    WHERE MaPhien = p_MaPhien;

    IF v_countPhien = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Không tìm thấy phiên làm việc tương ứng trong hệ thống!');
    END IF;

    -- 3. Kiểm tra sự tồn tại của dịch vụ
    SELECT COUNT(*) INTO v_countDV
    FROM DICHVU
    WHERE MaDV = p_MaDV;

    IF v_countDV = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Không tìm thấy dịch vụ tương ứng với mã cung cấp!');
    END IF;

    -- 4. Kiểm tra dịch vụ đã từng được gọi trong phiên này chưa
    SELECT COUNT(*) INTO v_countCTDV
    FROM CHITIETDICHVU
    WHERE MaPhien = p_MaPhien AND MaDV = p_MaDV;

    -- 5. Thêm mới hoặc cập nhật cộng dồn
    IF v_countCTDV > 0 THEN
        -- Nếu đã có, tiến hành cộng dồn số lượng và cập nhật ghi chú
        UPDATE CHITIETDICHVU
        SET SoLuong = SoLuong + p_SoLuong,
            GhiChu = CASE
                        WHEN p_GhiChu IS NOT NULL THEN p_GhiChu
                        ELSE GhiChu
                     END
        WHERE MaPhien = p_MaPhien AND MaDV = p_MaDV;
    ELSE
        -- Nếu chưa có, tạo bản ghi chi tiết dịch vụ mới
        INSERT INTO CHITIETDICHVU (MaDV, MaPhien, SoLuong, GhiChu)
        VALUES (p_MaDV, p_MaPhien, p_SoLuong, p_GhiChu);
    END IF;

    COMMIT;
    p_outMessage := 'Đã thêm dịch vụ vào phiên làm việc thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi trong quá trình thêm chi tiết dịch vụ: ' || SQLERRM;
        RAISE;
END sp_ThemChiTietDichVu;
/