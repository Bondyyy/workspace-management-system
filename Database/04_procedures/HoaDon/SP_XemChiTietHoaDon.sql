CREATE OR REPLACE PROCEDURE SP_XemChiTietHoaDon(
    p_MaPhien IN VARCHAR2,
    p_RS_ThongTinHD OUT SYS_REFCURSOR,
    p_RS_ChiTietKhongGian OUT SYS_REFCURSOR,
    p_RS_ChiTietDichVu OUT SYS_REFCURSOR,
    p_outMessage OUT VARCHAR2
) AS
    v_CountPhien NUMBER;
    v_TrangThaiPhien VARCHAR2(50);
BEGIN
    SELECT COUNT(*), MAX(TrangThaiPhien)
    INTO v_CountPhien, v_TrangThaiPhien
    FROM PHIENLAMVIEC
    WHERE MaPhien = p_MaPhien;

    IF v_CountPhien = 0 THEN
        RAISE_APPLICATION_ERROR(-20310, 'Không tìm thấy phiên làm việc [' || p_MaPhien || ']!');
    END IF;

    IF v_TrangThaiPhien != 'Đã kết thúc' THEN
        RAISE_APPLICATION_ERROR(-20311,
            'Phiên chưa kết thúc! Vui lòng kết thúc phiên trước khi thanh toán.');
    END IF;

    OPEN p_RS_ThongTinHD FOR
        SELECT
            HD.MaHoaDon,
            HD.SoHD,
            NVL(NULLIF(HD.TongTienGoc, 0), NVL(HD.TongTien, FN_TinhTongTien(PLV.MaPhien))) AS TongTienGoc,
            NVL(HD.TongTien, FN_TinhTongTien(PLV.MaPhien)) AS TongTien,
            NVL(HD.ThanhTien, FN_TinhThanhTien(PLV.MaPhien, HD.MaPGG)) AS ThanhTien,
            HD.NgayLapHoaDon,
            HD.TrangThaiThanhToan,
            HD.PhuongThucThanhToan,
            HD.MaPGG,
            HD.MaPGGDatTruoc,
            PGGDT.MaChuSoPGG AS MaChuSoPGGDatTruoc,
            HD.MaPGGTaiQuay,
            PGGTQ.MaChuSoPGG AS MaChuSoPGGTaiQuay,
            PGG.MaChuSoPGG,
            PGG.GiaTriGiamGia AS GiaTriGiamGiaPGG,
            NVL(HD.DaTraTruoc, 0) AS DaTraTruoc,
            NVL(HD.TienGocDatTruoc, 0) AS TienGocDatTruoc,
            NVL(HD.TienGocPhatSinh, 0) AS TienGocPhatSinh,
            NVL(HD.TienGiamVoucherDatTruoc, 0) AS TienGiamVoucherDatTruoc,
            NVL(HD.PhanTramGiamHangTVDatTruoc, 0) AS PhanTramGiamHangTVDatTruoc,
            NVL(HD.TienGiamHangTVDatTruoc, 0) AS TienGiamHangTVDatTruoc,
            NVL(HD.TienGiamVoucherTaiQuay, 0) AS TienGiamVoucherTaiQuay,
            NVL(HD.PhanTramGiamHangTVTaiQuay, 0) AS PhanTramGiamHangTVTaiQuay,
            NVL(HD.TienGiamHangTVTaiQuay, 0) AS TienGiamHangTVTaiQuay,
            NVL(HD.TongTienGiam, 0) AS TongTienGiam,
            NVL(HD.SoTienThanhToanTaiQuay, 0) AS SoTienThanhToanTaiQuay,
            NVL(ND.HoTen, 'Khách vãng lai') AS TenKhachHang,
            HTV.TenHangThanhVien,
            HTV.PhanTramTienGiam AS PhanTramGiamHangTV,
            PLV.ThoiGianBatDau,
            PLV.ThoiGianKetThuc,
            KG.TenKG AS TenKhongGian,
            CN.TenCN AS TenChiNhanh
        FROM HOADON HD
        JOIN PHIENLAMVIEC PLV ON HD.MaPhien = PLV.MaPhien
        LEFT JOIN KHACHHANG KH ON PLV.MaKH = KH.MaKH
        LEFT JOIN NGUOIDUNG ND ON KH.MaND = ND.MaND
        LEFT JOIN HANGTHANHVIEN HTV ON KH.MaHangThanhVien = HTV.MaHangThanhVien
        LEFT JOIN PHIEUGIAMGIA PGG ON HD.MaPGG = PGG.MaPGG
        LEFT JOIN PHIEUGIAMGIA PGGDT ON HD.MaPGGDatTruoc = PGGDT.MaPGG
        LEFT JOIN PHIEUGIAMGIA PGGTQ ON HD.MaPGGTaiQuay = PGGTQ.MaPGG
        LEFT JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
        LEFT JOIN CHINHANH CN ON KG.MaCN = CN.MaCN
        WHERE HD.MaPhien = p_MaPhien;

    OPEN p_RS_ChiTietKhongGian FOR
        SELECT
            CASE
                WHEN PLV.MaDatCho IS NOT NULL THEN 'Thuê ' || KG.TenKG || ' (đã đặt trước)'
                ELSE KG.TenKG
            END AS TenKG,
            LKG.TenLoaiKG,
            LKG.DonGiaTheoGio AS DonGiaTheoGio,
            CASE
                WHEN PLV.MaDatCho IS NOT NULL THEN DC.KhoangThoiGianSuDung
                ELSE ROUND(
                    EXTRACT(DAY FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) * 24 +
                    EXTRACT(HOUR FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) +
                    EXTRACT(MINUTE FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) / 60,
                    2
                )
            END AS SoGioSuDung,
            CASE
                WHEN PLV.MaDatCho IS NOT NULL THEN
                    CASE
                        WHEN NVL(DC.TongTienGoc, 0) > 0 THEN ROUND(DC.TongTienGoc, 2)
                        ELSE ROUND(NVL(LKG.DonGiaTheoGio, 0) * NVL(DC.KhoangThoiGianSuDung, 0), 2)
                    END
                ELSE FN_TinhTienKhongGian(p_MaPhien)
            END AS ThanhTien
        FROM PHIENLAMVIEC PLV
        JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
        JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
        LEFT JOIN DATCHO DC ON PLV.MaDatCho = DC.MaDatCho
        WHERE PLV.MaPhien = p_MaPhien;

    OPEN p_RS_ChiTietDichVu FOR
        SELECT
            DV.TenDV,
            DV.DonGia,
            CTDV.SoLuong,
            (DV.DonGia * CTDV.SoLuong) AS ThanhTien,
            CTDV.GhiChu
        FROM CHITIETDICHVU CTDV
        JOIN DICHVU DV ON CTDV.MaDV = DV.MaDV
        WHERE CTDV.MaPhien = p_MaPhien
        ORDER BY DV.TenDV;

    p_outMessage := 'Truy xuất chi tiết hóa đơn thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi xem chi tiết hóa đơn: ' || SQLERRM;
        IF p_RS_ThongTinHD%ISOPEN THEN CLOSE p_RS_ThongTinHD; END IF;
        IF p_RS_ChiTietKhongGian%ISOPEN THEN CLOSE p_RS_ChiTietKhongGian; END IF;
        IF p_RS_ChiTietDichVu%ISOPEN THEN CLOSE p_RS_ChiTietDichVu; END IF;
        RAISE;
END SP_XemChiTietHoaDon;
/
