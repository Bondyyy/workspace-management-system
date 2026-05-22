ALTER TABLE LOAIKHONGGIAN
ADD TRANGTHAI VARCHAR2(100);

ALTER TABLE PHIEUGIAMGIA
ADD TRANGTHAI VARCHAR2(100);

COMMIT;

ALTER TABLE PHIENLAMVIEC add CONSTRAINT CHK_PLV_TrangThai
    CHECK (TrangThaiPhien IN ('Đang chờ thanh toán','Đang hoạt động', 'Đã đặt trước', 'Đã kết thúc'));

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_TrangThai
    CHECK (TrangThai IN ('Đang có hiệu lực', 'Chưa đến hạn bắt đầu', 'Hết hiệu lực', 'Đã vô hiệu hoá'));

ALTER TABLE LOAIKHONGGIAN ADD CONSTRAINT CHK_LKG_TrangThai
    CHECK (TrangThai IN ('Đang hoạt động', 'Ngừng hoạt động'));