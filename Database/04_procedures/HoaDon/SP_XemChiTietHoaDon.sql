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
    -- 1. Kiểm tra phiên tồn tại
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
    
    -- 2. Thông tin hóa đơn tổng quan
    OPEN p_RS_ThongTinHD FOR
        SELECT 
            HD.MaHoaDon,
            HD.SoHD,
            NVL(HD.TongTien, FN_TinhTongTien(PLV.MaPhien)) AS TongTien,
            NVL(HD.ThanhTien, GREATEST(0, FN_TinhTongTien(PLV.MaPhien) - NVL(HD.DaTraTruoc, 0))) AS ThanhTien,
            HD.NgayLapHoaDon,
            HD.TrangThaiThanhToan,
            HD.PhuongThucThanhToan,
            HD.MaPGG,
            PGG.MaChuSoPGG,
            PGG.GiaTriGiamGia AS GiaTriGiamGiaPGG,
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
        LEFT JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
        LEFT JOIN CHINHANH CN ON KG.MaCN = CN.MaCN
        WHERE HD.MaPhien = p_MaPhien;
    
    -- 3. Chi tiết tiền không gian
    OPEN p_RS_ChiTietKhongGian FOR
        SELECT 
            CASE 
                WHEN PLV.MaDatCho IS NOT NULL THEN 'Thuê ' || KG.TenKG || ' (đã đặt trước)'
                ELSE KG.TenKG 
            END AS TenKG,
            LKG.TenLoaiKG,
            CASE 
                WHEN PLV.MaDatCho IS NOT NULL AND DC.KhoangThoiGianSuDung > 0 THEN ROUND(DC.ThanhTien / DC.KhoangThoiGianSuDung, 2)
                ELSE LKG.DonGiaTheoGio 
            END AS DonGiaTheoGio,
            CASE 
                WHEN PLV.MaDatCho IS NOT NULL THEN DC.KhoangThoiGianSuDung
                ELSE ROUND(
                    EXTRACT(DAY FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) * 24 +
                    EXTRACT(HOUR FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) +
                    EXTRACT(MINUTE FROM (PLV.ThoiGianKetThuc - PLV.ThoiGianBatDau)) / 60,
                    2
                )
            END AS SoGioSuDung,
            FN_TinhTienKhongGian(p_MaPhien) AS ThanhTien
        FROM PHIENLAMVIEC PLV
        JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
        JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG
        LEFT JOIN DATCHO DC ON PLV.MaDatCho = DC.MaDatCho
        WHERE PLV.MaPhien = p_MaPhien;
    
    -- 4. Chi tiết dịch vụ đã gọi
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
