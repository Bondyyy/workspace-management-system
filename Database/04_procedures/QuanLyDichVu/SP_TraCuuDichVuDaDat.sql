CREATE OR REPLACE PROCEDURE sp_TraCuuDichVuDaDat (
    p_TuKhoa      IN VARCHAR2,
    p_ResultSet   OUT SYS_REFCURSOR,
    p_outMessage  OUT VARCHAR2
)
AS
    v_TuKhoaTimKiem VARCHAR2(100);
BEGIN
    v_TuKhoaTimKiem := '%' || LOWER(TRIM(p_TuKhoa)) || '%';

    OPEN p_ResultSet FOR
        SELECT
            ctdv.MaPhien,
            dv.TenDV,
            dv.DonGia,
            ctdv.SoLuong,
            (dv.DonGia * ctddv.SoLuong) AS ThanhTien,
            ctdv.GhiChu,
            plv.TrangThaiPhien,
            plv.ThoiGianBatDau,
            kg.TenKG AS ViTriKhongGian,
            NVL(nd.HoTen, 'Khách vãng lai') AS TenKhachHang
        FROM CHITIETDICHVU ctdv
        INNER JOIN DICHVU dv ON ctdv.MaDV = dv.MaDV
        INNER JOIN PHIENLAMVIEC plv ON ctdv.MaPhien = plv.MaPhien
        LEFT JOIN KHONGGIAN kg ON plv.MaKG = kg.MaKG
        LEFT JOIN KHACHHANG kh ON plv.MaKH = kh.MaKH
        LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND
        WHERE LOWER(dv.TenDV) LIKE v_TuKhoaTimKiem
        ORDER BY plv.ThoiGianBatDau DESC;

    p_outMessage := 'Tra cứu thông tin dịch vụ đã đặt thành công!';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_outMessage := 'Không tìm thấy dịch vụ nào phù hợp với từ khóa: ' || p_TuKhoa;
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi trong quá trình tra cứu dịch vụ: ' || SQLERRM;
        IF p_ResultSet%ISOPEN THEN
            CLOSE p_ResultSet;
        END IF;
END sp_TraCuuDichVuDaDat;
/