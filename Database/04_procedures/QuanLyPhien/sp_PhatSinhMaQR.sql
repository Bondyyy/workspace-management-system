CREATE OR REPLACE PROCEDURE pro_SinhMaQR (
    p_MaDatCho IN VARCHAR2,
    p_MaQR OUT VARCHAR2
)
IS
BEGIN
    -- TO_CHAR định dạng thời gian hiện tại -> chuỗi (VD: 20260427_094045)
    -- Ghép Mã đặt chỗ và chuỗi thời gian để tạo thành Mã QR duy nhất
    p_MaQR := p_MaDatCho || '_' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDD_HH24MISS');
END pro_SinhMaQR;
/