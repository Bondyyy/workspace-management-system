CREATE OR REPLACE PROCEDURE sp_MoPhienLamViecTrucTiep ( 
    p_MaKG           IN  VARCHAR2, 
    p_MaKH           IN  VARCHAR2, 
    p_ThoiGianDuKien IN  TIMESTAMP, 
    p_MaPhien        IN  VARCHAR2,  -- Mã phiên đã được sinh từ Java
    p_MaDatCho       IN  VARCHAR2,  -- Mã đặt chỗ (nếu có)
    p_outMessage     OUT VARCHAR2 
) 
AS 
    v_TrangThaiKG VARCHAR2(50); 
BEGIN 
    -- 1. Khóa dòng và kiểm tra trạng thái không gian
    BEGIN 
        SELECT TrangThaiKG INTO v_TrangThaiKG 
        FROM KHONGGIAN 
        WHERE MaKG = p_MaKG 
        FOR UPDATE NOWAIT;  
    EXCEPTION 
        WHEN NO_DATA_FOUND THEN 
            p_outMessage := 'Lỗi: Không tìm thấy mã không gian ' || p_MaKG; 
            RETURN; 
        WHEN OTHERS THEN 
            p_outMessage := 'Lỗi: Không gian này đang được nhân viên khác thao tác. Vui lòng thử lại sau.'; 
            RETURN; 
    END; 

    IF v_TrangThaiKG != 'Trống' THEN 
        p_outMessage := 'Vị trí không khả dụng để phục vụ'; 
        RETURN; 
    END IF; 

    -- 2. Thêm phiên làm việc 
    INSERT INTO PHIENLAMVIEC ( 
        MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien, MaKG, MaKH, MaDatCho, CapNhatLanCuoi
    ) VALUES ( 
        p_MaPhien, SYSTIMESTAMP, p_ThoiGianDuKien, 'Đang hoạt động', p_MaKG, p_MaKH, p_MaDatCho, SYSTIMESTAMP
    ); 

    -- 3. Cập nhật trạng thái không gian 
    UPDATE KHONGGIAN 
    SET TrangThaiKG = 'Đang hoạt động' 
    WHERE MaKG = p_MaKG; 

    -- 4. Nếu có mã đặt chỗ, cập nhật trạng thái đặt chỗ thành 'Đang phục vụ'
    IF p_MaDatCho IS NOT NULL THEN
        UPDATE DATCHO 
        SET TrangThaiDatTruoc = 'Đang phục vụ'
        WHERE MaDatCho = p_MaDatCho;
    END IF;

    COMMIT; 

    p_outMessage := 'Mở phiên làm việc trực tiếp thành công!'; 

EXCEPTION 
    WHEN OTHERS THEN 
        ROLLBACK; 
        p_outMessage := 'Lỗi hệ thống trong quá trình mở phiên: ' || SQLERRM; 
END sp_MoPhienLamViecTrucTiep; 
/