CREATE OR REPLACE PROCEDURE sp_MoPhienLamViecTrucTiep (
    p_MaKG           IN  VARCHAR2,
    p_MaKH           IN  VARCHAR2,
    p_ThoiGianDuKien IN  TIMESTAMP,
    p_MaPhien        IN  VARCHAR2,
    p_MaDatCho       IN  VARCHAR2,
    p_outMessage     OUT VARCHAR2
)
AS
    v_TrangThaiKG KHONGGIAN.TrangThaiKG%TYPE;
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
BEGIN
    IF p_MaKG IS NULL OR LENGTH(TRIM(p_MaKG)) = 0 THEN
        p_outMessage := 'Lỗi: Thiếu mã không gian.';
        RETURN;
    END IF;

    IF p_MaKH IS NULL OR LENGTH(TRIM(p_MaKH)) = 0 THEN
        p_outMessage := 'Lỗi: Thiếu mã khách hàng.';
        RETURN;
    END IF;

    IF p_MaPhien IS NULL OR LENGTH(TRIM(p_MaPhien)) = 0 THEN
        p_outMessage := 'Lỗi: Thiếu mã phiên làm việc.';
        RETURN;
    END IF;

    SELECT TrangThaiKG
    INTO v_TrangThaiKG
    FROM KHONGGIAN
    WHERE MaKG = TRIM(p_MaKG)
    FOR UPDATE NOWAIT;

    IF v_TrangThaiKG <> 'Trống' THEN
        p_outMessage := 'Không gian hiện không khả dụng. Trạng thái hiện tại: ' || v_TrangThaiKG;
        RETURN;
    END IF;

    INSERT INTO PHIENLAMVIEC (
        MaPhien,
        ThoiGianBatDau,
        ThoiGianDuKienKetThuc,
        TrangThaiPhien,
        MaKG,
        MaKH,
        MaDatCho,
        CapNhatLanCuoi
    ) VALUES (
        TRIM(p_MaPhien),
        SYSTIMESTAMP,
        p_ThoiGianDuKien,
        'Đang hoạt động',
        TRIM(p_MaKG),
        TRIM(p_MaKH),
        NULLIF(TRIM(p_MaDatCho), ''),
        SYSTIMESTAMP
    );

    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Đang hoạt động'
    WHERE MaKG = TRIM(p_MaKG);

    IF p_MaDatCho IS NOT NULL AND LENGTH(TRIM(p_MaDatCho)) > 0 THEN
        UPDATE DATCHO
        SET TrangThaiDatTruoc = 'Đã sử dụng',
            MaQR = NULL,
            CapNhatLanCuoi = SYSTIMESTAMP
        WHERE MaDatCho = TRIM(p_MaDatCho);
    END IF;

    COMMIT;
    p_outMessage := 'Mở phiên làm việc trực tiếp thành công!';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_outMessage := 'Lỗi: Không tìm thấy không gian ' || p_MaKG || '.';
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
        p_outMessage := 'Lỗi: Mã phiên đã tồn tại, vui lòng tạo lại mã phiên.';
    WHEN ex_resource_busy THEN
        ROLLBACK;
        p_outMessage := 'Không gian đang được nhân viên khác thao tác. Vui lòng thử lại sau.';
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi hệ thống trong quá trình mở phiên: ' || SQLERRM;
END sp_MoPhienLamViecTrucTiep;
/
