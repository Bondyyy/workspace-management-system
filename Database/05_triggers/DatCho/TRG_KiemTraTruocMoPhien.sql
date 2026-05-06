CREATE OR REPLACE TRIGGER TRG_KiemTraTruocMoPhien
BEFORE INSERT ON PHIENLAMVIEC
FOR EACH ROW
DECLARE
    v_TrangThaiKG VARCHAR2(50);
BEGIN
    -- 1. Lấy trạng thái hiện tại của không gian tương ứng
    SELECT TrangThaiKG INTO v_TrangThaiKG
    FROM KHONGGIAN
    WHERE MaKG = :NEW.MaKG;

    -- 2. Ném lỗi dựa trên loại phiên
    -- Nếu là mở phiên dùng ngay (Đang hoạt động) -> Phải yêu cầu phòng Trống
    IF :NEW.TrangThaiPhien = 'Đang hoạt động' AND v_TrangThaiKG != 'Trống' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Không gian này hiện đang có người sử dụng hoặc chưa sẵn sàng (Trạng thái: ' || v_TrangThaiKG || ').');
    END IF;

    -- Nếu là phòng đang bảo trì -> Chặn tất cả
    IF v_TrangThaiKG = 'Bảo trì' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Không gian này đang được bảo trì, không thể đặt chỗ!');
    END IF;

    -- Lưu ý: Nếu là 'Đã đặt trước', chúng ta cho phép tạo phiên ngay cả khi v_TrangThaiKG là 'Đang hoạt động' 
    -- vì khách đang đặt cho một khung giờ trong tương lai.
END;
/
