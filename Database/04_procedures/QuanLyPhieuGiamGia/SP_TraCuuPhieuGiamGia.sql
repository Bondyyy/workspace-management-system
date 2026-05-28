CREATE OR REPLACE PROCEDURE SP_TraCuuPhieuGiamGia(
    p_TuKhoa IN VARCHAR2,
    p_TrangThai IN VARCHAR2,
    p_outCursor OUT SYS_REFCURSOR,
    p_outMessage OUT VARCHAR2
) AS
    v_TuKhoa VARCHAR2(200);
BEGIN
    IF p_TuKhoa IS NOT NULL AND TRIM(p_TuKhoa) IS NOT NULL THEN
        v_TuKhoa := '%' || UPPER(TRIM(p_TuKhoa)) || '%';
    END IF;

    OPEN p_outCursor FOR
        SELECT MaPGG,
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
        FROM PHIEUGIAMGIA
        WHERE (v_TuKhoa IS NULL
               OR UPPER(MaPGG) LIKE v_TuKhoa
               OR UPPER(MaChuSoPGG) LIKE v_TuKhoa)
          AND (p_TrangThai IS NULL
               OR TRIM(p_TrangThai) IS NULL
               OR p_TrangThai = 'Tất cả'
               OR TrangThai = p_TrangThai)
        ORDER BY NgayTaoPGG DESC;

    p_outMessage := 'Tra cứu phiếu giảm giá thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi tra cứu phiếu giảm giá: ' || SQLERRM;
        RAISE;
END SP_TraCuuPhieuGiamGia;
/

DECLARE
    v_SoPhieu NUMBER;
    CURSOR c_pgg IS
        SELECT MaPGG, MaChuSoPGG, GiaTriGiamGia, SLToiDa, TrangThai
        FROM PHIEUGIAMGIA
        WHERE TrangThai = 'Đang có hiệu lực'
        ORDER BY NgayTaoPGG DESC;
BEGIN
    SELECT COUNT(*) INTO v_SoPhieu
    FROM PHIEUGIAMGIA
    WHERE TrangThai = 'Đang có hiệu lực';
    DBMS_OUTPUT.PUT_LINE('--- DANH SÁCH PHIẾU GIẢM GIÁ---');
    DBMS_OUTPUT.PUT_LINE('Số phiếu đang có hiệu lực lần 1: ' || v_SoPhieu);
    FOR r IN c_pgg LOOP
       DBMS_OUTPUT.PUT_LINE('- Mã chữ số PGG: ' || r.MaChuSoPGG ||
                             ' | Giá trị giảm: ' || r.GiaTriGiamGia ||
                             ' | Số lượng tối đa: ' || r.SLToiDa);
    END LOOP; -- Kết thúc vòng lặp

    SELECT COUNT(*) INTO v_SoPhieu
FROM PHIEUGIAMGIA
WHERE TrangThai = 'Đang có hiệu lực';
DBMS_OUTPUT.PUT_LINE('--- DANH SÁCH PHIẾU GIẢM GIÁ ---');
DBMS_OUTPUT.PUT_LINE('Số phiếu đang có hiệu lực : ' || v_SoPhieu);
FOR r IN c_pgg LOOP
DBMS_OUTPUT.PUT_LINE('- Mã PGG: ' || r.MaPGG ||
                         ' | Mã chữ số: ' || r.MaChuSoPGG ||
                         ' | Trạng thái: ' || r.TrangThai);
END LOOP;
END;
/

DECLARE
    v_msg VARCHAR2(4000);
BEGIN
    SP_ThemPhieuGiamGia(
        p_MaChuSoPGG          => 'hello', -- Đã sửa dấu nháy đơn tại đây
        p_GiaTriGiamGia       => 10000,
        p_GiaTriApDungToiThieu => 50000,
        p_NgayBatDauApDung    => CURRENT_TIMESTAMP,
        p_NgayKetThucApDung   => CURRENT_TIMESTAMP + INTERVAL '30' DAY,
        p_SLToiDa             => 100,
        p_MaNV                => 'NV0001',
        p_TrangThai           => 'Đang có hiệu lực',
        p_outMessage          => v_msg
    );
    DBMS_OUTPUT.PUT_LINE(v_msg);
END;
/


