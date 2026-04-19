-- Cơ sở vật chất & Dịch vụ
-- Bảng Loại Không Gian
CREATE TABLE LOAIKHONGGIAN (
    MaLoaiKG VARCHAR2(50) PRIMARY KEY,
    TenLoaiKG VARCHAR2(100),
    SucChua NUMBER,
    DonGiaTheoGio NUMBER(18, 2)
);

-- Bảng Không Gian
CREATE TABLE KHONGGIAN (
    MaKG VARCHAR2(50) PRIMARY KEY,
    TenKG VARCHAR2(100),
    TrangThaiKG VARCHAR2(50),
    ViTri VARCHAR2(100),
    MaLoaiKG VARCHAR2(50),
    MaCN VARCHAR2(50)
);

-- Bảng Loại Dịch Vụ
CREATE TABLE LOAIDICHVU (
    MaLoaiDV VARCHAR2(50) PRIMARY KEY,
    TenLoaiDV VARCHAR2(100)
);

-- Bảng Dịch Vụ
CREATE TABLE DICHVU (
    MaDV VARCHAR2(50) PRIMARY KEY,
    TenDV VARCHAR2(100),
    TrangThaiDV VARCHAR2(50),
    DonGia NUMBER(18, 2),
    MaLoaiDV VARCHAR2(50)
);


-- Bảng Chi Nhánh
CREATE TABLE CHINHANH (
    MaCN VARCHAR2(50) PRIMARY KEY,
    TenCN VARCHAR2(100),
    ThoiGianMoCua VARCHAR2(8), 
    ThoiGianDongCua VARCHAR2(8),
    DuongDayNong VARCHAR2(20),
    TrangThai VARCHAR2(50),
    DiaChi VARCHAR2(255)
);

-- Bảng Phiên Làm Việc
CREATE TABLE PHIENLAMVIEC (
    MaPhien VARCHAR2(50) PRIMARY KEY,
    ThoiGianBatDau TIMESTAMP,
    ThoiGianDuKienKetThuc TIMESTAMP,
    TrangThaiPhien VARCHAR2(50),
    ThoiGianKetThuc TIMESTAMP,
    CapNhatLanCuoi TIMESTAMP,
    MaKG VARCHAR2(50),
    MaKH VARCHAR2(50),
    MaDatCho VARCHAR2(50)
);

-- Bảng Chi Tiết Dịch Vụ
CREATE TABLE CHITIETDICHVU (
    MaDV VARCHAR2(50),
    MaPhien VARCHAR2(50),
    SoLuong NUMBER,
    GhiChu VARCHAR2(255),
    PRIMARY KEY (MaDV, MaPhien)
);

-- Bảng Đặt Chỗ
CREATE TABLE DATCHO (
    MaDatCho VARCHAR2(50) PRIMARY KEY,
    ThoiGianDat TIMESTAMP,
    ThoiGianDuKienToi TIMESTAMP,
    KhoangThoiGianSuDung NUMBER,
    TrangThaiDatTruoc VARCHAR2(50),
    ThanhTien NUMBER(18, 2),
    GhiChu VARCHAR2(255),
    MaQR VARCHAR2(255),
    CapNhatLanCuoi TIMESTAMP,
    MaKH VARCHAR2(50),
    MaKG VARCHAR2(50)
);

--Nhóm Người dùng, Khách hàng & Nhân sự
-- Bảng Người Dùng
CREATE TABLE NGUOIDUNG (
    MaND VARCHAR2(50) PRIMARY KEY,
    TenTaiKhoan VARCHAR2(50),
    MatKhauMaHoa VARCHAR2(255),
    GioiTinh VARCHAR2(10),
    Email VARCHAR2(100),
    SDT VARCHAR2(20),
    NgaySinh DATE,
    ThoiGianTao TIMESTAMP,
    CapNhatLanCuoi TIMESTAMP,
    LanCuoiDangNhap TIMESTAMP,
    TrangThaiND VARCHAR2(50)
);

-- Bảng Hạng Thành Viên
CREATE TABLE HANGTHANHVIEN (
    MaHangThanhVien VARCHAR2(50) PRIMARY KEY,
    TenHangThanhVien VARCHAR2(50),
    PhanTramTienGiam NUMBER(5, 2),
    TongChiTieuToiThieu NUMBER(18, 2)
);

-- Bảng Khách Hàng
CREATE TABLE KHACHHANG (
    MaKH VARCHAR2(50) PRIMARY KEY,
    HoTenKH VARCHAR2(100),
    LoaiKH VARCHAR2(50),
    TongChiTieu NUMBER(18, 2),
    CapNhatLanCuoi TIMESTAMP,
    MaHangThanhVien VARCHAR2(50),
    MaND VARCHAR2(50), UNIQUE(MaND)
);

-- Bảng Nhân Viên
CREATE TABLE NHANVIEN (
    MaNV VARCHAR2(50) PRIMARY KEY,
    LoaiNV VARCHAR2(50),
    NgayVaoLam DATE,
    TrangThaiLamViec VARCHAR2(50),
    PhuCap NUMBER(18, 2),
    TienThuong NUMBER(18, 2),
    CaLamViec VARCHAR2(50),
    LuongCoBan NUMBER(18, 2),
    MaNQL VARCHAR2(50),
    MaCN VARCHAR2(50),
    MaND VARCHAR2(50), UNIQUE(MaND)
);

--Nhóm Phân quyền
-- Bảng Vai Trò
CREATE TABLE VAITRO (
    MaVaiTro VARCHAR2(50) PRIMARY KEY,
    TenVaiTro VARCHAR2(100),
    MoTa VARCHAR2(255)
);

-- Bảng Chi Tiết Vai Trò
CREATE TABLE CHITIETVAITRO (
    MaND VARCHAR2(50),
    MaVaiTro VARCHAR2(50),
    PRIMARY KEY (MaND, MaVaiTro)
);

-- Bảng Nhóm Chức Năng
CREATE TABLE NHOMCHUCNANG (
    MaNhomChucNang VARCHAR2(50) PRIMARY KEY,
    TenNhomChucNang VARCHAR2(100),
    MoTa VARCHAR2(255)
);

-- Bảng Chi Tiết Nhóm Chức Năng
CREATE TABLE CHITIETNHOMCHUCNANG (
    MaVaiTro VARCHAR2(50),
    MaNhomChucNang VARCHAR2(50),
    PRIMARY KEY (MaVaiTro, MaNhomChucNang)
);

-- Bảng Chức Năng
CREATE TABLE CHUCNANG (
    MaChucNang VARCHAR2(50) PRIMARY KEY,
    TenChucNang VARCHAR2(100),
    MoTa VARCHAR2(255)
);

-- Bảng Chi Tiết Chức Năng
CREATE TABLE CHITIETCHUCNANG (
    MaNhomChucNang VARCHAR2(50),
    MaChucNang VARCHAR2(50),
    PRIMARY KEY (MaNhomChucNang, MaChucNang)
);

--Nhóm Thanh toán & Khuyến mãi
-- Bảng Phiếu Giảm Giá
CREATE TABLE PHIEUGIAMGIA (
    MaPGG VARCHAR2(50) PRIMARY KEY,
    MaChuSoPGG VARCHAR2(100),
    GiaTriGiamGia NUMBER(18, 2),
    GiaTriApDungToiThieu NUMBER(18, 2),
    NgayBatDauApDung TIMESTAMP,
    NgayKetThucApDung TIMESTAMP,
    SLDaDung NUMBER,
    SLToiDa NUMBER,
    NgayTaoPGG TIMESTAMP,
    MaNV VARCHAR2(50)
);

-- Bảng Hóa Đơn
CREATE TABLE HOADON (
    MaHoaDon VARCHAR2(50) PRIMARY KEY,
    SoHD VARCHAR2(50),
    TongTien NUMBER(18, 2),
    ThanhTien NUMBER(18, 2),
    NgayLapHoaDon TIMESTAMP,
    PhuongThucThanhToan VARCHAR2(50),
    TrangThaiThanhToan VARCHAR2(50),
    MaPhien VARCHAR2(50),
    MaPGG VARCHAR2(50),
    MaNV VARCHAR2(50)
);



