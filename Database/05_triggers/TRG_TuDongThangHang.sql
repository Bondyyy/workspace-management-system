CREATE OR REPLACE TRIGGER TRG_TuDongThangHang
BEFORE UPDATE OF TongChiTieu ON KHACHHANG
FOR EACH ROW
DECLARE
    v_MaHangMoi VARCHAR2(50);
BEGIN
    IF :NEW.TongChiTieu > NVL(:OLD.TongChiTieu, 0) THEN
        BEGIN
            SELECT MaHangThanhVien INTO v_MaHangMoi
            FROM (
                SELECT MaHangThanhVien
                FROM HANGTHANHVIEN
                WHERE TongChiTieuToiThieu <= :NEW.TongChiTieu
                ORDER BY TongChiTieuToiThieu DESC
            )
            WHERE ROWNUM = 1;

            IF v_MaHangMoi IS NOT NULL AND NVL(:OLD.MaHangThanhVien, ' ') != v_MaHangMoi THEN
                :NEW.MaHangThanhVien := v_MaHangMoi;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL;
        END;
    END IF;
END;
/

