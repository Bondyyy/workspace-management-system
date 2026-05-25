-- Report-only script: kiểm tra cấu hình charset Oracle và dữ liệu dịch vụ đã bị lưu sai dấu.
-- Nếu các dòng TenDV/TenLoaiDV đã chứa dấu hỏi, cần cập nhật lại dữ liệu gốc bằng script UTF-8.

SELECT *
FROM NLS_DATABASE_PARAMETERS
WHERE PARAMETER IN ('NLS_CHARACTERSET', 'NLS_NCHAR_CHARACTERSET');

SELECT MaDV, TenDV
FROM DICHVU
WHERE TenDV LIKE '%?%'
ORDER BY MaDV;

SELECT MaLoaiDV, TenLoaiDV
FROM LOAIDICHVU
WHERE TenLoaiDV LIKE '%?%'
ORDER BY MaLoaiDV;
