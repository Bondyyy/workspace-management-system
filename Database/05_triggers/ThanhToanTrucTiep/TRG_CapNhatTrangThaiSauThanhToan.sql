CREATE OR REPLACE TRIGGER TRG_CapNhatTrangThaiSauThanhToan
BEFORE UPDATE OF PhuongThucThanhToan ON HOADON
FOR EACH ROW
WHEN (NEW.PhuongThucThanhToan IS NOT NULL AND OLD.PhuongThucThanhToan IS NULL)
DECLARE
    v_MaKH VARCHAR2(50);
    v_LoaiKH VARCHAR2(50);
    v_MaND VARCHAR2(50);
BEGIN
    -- 1. Tự động cập nhật trạng thái thanh toán thành công
    -- khi lễ tân xác nhận phương thức (Tiền mặt hoặc Chuyển khoản)
    :NEW.TrangThaiThanhToan := 'Đã thanh toán thành công';
    
    -- Cập nhật ngày lập hóa đơn nếu chưa có
    IF :NEW.NgayLapHoaDon IS NULL THEN
        :NEW.NgayLapHoaDon := SYSTIMESTAMP;
    END IF;

    -- 2. Logic dọn dẹp khách vãng lai (Theo yêu cầu người dùng)
    -- Lấy thông tin khách hàng từ phiên làm việc
    BEGIN
        SELECT MaKH INTO v_MaKH FROM PHIENLAMVIEC WHERE MaPhien = :NEW.MaPhien;
        
        IF v_MaKH IS NOT NULL THEN
            -- Kiểm tra loại khách hàng
            SELECT LoaiKH, MaND INTO v_LoaiKH, v_MaND FROM KHACHHANG WHERE MaKH = v_MaKH;
            
            IF v_LoaiKH = 'Khách vãng lai' THEN
                -- Gỡ liên kết MaKH khỏi phiên làm việc để có thể xóa bản ghi khách hàng
                UPDATE PHIENLAMVIEC SET MaKH = NULL WHERE MaPhien = :NEW.MaPhien;
                
                -- Xóa thông tin khách hàng và tài khoản người dùng tương ứng
                DELETE FROM CHITIETVAITRO WHERE MaND = v_MaND;
                DELETE FROM KHACHHANG WHERE MaKH = v_MaKH;
                DELETE FROM NGUOIDUNG WHERE MaND = v_MaND;
            END IF;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN NULL;
        WHEN OTHERS THEN NULL; -- Tránh làm gián đoạn luồng thanh toán nếu lỗi dọn dẹp
    END;
END;
/
