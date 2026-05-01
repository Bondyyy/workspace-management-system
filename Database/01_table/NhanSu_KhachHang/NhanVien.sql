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

alter table NHANVIEN
add (HoVaTen VARCHAR2(50))

INSERT INTO NHANVIEN (MaNV, HoVaTen, LoaiNV) VALUES ('NV01', 'Nguyễn Văn Bondy', 'Quản lý');
INSERT INTO NHANVIEN (MaNV, HoVaTen, LoaiNV) VALUES ('NV02', 'Trần Thị Lễ Tân', 'Lễ tân');

commit;
select * from NHANVIEN
