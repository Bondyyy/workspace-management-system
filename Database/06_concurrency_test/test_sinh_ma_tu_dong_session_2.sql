-- Chạy file này ở session 2 gần như đồng thời với session 1.
-- Cả hai session phải insert thành công và nhận hai MaCN khác nhau.

INSERT INTO CHINHANH (
    TenCN,
    ThoiGianMoCua,
    ThoiGianDongCua,
    DuongDayNong,
    TrangThai,
    DiaChi
) VALUES (
    'Test tự sinh mã session 2',
    '07:00',
    '22:00',
    '0900000002',
    'Đang hoạt động',
    'Dữ liệu test concurrency'
);

SELECT MaCN, TenCN
FROM CHINHANH
WHERE TenCN = 'Test tự sinh mã session 2';

COMMIT;
