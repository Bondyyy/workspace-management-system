CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuKhachHang
AFTER UPDATE OF TrangThaiThanhToan ON HOADON
FOR EACH ROW
DECLARE
    v_MaKH VARCHAR2(50);
    v_MaDatCho VARCHAR2(50);
    v_TienDatChoDaTra NUMBER(18, 2) := 0;
    v_TienVoucher NUMBER(18, 2) := 0;
    v_PhanTramHang NUMBER(5, 2) := 0;
    v_ThanhTienCuoi NUMBER(18, 2) := 0;
    v_TienThucTra NUMBER(18, 2);
BEGIN
    IF NVL(:OLD.TrangThaiThanhToan, ' ') <> 'Đã thanh toán thành công'
       AND :NEW.TrangThaiThanhToan = 'Đã thanh toán thành công'
       AND :NEW.MaPhien IS NOT NULL THEN

        BEGIN
            SELECT MaKH, MaDatCho
            INTO v_MaKH, v_MaDatCho
            FROM PHIENLAMVIEC
            WHERE MaPhien = :NEW.MaPhien;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_MaKH := NULL;
                v_MaDatCho := NULL;
        END;

        IF v_MaKH IS NOT NULL THEN
            SELECT NVL(SUM(NVL(ct.SoTienGiam, 0)), 0)
            INTO v_TienVoucher
            FROM CHITIETAPDUNGPGG ct
            WHERE ct.MaHoaDon = :NEW.MaHoaDon
               OR (v_MaDatCho IS NOT NULL AND ct.MaDatCho = v_MaDatCho);

            SELECT NVL(MAX(htv.PhanTramTienGiam), 0)
            INTO v_PhanTramHang
            FROM KHACHHANG kh
            LEFT JOIN HANGTHANHVIEN htv ON htv.MaHangThanhVien = kh.MaHangThanhVien
            WHERE kh.MaKH = v_MaKH;

            v_ThanhTienCuoi := GREATEST(0,
                NVL(:NEW.TongTien, 0)
                - v_TienVoucher
                - ROUND(GREATEST(0, NVL(:NEW.TongTien, 0) - v_TienVoucher)
                    * LEAST(100, GREATEST(0, NVL(v_PhanTramHang, 0))) / 100, 0)
            );

            IF v_MaDatCho IS NOT NULL THEN
                BEGIN
                    SELECT NVL(ThanhTien, 0)
                    INTO v_TienDatChoDaTra
                    FROM DATCHO
                    WHERE MaDatCho = v_MaDatCho;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        v_TienDatChoDaTra := 0;
                END;
                -- Phần DATCHO đã cộng khi thanh toán web thành công, chỉ cộng phần thu thêm tại quầy.
                v_TienThucTra := GREATEST(0, v_ThanhTienCuoi - v_TienDatChoDaTra);
            ELSE
                v_TienThucTra := v_ThanhTienCuoi;
            END IF;

            IF NVL(v_TienThucTra, 0) > 0 THEN
                UPDATE KHACHHANG
                SET TongChiTieu = NVL(TongChiTieu, 0) + v_TienThucTra,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaKH = v_MaKH;
            END IF;
        END IF;
    END IF;
END TRG_TichLuyChiTieuKhachHang;
/
