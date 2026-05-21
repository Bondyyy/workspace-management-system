SET SERVEROUTPUT ON

-- Chạy khi session 1 đang giữ lock PHIENLAMVIEC/HOADON.
DEFINE MA_PHIEN = 'PLV000001'
DEFINE MA_NV = 'NV000001'

DECLARE
    v_message VARCHAR2(4000);
BEGIN
    SP_ThanhToanVoiPhieuGiamGia(
        p_MaPhien => '&&MA_PHIEN',
        p_MaNV => '&&MA_NV',
        p_MaPGG => NULL,
        p_PhuongThucTT => 'Tiền mặt',
        p_outMessage => v_message
    );
    DBMS_OUTPUT.PUT_LINE(v_message);
END;
/

-- Mong đợi: Dữ liệu thanh toán đang được nhân viên khác xử lý.
