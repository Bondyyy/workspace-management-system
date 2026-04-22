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