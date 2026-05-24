CREATE OR REPLACE TRIGGER TRG_KiemTraPhuongThucThanhToan
BEFORE INSERT OR UPDATE OF PhuongThucThanhToan ON HOADON
FOR EACH ROW
WHEN (NEW.PhuongThucThanhToan IS NOT NULL)
BEGIN
    -- Kiểm tra phương thức thanh toán hợp lệ (bao gồm đặt trước đã trả tiền)
    IF :NEW.PhuongThucThanhToan NOT IN ('Tiền mặt', 'Chuyển khoản', 'Đặt trước') THEN
        RAISE_APPLICATION_ERROR(
            -20040,
            'Lỗi: Phương thức thanh toán không hợp lệ! '
            || 'Chỉ chấp nhận: "Tiền mặt", "Chuyển khoản" hoặc "Đặt trước". '
            || 'Giá trị nhận được: "' || :NEW.PhuongThucThanhToan || '"'
        );
    END IF;
END TRG_KiemTraPhuongThucThanhToan;
/
