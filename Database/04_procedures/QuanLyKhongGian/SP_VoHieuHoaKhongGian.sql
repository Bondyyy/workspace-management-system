CREATE OR REPLACE PROCEDURE SP_VoHieuHoaKhongGian(
    p_MaKG IN VARCHAR2,
    p_outMessage OUT VARCHAR2
) AS
    v_TrangThaiKG VARCHAR2(50);
    v_SoPhienMo NUMBER;
BEGIN
    BEGIN
        SELECT TrangThaiKG INTO v_TrangThaiKG
        FROM KHONGGIAN WHERE MaKG = p_MaKG;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20120, 'Không tìm thấy không gian [' || p_MaKG || ']!');
    END;

    IF v_TrangThaiKG = 'Bảo trì' THEN
        RAISE_APPLICATION_ERROR(-20121, 'Không gian đã ở trạng thái Bảo trì!');
    END IF;

    IF v_TrangThaiKG = 'Đã đặt trước' THEN
        RAISE_APPLICATION_ERROR(-20122,
            'Không thể vô hiệu hóa: Không gian [' || p_MaKG || '] đang có đặt trước!');
    END IF;

    SELECT COUNT(*) INTO v_SoPhienMo
    FROM PHIENLAMVIEC
    WHERE MaKG = p_MaKG AND TrangThaiPhien = 'Đang hoạt động';

    IF v_SoPhienMo > 0 THEN
        RAISE_APPLICATION_ERROR(-20123,
            'Không thể vô hiệu hóa: Không gian [' || p_MaKG
            || '] đang có phiên làm việc chưa kết thúc!');
    END IF;

    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Bảo trì'
    WHERE MaKG = p_MaKG;

    COMMIT;
    p_outMessage := 'Vô hiệu hóa không gian [' || p_MaKG || '] thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi vô hiệu hóa không gian: ' || SQLERRM;
        RAISE;
END SP_VoHieuHoaKhongGian;
/
