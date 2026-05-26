CREATE OR REPLACE PROCEDURE sp_MoPhienLamViecTrucTiep (
    p_MaKG           IN  VARCHAR2,
    p_MaKH           IN  VARCHAR2,
    p_ThoiGianBatDau IN  TIMESTAMP,
    p_ThoiGianDuKien IN  TIMESTAMP,
    p_MaPhien        IN  VARCHAR2,
    p_MaDatCho       IN  VARCHAR2,
    p_outMessage     OUT VARCHAR2
)
AS
    v_TrangThaiKG KHONGGIAN.TrangThaiKG%TYPE;
    v_GioMoCua VARCHAR2(8);
    v_GioDongCua VARCHAR2(8);
    v_ThoiDiemMoCua TIMESTAMP;
    v_ThoiDiemDongCua TIMESTAMP;
    v_RefTime TIMESTAMP;
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);
BEGIN
    IF p_MaKG IS NULL OR LENGTH(TRIM(p_MaKG)) = 0 THEN
        p_outMessage := 'Loi: Thieu ma khong gian.';
        RETURN;
    END IF;

    IF p_MaKH IS NULL OR LENGTH(TRIM(p_MaKH)) = 0 THEN
        p_outMessage := 'Loi: Thieu ma khach hang.';
        RETURN;
    END IF;

    -- 1. Lay thong tin gio hoat dong cua chi nhanh
    BEGIN
        SELECT cn.ThoiGianMoCua, cn.ThoiGianDongCua
        INTO v_GioMoCua, v_GioDongCua
        FROM KHONGGIAN kg
        JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
        WHERE kg.MaKG = TRIM(p_MaKG);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_outMessage := 'Loi: Khong tim thay khong gian ' || p_MaKG || '.';
            RETURN;
    END;

    -- 2. Tinh thoi diem mo/dong cua hom nay dua tren p_ThoiGianBatDau
    v_RefTime := COALESCE(p_ThoiGianBatDau, CURRENT_TIMESTAMP);
    
    -- Xu ly truong hop gio dong cua <= gio mo cua (hoat dong qua dem hoac 24/24)
    v_GioMoCua := TRIM(v_GioMoCua);
    IF v_GioMoCua = '24:00' THEN
        v_GioMoCua := '00:00';
    END IF;
    IF LENGTH(v_GioMoCua) > 5 THEN
        v_GioMoCua := SUBSTR(v_GioMoCua, 1, 5);
    END IF;
    
    v_GioDongCua := TRIM(v_GioDongCua);
    IF v_GioDongCua = '24:00' THEN
        v_GioDongCua := '00:00';
    END IF;
    IF LENGTH(v_GioDongCua) > 5 THEN
        v_GioDongCua := SUBSTR(v_GioDongCua, 1, 5);
    END IF;

    v_ThoiDiemMoCua := TO_TIMESTAMP(TO_CHAR(v_RefTime, 'YYYY-MM-DD') || ' ' || v_GioMoCua, 'YYYY-MM-DD HH24:MI');
    v_ThoiDiemDongCua := TO_TIMESTAMP(TO_CHAR(v_RefTime, 'YYYY-MM-DD') || ' ' || v_GioDongCua, 'YYYY-MM-DD HH24:MI');
    
    IF v_ThoiDiemDongCua <= v_ThoiDiemMoCua THEN
        v_ThoiDiemDongCua := v_ThoiDiemDongCua + INTERVAL '1' DAY;
    END IF;

    -- Tinh thoi diem ca lam viec ngay hom qua (neu dang o trong ca do)
    DECLARE
        v_ThoiDiemMoCua_HomQua TIMESTAMP := v_ThoiDiemMoCua - INTERVAL '1' DAY;
        v_ThoiDiemDongCua_HomQua TIMESTAMP := v_ThoiDiemDongCua - INTERVAL '1' DAY;
        v_TrongCaHomNay BOOLEAN;
        v_TrongCaHomQua BOOLEAN;
        v_ThoiDiemDongCua_HienHanh TIMESTAMP;
    BEGIN
        v_TrongCaHomNay := (v_RefTime >= v_ThoiDiemMoCua AND v_RefTime < v_ThoiDiemDongCua);
        v_TrongCaHomQua := (v_RefTime >= v_ThoiDiemMoCua_HomQua AND v_RefTime < v_ThoiDiemDongCua_HomQua);
        
        IF NOT (v_TrongCaHomNay OR v_TrongCaHomQua) THEN
            IF v_RefTime < v_ThoiDiemMoCua AND v_RefTime >= v_ThoiDiemDongCua_HomQua THEN
                p_outMessage := 'Loi: Chi nhanh chua den gio mo cua. Gio mo cua: ' || TRIM(v_GioMoCua) || '.';
            ELSE
                p_outMessage := 'Loi: Chi nhanh da qua gio hoat dong. Khong the mo phien moi.';
            END IF;
            RETURN;
        END IF;
        
        IF v_TrongCaHomNay THEN
            v_ThoiDiemDongCua_HienHanh := v_ThoiDiemDongCua;
        ELSE
            v_ThoiDiemDongCua_HienHanh := v_ThoiDiemDongCua_HomQua;
        END IF;

        IF p_ThoiGianDuKien > v_ThoiDiemDongCua_HienHanh THEN
            p_outMessage := 'Loi: Thoi gian su dung vuot qua gio dong cua cua chi nhanh. Chi nhanh dong cua luc ' || TRIM(v_GioDongCua) || '.';
            RETURN;
        END IF;
    END;

    -- 4. Kiem tra trang thai khong gian
    SELECT TrangThaiKG
    INTO v_TrangThaiKG
    FROM KHONGGIAN
    WHERE MaKG = TRIM(p_MaKG)
    FOR UPDATE NOWAIT;

    IF p_MaDatCho IS NULL OR LENGTH(TRIM(p_MaDatCho)) = 0 THEN
        IF v_TrangThaiKG <> 'Trống' THEN
            p_outMessage := 'Khong gian hien khong kha dung. Trang thai hien tai: ' || v_TrangThaiKG;
            RETURN;
        END IF;
    ELSIF v_TrangThaiKG NOT IN ('Trống', 'Đã đặt trước', 'Tạm khoá', 'Tạm khóa') THEN
        p_outMessage := 'Khong gian hien khong kha dung. Trang thai hien tai: ' || v_TrangThaiKG;
        RETURN;
    END IF;

    INSERT INTO PHIENLAMVIEC (
        MaPhien,
        ThoiGianBatDau,
        ThoiGianDuKienKetThuc,
        TrangThaiPhien,
        MaKG,
        MaKH,
        MaDatCho,
        CapNhatLanCuoi
    ) VALUES (
        NULLIF(TRIM(p_MaPhien), ''),
        p_ThoiGianBatDau,
        p_ThoiGianDuKien,
        'Đang hoạt động',
        TRIM(p_MaKG),
        TRIM(p_MaKH),
        NULLIF(TRIM(p_MaDatCho), ''),
        CURRENT_TIMESTAMP
    );

    UPDATE KHONGGIAN
    SET TrangThaiKG = 'Đang hoạt động'
    WHERE MaKG = TRIM(p_MaKG);

    IF p_MaDatCho IS NOT NULL AND LENGTH(TRIM(p_MaDatCho)) > 0 THEN
        UPDATE DATCHO
        SET TrangThaiDatTruoc = 'Đã sử dụng',
            MaQR = NULL,
            CapNhatLanCuoi = CURRENT_TIMESTAMP
        WHERE MaDatCho = TRIM(p_MaDatCho);
    END IF;

    COMMIT;
    p_outMessage := 'Mo phien lam viec truc tiep thanh cong!';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_outMessage := 'Loi: Khong tim thay khong gian ' || p_MaKG || '.';
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
        p_outMessage := 'Loi: Ma phien da ton tai.';
    WHEN ex_resource_busy THEN
        ROLLBACK;
        p_outMessage := 'Khong gian dang duoc nhan vien khac thao tac. Vui long thu lai sau.';
    WHEN OTHERS THEN
        ROLLBACK;
        p_outMessage := 'Loi he thong trong qua trinh mo phien: ' || SQLERRM;
END sp_MoPhienLamViecTrucTiep;
/
