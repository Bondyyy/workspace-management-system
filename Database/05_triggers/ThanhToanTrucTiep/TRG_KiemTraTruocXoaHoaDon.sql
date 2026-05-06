CREATE OR REPLACE TRIGGER TRG_KiemTraTruocXoaHoaDon
BEFORE DELETE ON HOADON
FOR EACH ROW
DECLARE
    v_TrangThaiPhien PHIENLAMVIEC.TrangThaiPhien%TYPE;
BEGIN
    -- 1. Không cho phép xóa hóa đơn đã thanh toán
    IF :OLD.TrangThaiThanhToan = 'Đã thanh toán thành công' OR :OLD.TrangThaiThanhToan = 'Đã thanh toán' THEN
        RAISE_APPLICATION_ERROR(-20050, 'Loi - Khong the xoa hoa don da thanh toan thanh cong!');
    END IF;

    -- 2. Không cho phép xóa hóa đơn nếu phiên làm việc đang chạy
    IF :OLD.MaPhien IS NOT NULL THEN
        BEGIN
            SELECT TrangThaiPhien INTO v_TrangThaiPhien
            FROM PHIENLAMVIEC
            WHERE MaPhien = :OLD.MaPhien;

            IF v_TrangThaiPhien = 'Đang hoạt động' THEN
                RAISE_APPLICATION_ERROR(-20051, 'Loi - Khong the xoa hoa don vi phien lam viec tuong ung dang hoat dong!');
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL;
        END;
    END IF;
END;
/
