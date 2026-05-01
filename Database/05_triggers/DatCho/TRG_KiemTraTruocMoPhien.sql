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

    -- 2. Ném lỗi nếu không gian không sẵn sàng
    IF v_TrangThaiKG != 'Trống' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Lỗi: Không gian này hiện không trống (Trạng thái: ' || v_TrangThaiKG || '). Không thể tạo phiên làm việc!');
    END IF;
END;
/
