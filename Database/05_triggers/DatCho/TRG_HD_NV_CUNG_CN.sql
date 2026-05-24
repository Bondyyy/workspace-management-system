CREATE OR REPLACE TRIGGER TRG_HD_NV_CUNG_CN
BEFORE INSERT OR UPDATE OF MaPhien, MaNV ON HOADON
FOR EACH ROW
DECLARE
    v_MaCN_KhongGian KHONGGIAN.MaCN%TYPE;
    v_MaCN_NhanVien  NHANVIEN.MaCN%TYPE;
BEGIN
    IF :NEW.MaNV IS NULL OR :NEW.MaPhien IS NULL THEN
        RETURN;
    END IF;

    SELECT KG.MaCN
    INTO v_MaCN_KhongGian
    FROM PHIENLAMVIEC PLV
    JOIN KHONGGIAN KG ON KG.MaKG = PLV.MaKG
    WHERE PLV.MaPhien = :NEW.MaPhien;

    SELECT MaCN
    INTO v_MaCN_NhanVien
    FROM NHANVIEN
    WHERE MaNV = :NEW.MaNV;

    IF v_MaCN_KhongGian <> v_MaCN_NhanVien THEN
        RAISE_APPLICATION_ERROR(
            -20229,
            'Nhân viên thu ngân phải cùng chi nhánh với không gian khách đã sử dụng.'
        );
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20230,
            'Không tìm thấy phiên làm việc, không gian hoặc nhân viên tương ứng khi lập hóa đơn.'
        );
END;
/