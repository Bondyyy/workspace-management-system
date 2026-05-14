CREATE OR REPLACE PROCEDURE sp_MoPhienLamViecTrucTiep (
    p_MaKG           IN  VARCHAR2,
    p_MaKH           IN  VARCHAR2,
    p_ThoiGianDuKien IN  TIMESTAMP,
    p_MaPhien        OUT VARCHAR2,
    p_outMessage     OUT VARCHAR2
)
AS
    v_TrangThaiKG VARCHAR2(50);
    v_NewMaPhien  VARCHAR2(50);
BEGIN
    BEGIN
        SELECT TrangThaiKG INTO v_TrangThaiKG
        FROM KHONGGIAN
        WHERE MaKG = p_MaKG
        FOR UPDATE NOWAIT;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_outMessage := 'Lỗi: Không tìm thấy mã không gian ' || p_MaKG;
            RETURN;
        WHEN OTHERS THEN
            p_outMessage := 'Lỗi: Không gian này đang được nhân viên khác thao tác. Vui lòng thử lại sau.';
            RETURN;
    END;

    IF v_TrangThaiKG != 'Trống' THEN
        p_outMessage := 'Vị trí không khả dụng để phục vụ';
        RETURN;
    END IF;

    -- Sinh mã phiên dạng PH + số thứ tự (tương tự logic Java cũ)
    SELECT 'PH' || LPAD(COUNT(*) + 1, 4, '0') INTO v_NewMaPhien
    FROM PHIENLAMVIEC;

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
        v_NewMaPhien,
        SYSTIMESTAMP,
        p_ThoiGianDuKien,
        'Đang hoạt động',
        p_MaKG,
        p_MaKH,
        NULL,
        SYSTIMESTAMP
    );

    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Đang hoạt động'
    WHERE MaKG = p_MaKG;

    COMMIT;

    p_MaPhien := v_NewMaPhien;
    p_outMessage := 'Mở phiên làm việc trực tiếp thành công!';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi hệ thống trong quá trình mở phiên: ' || SQLERRM;
        p_MaPhien := NULL;
END sp_MoPhienLamViecTrucTiep;
/