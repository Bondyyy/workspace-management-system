CREATE OR REPLACE TRIGGER TRG_KiemTraPhuongThucThanhToan
BEFORE INSERT OR UPDATE OF PhuongThucThanhToan ON HOADON
FOR EACH ROW
WHEN (NEW.PhuongThucThanhToan IS NOT NULL)
BEGIN
    -- Kiểm tra phương thức thanh toán hợp lệ
    IF :NEW.PhuongThucThanhToan NOT IN ('Tiền mặt', 'Chuyển khoản') THEN
        RAISE_APPLICATION_ERROR(
            -20040,
            'Lỗi: Phương thức thanh toán không hợp lệ! '
            || 'Chỉ chấp nhận: "Tiền mặt" hoặc "Chuyển khoản". '
            || 'Giá trị nhận được: "' || :NEW.PhuongThucThanhToan || '"'
        );
    END IF;
END TRG_KiemTraPhuongThucThanhToan;
/
