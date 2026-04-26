CREATE OR REPLACE TRIGGER TRG_KiemTraKhongGianTruocVoHieuHoa
BEFORE UPDATE OF TrangThaiKG ON KHONGGIAN
FOR EACH ROW
WHEN (NEW.TrangThaiKG = 'Bảo trì')
DECLARE
    v_SoPhienMo NUMBER;
BEGIN
    IF :OLD.TrangThaiKG IN ('Đã đặt trước') THEN
        RAISE_APPLICATION_ERROR(
            -20011,
            'Lỗi: Không gian [' || :OLD.MaKG || '] đang ở trạng thái "'
            || :OLD.TrangThaiKG || '". Không thể vô hiệu hóa!'
        );
    END IF;

    SELECT COUNT(*)
    INTO v_SoPhienMo
    FROM PHIENLAMVIEC
    WHERE MaKG = :OLD.MaKG
      AND TrangThaiPhien = 'Đang hoạt động';

    IF v_SoPhienMo > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20012,
            'Lỗi: Không gian [' || :OLD.MaKG
            || '] đang có phiên làm việc chưa kết thúc. Không thể vô hiệu hóa!'
        );
    END IF;
END TRG_KiemTraKhongGianTruocVoHieuHoa;
/