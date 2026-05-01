CREATE OR REPLACE PROCEDURE sp_XemChiTietPhienLamViec (
    p_MaPhien          IN  VARCHAR2,
    p_RS_ThongTinPhien OUT SYS_REFCURSOR,
    p_RS_ChiTietDichVu OUT SYS_REFCURSOR,
    p_outMessage       OUT VARCHAR2
)
AS
    v_countPhien NUMBER;
BEGIN
    -- 1. Kiểm tra sự tồn tại của phiên làm việc
    SELECT COUNT(*) INTO v_countPhien
    FROM PHIENLAMVIEC
    WHERE MaPhien = p_MaPhien;

    IF v_countPhien = 0 THEN
        p_outMessage := 'Không tìm thấy phiên làm việc nào với mã: ' || p_MaPhien;
        OPEN p_RS_ThongTinPhien FOR SELECT * FROM DUAL WHERE 1=0;
        OPEN p_RS_ChiTietDichVu FOR SELECT * FROM DUAL WHERE 1=0;
        RETURN;
    END IF;

    OPEN p_RS_ThongTinPhien FOR
        SELECT
            p.MaPhien,
            p.TrangThaiPhien,
            p.ThoiGianBatDau,
            p.ThoiGianDuKienKetThuc,
            p.ThoiGianKetThuc,
            kg.TenKG AS TenKhongGian,
            kg.ViTri,
            NVL(kh.HoTenKH, 'Khách vãng lai') AS TenKhachHang,
            kh.LoaiKH,
            p.MaDatCho
        FROM PHIENLAMVIEC p
        LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG
        LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH
        WHERE p.MaPhien = p_MaPhien;

    OPEN p_RS_ChiTietDichVu FOR
        SELECT
            dv.MaDV,
            dv.TenDV,
            dv.DonGia,
            ct.SoLuong,
            (dv.DonGia * ct.SoLuong) AS ThanhTien,
            ct.GhiChu
        FROM CHITIETDICHVU ct
        INNER JOIN DICHVU dv ON ct.MaDV = dv.MaDV
        WHERE ct.MaPhien = p_MaPhien;

    p_outMessage := 'Truy xuất chi tiết phiên làm việc thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi hệ thống trong quá trình tra cứu: ' || SQLERRM;
        IF p_RS_ThongTinPhien%ISOPEN THEN
            CLOSE p_RS_ThongTinPhien;
        END IF;
        IF p_RS_ChiTietDichVu%ISOPEN THEN
            CLOSE p_RS_ChiTietDichVu;
        END IF;
        RAISE;
END sp_XemChiTietPhienLamViec;
/