SET SERVEROUTPUT ON

-- Chạy khi session 1 đang giữ lock DICHVU.
DEFINE MA_DV = 'DV000001'
DEFINE TEN_NHAN_VIEN = 'Spring Admin'
DEFINE TEN_LOAI_DV = 'Đồ uống'
DEFINE TEN_DV = 'Nước suối'

BEGIN
    SP_NhapKhoDichVu(
        p_MaDV => '&&MA_DV',
        p_TenNhanVien => '&&TEN_NHAN_VIEN',
        p_TenLoaiDV => '&&TEN_LOAI_DV',
        p_TenDV => '&&TEN_DV',
        p_SoLuong => 5,
        p_TenFile => NULL,
        p_GiaNhap => 5000,
        p_NoiDungFile => NULL
    );
    DBMS_OUTPUT.PUT_LINE('Nhập kho thành công.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE(SQLERRM);
END;
/

-- Mong đợi: Dịch vụ hoặc loại dịch vụ đang được nhân viên khác nhập kho.
