CREATE OR REPLACE TRIGGER TRG_ChanThemDichVu_PhienDong
BEFORE INSERT ON CHITIETDICHVU
FOR EACH ROW
DECLARE
    v_TrangThaiPhien VARCHAR2(50);
BEGIN
    SELECT TrangThaiPhien INTO v_TrangThaiPhien
    FROM PHIENLAMVIEC
    WHERE MaPhien = :NEW.MaPhien;

    IF v_TrangThaiPhien != 'Đang hoạt động' THEN
        RAISE_APPLICATION_ERROR(-20007, 'Lỗi: Phiên làm việc này hiện không hoạt động (Trạng thái: ' || v_TrangThaiPhien || '). Không thể gọi thêm dịch vụ!');
    END IF;
END;
/