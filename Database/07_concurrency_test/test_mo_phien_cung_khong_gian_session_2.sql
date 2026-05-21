SET SERVEROUTPUT ON

-- Chạy khi session 1 đang giữ lock KHONGGIAN.
DEFINE MA_KG = 'KG000001'
DEFINE MA_KH = 'HV000001'
DEFINE MA_PHIEN = 'PLV999901'

DECLARE
    v_message VARCHAR2(4000);
BEGIN
    sp_MoPhienLamViecTrucTiep(
        p_MaKG => '&&MA_KG',
        p_MaKH => '&&MA_KH',
        p_ThoiGianDuKien => SYSTIMESTAMP + INTERVAL '2' HOUR,
        p_MaPhien => '&&MA_PHIEN',
        p_MaDatCho => NULL,
        p_outMessage => v_message
    );
    DBMS_OUTPUT.PUT_LINE(v_message);
END;
/

-- Mong đợi: Không gian đang được nhân viên khác thao tác.
