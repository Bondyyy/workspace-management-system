CREATE OR REPLACE PROCEDURE sp_TraCuuPhienLamViec (
    p_MaPhien     IN VARCHAR2,
    p_ResultSet   OUT SYS_REFCURSOR,
    p_outMessage  OUT VARCHAR2
)
AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM PHIENLAMVIEC
    WHERE MaPhien = p_MaPhien;

    IF v_count = 0 THEN
        p_outMessage := 'Không tìm thấy phiên làm việc nào với mã: ' || p_MaPhien;
        OPEN p_ResultSet FOR SELECT * FROM DUAL WHERE 1=0;
        RETURN;
    END IF;

    OPEN p_ResultSet FOR
        SELECT
            p.MaPhien,
            p.TrangThaiPhien,
            p.ThoiGianBatDau,
            p.ThoiGianDuKienKetThuc,
            p.ThoiGianKetThuc,
            kg.TenKG AS TenKhongGian,
            kg.ViTri,
            NVL(nd.HoTen, 'Khách vãng lai') AS TenKhachHang,
            kh.LoaiKH,
            p.MaDatCho
        FROM PHIENLAMVIEC p
        LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG
        LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH
        LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND
        WHERE p.MaPhien = p_MaPhien;

    p_outMessage := 'Tra cứu thông tin phiên làm việc thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi hệ thống trong quá trình tra cứu: ' || SQLERRM;
        IF p_ResultSet%ISOPEN THEN
            CLOSE p_ResultSet;
        END IF;
END sp_TraCuuPhienLamViec;
/