-- =====================================================================
-- Trigger: TRG_KiemTraPhienTruocThanhToan
-- Mục đích: Đảm bảo chỉ thanh toán được khi phiên đã kết thúc
-- Điều kiện: TrangThaiPhien = 'Đã kết thúc'
-- Thời điểm: BEFORE UPDATE OF TrangThaiThanhToan, PhuongThucThanhToan
-- =====================================================================

CREATE OR REPLACE TRIGGER TRG_KiemTraPhienTruocThanhToan
BEFORE UPDATE OF TrangThaiThanhToan, PhuongThucThanhToan ON HOADON
FOR EACH ROW
DECLARE
    v_TrangThaiPhien VARCHAR2(50);
    v_MaPhien VARCHAR2(50);
BEGIN
    -- Chỉ kiểm tra khi đang cập nhật sang trạng thái thanh toán thành công
    -- hoặc khi chọn phương thức thanh toán
    IF (:NEW.TrangThaiThanhToan = 'Đã thanh toán thành công' AND 
        NVL(:OLD.TrangThaiThanhToan, '') != 'Đã thanh toán thành công')
       OR
       (:NEW.PhuongThucThanhToan IS NOT NULL AND 
        :OLD.PhuongThucThanhToan IS NULL) THEN
        
        -- Lấy trạng thái phiên làm việc
        BEGIN
            SELECT TrangThaiPhien, MaPhien
            INTO v_TrangThaiPhien, v_MaPhien
            FROM PHIENLAMVIEC
            WHERE MaPhien = :NEW.MaPhien;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(
                    -20030,
                    'Lỗi: Không tìm thấy phiên làm việc [' || :NEW.MaPhien || ']!'
                );
        END;
        
        -- Kiểm tra phiên đã kết thúc
        IF v_TrangThaiPhien != 'Đã kết thúc' THEN
            RAISE_APPLICATION_ERROR(
                -20031,
                'Lỗi: Không thể thanh toán! Phiên làm việc [' || v_MaPhien || '] '
                || 'chưa kết thúc (Trạng thái hiện tại: ' || v_TrangThaiPhien || '). '
                || 'Vui lòng kết thúc phiên trước khi thanh toán!'
            );
        END IF;
    END IF;
END TRG_KiemTraPhienTruocThanhToan;
/
