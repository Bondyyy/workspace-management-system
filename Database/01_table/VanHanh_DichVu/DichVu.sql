CREATE TABLE DICHVU (
    MaDV VARCHAR2(50) PRIMARY KEY,
    TenDV VARCHAR2(100),
    HinhAnh BLOB,
    TrangThaiDV VARCHAR2(50),
    DonGia NUMBER(18, 2),
    MaLoaiDV VARCHAR2(50),
    SoLuong NUMBER,
    GiaGoc NUMBER
);

INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV000', 'Thuê thêm giờ', 'Đang hoạt động', 20000, 'LDV01', 999);

INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV001', 'In ấn A4', 'Đang hoạt động', 2000, 'LDV01', 100);

INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV002', 'Cà phê đen đá', 'Đang hoạt động', 25000, 'LDV02', 150);

INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV003', 'Trà đào cam sả', 'Đang hoạt động', 35000, 'LDV02', 80);

INSERT INTO DICHVU (MaDV, TenDV, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV004', 'Bánh mì ngọt', 'Đang hoạt động', 15000, 'LDV03', 30);

COMMIT;
