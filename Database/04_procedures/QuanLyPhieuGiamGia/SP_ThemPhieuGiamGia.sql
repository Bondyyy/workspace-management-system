CREATE OR REPLACE PROCEDURE SP_ThemPhieuGiamGia(
    p_MaChuSoPGG IN VARCHAR2,
    p_GiaTriGiamGia IN NUMBER,
    p_GiaTriApDungToiThieu IN NUMBER,
    p_NgayBatDauApDung IN TIMESTAMP,
    p_NgayKetThucApDung IN TIMESTAMP,
    p_SLToiDa IN NUMBER,
    p_MaNV IN VARCHAR2,
    p_TrangThai IN VARCHAR2,
    p_outMessage OUT VARCHAR2
)
AS
    v_Count NUMBER;
    v_TrangThai PHIEUGIAMGIA.TrangThai%TYPE;
BEGIN
    -- Validate mã chữ số phiếu giảm giá
    IF p_MaChuSoPGG IS NULL OR TRIM(p_MaChuSoPGG) IS NULL THEN
        p_outMessage := 'Lỗi: Mã chữ số phiếu giảm giá không được để trống.';
        RETURN;
    END IF;

    -- Không cho trùng mã chữ số phiếu giảm giá
    SELECT COUNT(*)
    INTO v_Count
    FROM PHIEUGIAMGIA
    WHERE UPPER(TRIM(MaChuSoPGG)) = UPPER(TRIM(p_MaChuSoPGG));

    IF v_Count > 0 THEN
        p_outMessage := 'Lỗi: Mã chữ số phiếu giảm giá đã tồn tại.';
        RETURN;
    END IF;

    -- Validate giá trị giảm giá
    IF p_GiaTriGiamGia IS NULL OR p_GiaTriGiamGia <= 0 THEN
        p_outMessage := 'Lỗi: Giá trị giảm giá phải lớn hơn 0.';
        RETURN;
    END IF;

    -- Validate giá trị áp dụng tối thiểu
    IF p_GiaTriApDungToiThieu IS NULL OR p_GiaTriApDungToiThieu < 0 THEN
        p_outMessage := 'Lỗi: Giá trị áp dụng tối thiểu không hợp lệ.';
        RETURN;
    END IF;

    -- Validate thời gian
    IF p_NgayBatDauApDung IS NULL OR p_NgayKetThucApDung IS NULL THEN
        p_outMessage := 'Lỗi: Ngày bắt đầu và ngày kết thúc không được để trống.';
        RETURN;
    END IF;

    IF p_NgayKetThucApDung <= p_NgayBatDauApDung THEN
        p_outMessage := 'Lỗi: Ngày kết thúc phải sau ngày bắt đầu.';
        RETURN;
    END IF;

    -- Validate số lượng tối đa
    IF p_SLToiDa IS NULL OR p_SLToiDa <= 0 THEN
        p_outMessage := 'Lỗi: Số lượng tối đa phải lớn hơn 0.';
        RETURN;
    END IF;

    -- Validate nhân viên tạo nếu có truyền MaNV
    IF p_MaNV IS NOT NULL THEN
        SELECT COUNT(*)
        INTO v_Count
        FROM NHANVIEN
        WHERE MaNV = p_MaNV;

        IF v_Count = 0 THEN
            p_outMessage := 'Lỗi: Nhân viên tạo phiếu giảm giá không tồn tại.';
            RETURN;
        END IF;
    END IF;

    -- Trạng thái mặc định
    v_TrangThai := NVL(NULLIF(TRIM(p_TrangThai), ''), 'Đang có hiệu lực');

    INSERT INTO PHIEUGIAMGIA (
        MaChuSoPGG,
        GiaTriGiamGia,
        GiaTriApDungToiThieu,
        NgayBatDauApDung,
        NgayKetThucApDung,
        SLDaDung,
        SLToiDa,
        NgayTaoPGG,
        MaNV,
        TrangThai
    )
    VALUES (
        TRIM(p_MaChuSoPGG),
        p_GiaTriGiamGia,
        p_GiaTriApDungToiThieu,
        p_NgayBatDauApDung,
        p_NgayKetThucApDung,
        0,
        p_SLToiDa,
        CURRENT_TIMESTAMP,
        p_MaNV,
        v_TrangThai
    );

    COMMIT;
    p_outMessage := 'Thêm phiếu giảm giá thành công!';

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
        p_outMessage := 'Lỗi: Mã phiếu giảm giá hoặc mã chữ số phiếu giảm giá đã tồn tại.';
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Lỗi thêm phiếu giảm giá: ' || SQLERRM;
END SP_ThemPhieuGiamGia;
/

/*
Test thủ công:

DECLARE
    v_msg VARCHAR2(4000);
BEGIN
    SP_ThemPhieuGiamGia(
        p_MaChuSoPGG => 'PHANTOM_TEST_001',
        p_GiaTriGiamGia => 10000,
        p_GiaTriApDungToiThieu => 50000,
        p_NgayBatDauApDung => CURRENT_TIMESTAMP,
        p_NgayKetThucApDung => CURRENT_TIMESTAMP + INTERVAL '30' DAY,
        p_SLToiDa => 100,
        p_MaNV => 'NV0001',
        p_TrangThai => 'Đang có hiệu lực',
        p_outMessage => v_msg
    );
    DBMS_OUTPUT.PUT_LINE(v_msg);
END;
/

Nếu NV0001 không tồn tại thì dùng mã nhân viên thật trong database.
*/
