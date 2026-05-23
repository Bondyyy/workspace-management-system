SET SERVEROUTPUT ON

-- Thay bằng dịch vụ đang tồn tại trong DB demo.
DEFINE MA_DV = 'DV000001'

PROMPT Session 1: giữ khóa dịch vụ, chưa COMMIT/ROLLBACK.
SELECT MaDV, SoLuong
FROM DICHVU
WHERE MaDV = '&&MA_DV'
FOR UPDATE NOWAIT;

PROMPT Giữ worksheet này đang mở. Sau đó chạy test_nhap_kho_cung_dich_vu_session_2.sql ở session khác.
PROMPT Khi test xong, chạy ROLLBACK; để nhả khóa.
