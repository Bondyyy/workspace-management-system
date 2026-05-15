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
    -- t0: Khởi tạo và định danh Transaction
    SET TRANSACTION NAME 'mo_phien_truc_tiep';

    -- t1: Đọc và giữ khóa dòng
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

    -- Sinh mã phiên mới
    v_NewMaPhien := 'PLV_' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDD_HH24MISS') || '_' || DBMS_RANDOM.STRING('U', 4);

    -- t2: Thêm phiên làm việc vào hệ thống
    INSERT INTO PHIENLAMVIEC (
        MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien, MaKG, MaKH, MaDatCho
    ) VALUES (
        v_NewMaPhien, SYSTIMESTAMP, p_ThoiGianDuKien, 'Đang hoạt động', p_MaKG, p_MaKH, NULL
    );

    -- t3: Cập nhật trạng thái không gian
    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Đang hoạt động'
    WHERE MaKG = p_MaKG;

    -- Nếu mọi lệnh đều suôn sẻ, hệ thống chốt giao dịch, nhả Row Lock
    DBMS_SESSION.SLEEP(10);
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
