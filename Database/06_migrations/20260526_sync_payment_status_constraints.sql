DECLARE
    PROCEDURE drop_constraint_if_exists(p_Name IN VARCHAR2) IS
        v_Count NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO v_Count
        FROM USER_CONSTRAINTS
        WHERE CONSTRAINT_NAME = UPPER(p_Name);

        IF v_Count > 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || CASE
                WHEN UPPER(p_Name) LIKE 'CHK_DC_%' THEN 'DATCHO'
                WHEN UPPER(p_Name) LIKE 'CHK_HD_%' THEN 'HOADON'
                WHEN UPPER(p_Name) LIKE 'CHK_KG_%' THEN 'KHONGGIAN'
                WHEN UPPER(p_Name) LIKE 'CHK_PLV_%' THEN 'PHIENLAMVIEC'
            END || ' DROP CONSTRAINT ' || p_Name;
        END IF;
    END;
BEGIN
    drop_constraint_if_exists('CHK_DC_TrangThai');
    drop_constraint_if_exists('CHK_HD_PTTT');
    drop_constraint_if_exists('CHK_HD_TrangThai');
    drop_constraint_if_exists('CHK_HD_TienHopLe');

    EXECUTE IMMEDIATE q'[
        ALTER TABLE DATCHO ADD CONSTRAINT CHK_DC_TrangThai
        CHECK (TrangThaiDatTruoc IN (
            'Đang chờ thanh toán',
            'Đã thanh toán thành công',
            'Thanh toán không thành công',
            'Đã sử dụng',
            'Quá hạn nhận chỗ'
        ))
    ]';

    EXECUTE IMMEDIATE q'[
        ALTER TABLE HOADON ADD CONSTRAINT CHK_HD_PTTT
        CHECK (PhuongThucThanhToan IN ('Chuyển khoản', 'Tiền mặt', 'Đặt trước'))
    ]';

    EXECUTE IMMEDIATE q'[
        ALTER TABLE HOADON ADD CONSTRAINT CHK_HD_TrangThai
        CHECK (TrangThaiThanhToan IN (
            'Đang chờ thanh toán',
            'Đã trả trước',
            'Đã thanh toán thành công',
            'Thanh toán không thành công'
        ))
    ]';

    EXECUTE IMMEDIATE q'[
        ALTER TABLE HOADON ADD CONSTRAINT CHK_HD_TienHopLe
        CHECK (
            TongTien >= 0
            AND ThanhTien >= 0
            AND DaTraTruoc >= 0
            AND ThanhTien <= TongTien
        )
    ]';
END;
/
