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
                WHERE TenHangThanhVien <> 'Không có'
                  AND TongChiTieuToiThieu <= NVL(:NEW.TongChiTieu, 0)
                ORDER BY TongChiTieuToiThieu DESC, MaHangThanhVien DESC
            )
            WHERE ROWNUM = 1;

            IF v_MaHangMoi IS NOT NULL AND NVL(:NEW.MaHangThanhVien, ' ') != v_MaHangMoi THEN
                :NEW.MaHangThanhVien := v_MaHangMoi;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                BEGIN
                    SELECT MaHangThanhVien INTO v_MaHangMoi
                    FROM HANGTHANHVIEN
                    WHERE TenHangThanhVien = 'Đồng'
                      AND ROWNUM = 1;

                    IF v_MaHangMoi IS NOT NULL AND NVL(:NEW.MaHangThanhVien, ' ') != v_MaHangMoi THEN
                        :NEW.MaHangThanhVien := v_MaHangMoi;
                    END IF;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        NULL;
                END;
        END;
    END IF;
END;
/

