-- 1. Bảng NGUOIDUNG
ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_GioiTinh
    CHECK (GioiTinh IN ('Nam', 'Nữ', 'Khác'));

ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_SDT 
    CHECK (LENGTH(SDT) = 10 AND SUBSTR(SDT, 1, 1) = '0' AND REGEXP_LIKE(SDT, '^[0-9]+$'));

ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_Email 
    CHECK (Email LIKE '%@%.%');

ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_TrangThai
    CHECK (TrangThaiND IN ('Đang hoạt động', 'Không hoạt động'));

-- 2. Bảng KHACHHANG (Hội viên)
ALTER TABLE KHACHHANG ADD CONSTRAINT CHK_KH_TongChiTieu 
    CHECK (TongChiTieu >= 0);

-- 3. Bảng HANGTHANHVIEN
ALTER TABLE HANGTHANHVIEN ADD CONSTRAINT CHK_HTV_PhanTram 
    CHECK (PhanTramTienGiam >= 0 AND PhanTramTienGiam <= 100);
    
ALTER TABLE HANGTHANHVIEN ADD CONSTRAINT CHK_HTV_TenHang
    CHECK (TenHangThanhVien IN ('Không có', 'Đồng', 'Bạc', 'Vàng', 'Kim cương'));

-- 4. Bảng DATCHO
ALTER TABLE DATCHO ADD CONSTRAINT CHK_DC_ThoiGianSD 
    CHECK (KhoangThoiGianSuDung >= 1);

ALTER TABLE DATCHO ADD CONSTRAINT CHK_DC_TrangThai 
    CHECK (TrangThaiDatTruoc IN (
        'Đang chờ thanh toán', 'Đã thanh toán thành công', 'Thanh toán không thành công',
        'Đã sử dụng', 'Quá hạn nhận chỗ'));

ALTER TABLE DATCHO ADD CONSTRAINT CHK_DC_ThoiGianDat
    CHECK (ThoiGianDat <= ThoiGianDuKienToi);
-- 5. Bảng PHIENLAMVIEC
ALTER TABLE PHIENLAMVIEC ADD CONSTRAINT CHK_PLV_TrangThai
    CHECK (TrangThaiPhien IN ('Đang hoạt động', 'Đã kết thúc'));

ALTER TABLE PHIENLAMVIEC ADD CONSTRAINT CHK_PLV_ThoiGianDuKien
    CHECK (ThoiGianDuKienKetThuc >= ThoiGianBatDau + INTERVAL '1' HOUR);
-- 6. Bảng DICHVU & CHITIETDICHVU
ALTER TABLE DICHVU ADD CONSTRAINT CHK_DV_DonGia 
    CHECK (DonGia >= 0);

ALTER TABLE LOAIDICHVU ADD CONSTRAINT CHK_LDV_TrangThai
    CHECK (TrangThaiLDV IN ('Đang hoạt động', 'Tạm ngưng', 'Ngừng kinh doanh'));

ALTER TABLE CHITIETDICHVU ADD CONSTRAINT CHK_CTDV_SoLuong
    CHECK (SoLuong > 0);

-- 7. Bảng KHONGGIAN & LOAIKHONGGIAN
ALTER TABLE KHONGGIAN ADD CONSTRAINT CHK_KG_TrangThai
    CHECK (TrangThaiKG IN ('Trống', 'Tạm khoá', 'Đã đặt trước', 'Đang hoạt động', 'Bảo trì'));

ALTER TABLE LOAIKHONGGIAN ADD CONSTRAINT CHK_LKG_SucChua 
    CHECK (SucChua > 0);

ALTER TABLE LOAIKHONGGIAN ADD CONSTRAINT CHK_LKG_DonGia 
    CHECK (DonGiaTheoGio > 0);

ALTER TABLE LOAIKHONGGIAN ADD CONSTRAINT CHK_LKG_TrangThai
    CHECK (TrangThai IN ('Đang hoạt động', 'Ngừng hoạt động'));

-- 8. Bảng CHINHANH
ALTER TABLE CHINHANH ADD CONSTRAINT CHK_CN_MoCua 
    CHECK (REGEXP_LIKE(ThoiGianMoCua, '^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$'));

ALTER TABLE CHINHANH ADD CONSTRAINT CHK_CN_DongCua 
    CHECK (REGEXP_LIKE(ThoiGianDongCua, '^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$'));

ALTER TABLE CHINHANH ADD CONSTRAINT CHK_CN_TrangThai
    CHECK (TrangThai IN ('Đang hoạt động', 'Ngừng hoạt động'));

-- 9. Bảng NHANVIEN (Bao gồm quản lý và lễ tân)
ALTER TABLE NHANVIEN ADD CONSTRAINT CHK_NV_LoaiNV
    CHECK (LoaiNV IN ('Nhân viên', 'Quản lý', 'Quản trị viên Hệ thống'));

ALTER TABLE NHANVIEN ADD CONSTRAINT CHK_NV_LuongCB 
    CHECK (LuongCoBan > 0); 

ALTER TABLE NHANVIEN ADD CONSTRAINT CHK_NV_PhuCap 
    CHECK (PhuCap >= 0);

-- 10. Bảng HOADON
ALTER TABLE HOADON ADD CONSTRAINT CHK_HD_PTTT
    CHECK (PhuongThucThanhToan IN ('Chuyển khoản', 'Tiền mặt', 'Đặt trước'));

ALTER TABLE HOADON ADD CONSTRAINT CHK_HD_TrangThai
    CHECK (TrangThaiThanhToan IN ('Đang chờ thanh toán', 'Đã trả trước', 'Đã thanh toán thành công', 'Thanh toán không thành công'));

ALTER TABLE HOADON ADD CONSTRAINT CHK_HD_TienHopLe
    CHECK (
        TongTien >= 0
        AND ThanhTien >= 0
        AND DaTraTruoc >= 0
        AND ThanhTien <= TongTien
    );
-- 11. Bảng PHIEUGIAMGIA
ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_GiaTri 
    CHECK (GiaTriGiamGia > 0);

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_ToiThieu 
    CHECK (GiaTriApDungToiThieu > 0);

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_SLToiDa 
    CHECK (SLToiDa > 0);

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_TrangThai
    CHECK (TrangThai IN ('Đang có hiệu lực', 'Chưa đến hạn bắt đầu', 'Hết hiệu lực', 'Đã vô hiệu hoá'));

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_SLDaDung
    CHECK (SLDaDung >= 0 AND SLDaDung <= SLToiDa);
