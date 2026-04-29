CREATE OR REPLACE TRIGGER TRG_KiemTraPhieuGiamGia
BEFORE INSERT OR UPDATE OF MaPGG ON HOADON
FOR EACH ROW
WHEN (NEW.MaPGG IS NOT NULL)
DECLARE
    v_NgayBatDau TIMESTAMP;
    v_NgayKetThuc TIMESTAMP;
    v_GiaTriToiThieu NUMBER(18, 2);
    v_SLDaDung NUMBER;
    v_SLToiDa NUMBER;
    v_MaChuSoPGG VARCHAR2(100);
    v_TongTien NUMBER(18, 2);
BEGIN
    -- 1. Lấy thông tin phiếu giảm giá
    BEGIN
        SELECT 
            NgayBatDauApDung,
            NgayKetThucApDung,
            GiaTriApDungToiThieu,
            SLDaDung,
            SLToiDa,
            MaChuSoPGG
        INTO 
            v_NgayBatDau,
            v_NgayKetThuc,
            v_GiaTriToiThieu,
            v_SLDaDung,
            v_SLToiDa,
            v_MaChuSoPGG
        FROM PHIEUGIAMGIA
        WHERE MaPGG = :NEW.MaPGG;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(
                -20020,
                'Lỗi: Mã phiếu giảm giá [' || :NEW.MaPGG || '] không tồn tại!'
            );
    END;
    
    -- 2. Kiểm tra thời gian hiệu lực
    IF SYSTIMESTAMP < v_NgayBatDau THEN
        RAISE_APPLICATION_ERROR(
            -20021,
            'Lỗi: Phiếu giảm giá [' || v_MaChuSoPGG || '] chưa đến thời gian áp dụng! '
            || 'Có hiệu lực từ: ' || TO_CHAR(v_NgayBatDau, 'DD/MM/YYYY HH24:MI')
        );
    END IF;
    
    IF SYSTIMESTAMP > v_NgayKetThuc THEN
        RAISE_APPLICATION_ERROR(
            -20022,
            'Lỗi: Phiếu giảm giá [' || v_MaChuSoPGG || '] đã hết hạn! '
            || 'Hết hiệu lực: ' || TO_CHAR(v_NgayKetThuc, 'DD/MM/YYYY HH24:MI')
        );
    END IF;
    
    -- 3. Kiểm tra số lượng còn lại
    IF v_SLDaDung >= v_SLToiDa THEN
        RAISE_APPLICATION_ERROR(
            -20023,
            'Lỗi: Phiếu giảm giá [' || v_MaChuSoPGG || '] đã hết lượt sử dụng! '
            || '(Đã dùng: ' || v_SLDaDung || '/' || v_SLToiDa || ')'
        );
    END IF;
    
    -- 4. Kiểm tra giá trị áp dụng tối thiểu
    -- Lấy TongTien từ hóa đơn (nếu đang UPDATE) hoặc tính toán tạm (nếu INSERT)
    v_TongTien := :NEW.TongTien;
    
    IF v_TongTien < v_GiaTriToiThieu THEN
        RAISE_APPLICATION_ERROR(
            -20024,
            'Lỗi: Giá trị hóa đơn (' || TO_CHAR(v_TongTien, '999,999,999.99') || ' VNĐ) '
            || 'chưa đạt điều kiện áp dụng phiếu giảm giá [' || v_MaChuSoPGG || ']! '
            || 'Yêu cầu tối thiểu: ' || TO_CHAR(v_GiaTriToiThieu, '999,999,999.99') || ' VNĐ'
        );
    END IF;
END TRG_KiemTraPhieuGiamGia;
/
