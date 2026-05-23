SET SERVEROUTPUT ON

-- Thay bằng không gian đang Trống trong DB demo.
DEFINE MA_KG = 'KG000001'

PROMPT Session 1: giữ khóa không gian, chưa COMMIT/ROLLBACK.
SELECT TrangThaiKG
FROM KHONGGIAN
WHERE MaKG = '&&MA_KG'
FOR UPDATE NOWAIT;

PROMPT Giữ worksheet này đang mở. Sau đó chạy test_mo_phien_cung_khong_gian_session_2.sql ở session khác.
PROMPT Khi test xong, chạy ROLLBACK; để nhả khóa.
