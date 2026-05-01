DROP TABLE DICHVU CASCADE CONSTRAINTS;
CREATE TABLE DICHVU (
    MaDV VARCHAR2(50) PRIMARY KEY,
    TenDV VARCHAR2(100),
    HinhAnh VARCHAR2(500),
    TrangThaiDV VARCHAR2(50),
    DonGia NUMBER(18, 2),
    MaLoaiDV VARCHAR2(50)
);

alter table DICHVU
add (SoLuong Number);

INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV000', 'Thuê thêm giờ', 'thue_gio.png', 'Đang hoạt động', 20000, 'LDV01', 999);

INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV001', 'In ấn A4', 'in_an.png', 'Đang hoạt động', 2000, 'LDV01', 100);

INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV002', 'Cà phê đen đá', 'cafe.png', 'Đang hoạt động', 25000, 'LDV02', 150);

INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV003', 'Trà đào cam sả', 'tra_dao.png', 'Đang hoạt động', 35000, 'LDV02', 80);

INSERT INTO DICHVU (MaDV, TenDV, HinhAnh, TrangThaiDV, DonGia, MaLoaiDV, SoLuong)
VALUES ('DV004', 'Bánh mì ngọt', 'banh_mi.png', 'Đang hoạt động', 15000, 'LDV03', 30);


COMMIT;
