CREATE OR REPLACE TRIGGER trg_PhatSinhMaQR
BEFORE UPDATE OF TrangThaiThanhToan ON DATCHO
FOR EACH ROW
WHEN (NEW.TrangThaiThanhToan IN ('Đã thanh toán thành công') AND OLD.TrangThaiThanhToan = 'Đang chờ thanh toán')
DECLARE
    v_qr_moi VARCHAR2(255);
BEGIN
    -- Gọi Procedure hệ thống để phát sinh chuỗi mã QR duy nhất
    pro_SinhMaQR(:NEW.MaDatCho, v_q_moi);

    -- Gán chuỗi QR vừa tạo vào cột qr_code của bản ghi đang được cập nhật
    :NEW.MaQR := v_qr_moi;
    -- Ghi log lưu vết hệ thống
    :NEW.GhiChu := :NEW.GhiChu || ' | Cấp QR thành công qua Procedure.';
    -- Cập nhật thời gian chỉnh sửa cuối cùng
    :NEW.CapNhatLanCuoi := CURRENT_TIMESTAMP;
END;
/
