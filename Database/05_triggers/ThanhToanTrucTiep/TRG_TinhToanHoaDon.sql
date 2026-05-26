CREATE OR REPLACE TRIGGER TRG_TinhToanHoaDon
BEFORE INSERT OR UPDATE ON HOADON
FOR EACH ROW
BEGIN
    -- Chỉ tính toán nếu TongTien chưa có hoặc bằng 0
    -- Điều này bảo vệ dữ liệu bạn đã tính toán từ Java (tongTienBanDau) không bị ghi đè
    IF (:NEW.TongTien IS NULL OR :NEW.TongTien = 0) AND :NEW.MaPhien IS NOT NULL THEN
        -- Sử dụng khối BEGIN END lồng nhau để bắt lỗi cục bộ
        -- Nếu hàm FN_TinhTongTien bị lỗi, Trigger vẫn không làm sập câu lệnh INSERT
        BEGIN
            -- Sử dụng EXECUTE IMMEDIATE để tránh lỗi phụ thuộc (dependency) khi biên dịch
            EXECUTE IMMEDIATE 'SELECT FN_TinhTongTien(:1) FROM DUAL' INTO :NEW.TongTien USING :NEW.MaPhien;
            EXECUTE IMMEDIATE 'SELECT FN_TinhThanhTien(:1, :2) FROM DUAL' INTO :NEW.ThanhTien USING :NEW.MaPhien, :NEW.MaPGG;
            
            -- Nếu sau khi tính mà TongTien vẫn = 0 nhưng ThanhTien > 0 (hiếm gặp), 
            -- gán TongTien = ThanhTien để đồng bộ giao diện
            IF (:NEW.TongTien = 0 OR :NEW.TongTien IS NULL) AND :NEW.ThanhTien > 0 THEN
                :NEW.TongTien := :NEW.ThanhTien;
            END IF;
            IF :NEW.TongTienGoc IS NULL OR :NEW.TongTienGoc = 0 THEN
                :NEW.TongTienGoc := NVL(:NEW.TongTien, 0);
            END IF;
            IF :NEW.TienGocPhatSinh IS NULL OR :NEW.TienGocPhatSinh = 0 THEN
                :NEW.TienGocPhatSinh := NVL(:NEW.TongTien, 0);
            END IF;
        EXCEPTION
            WHEN OTHERS THEN
                -- Nếu có lỗi, giữ nguyên giá trị để tránh làm sập nghiệp vụ
                NULL;
        END;
    END IF;

    IF :NEW.DaTraTruoc IS NULL THEN
        :NEW.DaTraTruoc := 0;
    END IF;

    IF :NEW.TongTien IS NULL THEN
        :NEW.TongTien := 0;
    END IF;

    IF :NEW.TongTienGoc IS NULL THEN
        :NEW.TongTienGoc := NVL(:NEW.TongTien, 0);
    END IF;

    IF :NEW.ThanhTien IS NULL THEN
        :NEW.ThanhTien := GREATEST(0, NVL(:NEW.TongTien, 0) - NVL(:NEW.DaTraTruoc, 0));
    END IF;
END;
/

