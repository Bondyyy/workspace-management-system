-- Chạy file này ở session 1 sau khi đã chạy BangWMS.sql và TaoMaTuDong.sql.
-- Mục tiêu: insert không truyền MaCN, Oracle sequence + trigger sẽ tự sinh mã không trùng.

INSERT INTO CHINHANH (
    TenCN,
    ThoiGianMoCua,
    ThoiGianDongCua,
    DuongDayNong,
    TrangThai,
    DiaChi
) VALUES (
    'Test tự sinh mã session 1',
    '07:00',
    '22:00',
    '0900000001',
    'Đang hoạt động',
    'Dữ liệu test concurrency'
);

SELECT MaCN, TenCN
FROM CHINHANH
WHERE TenCN = 'Test tự sinh mã session 1';

COMMIT;
