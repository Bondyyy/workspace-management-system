SET SERVEROUTPUT ON

-- Thay bằng phiên đang Đang hoạt động và có hóa đơn Đang chờ thanh toán.
DEFINE MA_PHIEN = 'PLV000001'

PROMPT Session 1: giữ khóa phiên và hóa đơn, chưa COMMIT/ROLLBACK.
SELECT MaPhien, TrangThaiPhien
FROM PHIENLAMVIEC
WHERE MaPhien = '&&MA_PHIEN'
FOR UPDATE NOWAIT;

SELECT MaHoaDon, TrangThaiThanhToan
FROM HOADON
WHERE MaPhien = '&&MA_PHIEN'
FOR UPDATE NOWAIT;

PROMPT Giữ worksheet này đang mở. Sau đó chạy test_thanh_toan_cung_phien_session_2.sql ở session khác.
PROMPT Khi test xong, chạy ROLLBACK; để nhả khóa.
