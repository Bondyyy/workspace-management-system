CREATE OR REPLACE PROCEDURE SP_VoHieuHoaChiNhanh(
    p_MaCN IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_TrangThai VARCHAR2(50);
    v_SoPhienMo NUMBER;
BEGIN
    BEGIN
        SELECT TrangThai INTO v_TrangThai
        FROM CHINHANH WHERE MaCN = p_MaCN;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20050, 'Không tìm thấy chi nhánh [' || p_MaCN || ']!');
    END;

    IF v_TrangThai != 'Đang hoạt động' THEN
        RAISE_APPLICATION_ERROR(-20051, 'Chi nhánh đã ở trạng thái không hoạt động!');
    END IF;

    SELECT COUNT(*)
    INTO v_SoPhienMo
    FROM PHIENLAMVIEC PLV
    JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
    WHERE KG.MaCN = p_MaCN
      AND PLV.TrangThaiPhien = 'Đang hoạt động';

    IF v_SoPhienMo > 0 THEN
        RAISE_APPLICATION_ERROR(-20052,
            'Không thể vô hiệu hóa: Chi nhánh đang có '
            || v_SoPhienMo || ' phiên làm việc chưa kết thúc!');
    END IF;

    UPDATE CHINHANH
    SET TrangThai = 'Ngừng hoạt động'
    WHERE MaCN = p_MaCN;

    -- Vô hiệu hóa toàn bộ không gian thuộc chi nhánh này
    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Bảo trì'
    WHERE MaCN = p_MaCN;

    COMMIT;
    p_outMessage := 'Vô hiệu hóa chi nhánh [' || p_MaCN
                    || '] thành công! Toàn bộ không gian thuộc chi nhánh đã được vô hiệu hóa.';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi vô hiệu hóa chi nhánh: ' || SQLERRM;
        RAISE;
END SP_VoHieuHoaChiNhanh;
/
