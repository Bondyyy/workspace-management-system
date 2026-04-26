CREATE OR REPLACE TRIGGER TRG_KiemTraChiNhanhTruocVoHieuHoa
BEFORE UPDATE OF TrangThai ON CHINHANH
FOR EACH ROW
WHEN (NEW.TrangThai = 'Ngừng hoạt động' AND OLD.TrangThai = 'Đang hoạt động')
DECLARE
    v_SoPhienMo NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_SoPhienMo
    FROM PHIENLAMVIEC PLV
    JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
    WHERE KG.MaCN = :OLD.MaCN
      AND PLV.TrangThaiPhien = 'Đang hoạt động';

    IF v_SoPhienMo > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20010,
            'Lỗi: Chi nhánh [' || :OLD.MaCN || '] đang có '
            || v_SoPhienMo || ' phiên làm việc chưa kết thúc. Không thể vô hiệu hóa!'
        );
    END IF;
END TRG_KiemTraChiNhanhTruocVoHieuHoa;
/