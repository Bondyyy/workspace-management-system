CREATE OR REPLACE PROCEDURE SP_NhapKhoDichVu (
    p_MaDV IN VARCHAR2,
    p_TenNhanVien IN VARCHAR2,
    p_TenLoaiDV IN VARCHAR2,
    p_TenDV IN VARCHAR2,
    p_SoLuong IN NUMBER,
    p_TenFile IN VARCHAR2,
    p_GiaNhap IN NUMBER,
    p_NoiDungFile IN BLOB
) AS
    v_MaLoaiDV LOAIDICHVU.MaLoaiDV%TYPE;
    v_MaDV DICHVU.MaDV%TYPE;
    v_MaChungTu CHUNGTUNHAPKHO.MaChungTu%TYPE;
    v_TenFile CHUNGTUNHAPKHO.TenFile%TYPE;
    v_SoLuongHienTai NUMBER;
    v_SoLuongLuu NUMBER;
    v_MaNV NHANVIEN.MaNV%TYPE;
    v_MaCN NHANVIEN.MaCN%TYPE;
    v_DaTaoDichVuMoi NUMBER(1) := 0;
    ex_resource_busy EXCEPTION;
    PRAGMA EXCEPTION_INIT(ex_resource_busy, -54);

    FUNCTION co_gia_tri(p_value VARCHAR2) RETURN BOOLEAN IS
    BEGIN
        RETURN p_value IS NOT NULL AND LENGTH(TRIM(p_value)) > 0;
    END;
BEGIN
    IF p_SoLuong IS NULL OR p_SoLuong <= 0 THEN
        RAISE_APPLICATION_ERROR(-20400, 'Số lượng nhập kho phải lớn hơn 0.');
    END IF;

    IF NOT co_gia_tri(p_TenLoaiDV) THEN
        RAISE_APPLICATION_ERROR(-20401, 'Tên loại dịch vụ không được để trống.');
    END IF;

    IF NOT co_gia_tri(p_TenDV) AND NOT co_gia_tri(p_MaDV) THEN
        RAISE_APPLICATION_ERROR(-20402, 'Tên dịch vụ hoặc mã dịch vụ không được để trống.');
    END IF;

    BEGIN
        SELECT nv.MaNV, nv.MaCN
        INTO v_MaNV, v_MaCN
        FROM NHANVIEN nv
        JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND
        WHERE LOWER(TRIM(nd.HoTen)) = LOWER(TRIM(p_TenNhanVien))
          AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20403, 'Không tìm thấy nhân viên nhập kho: ' || p_TenNhanVien);
    END;

    IF LOWER(TRIM(p_TenLoaiDV)) LIKE '%tiện ích%' THEN
        v_SoLuongLuu := NULL;
    ELSE
        v_SoLuongLuu := p_SoLuong;
    END IF;

    BEGIN
        SELECT MaLoaiDV
        INTO v_MaLoaiDV
        FROM LOAIDICHVU
        WHERE LOWER(TRIM(TenLoaiDV)) = LOWER(TRIM(p_TenLoaiDV))
          AND ROWNUM = 1
        FOR UPDATE NOWAIT;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            LOCK TABLE LOAIDICHVU IN SHARE ROW EXCLUSIVE MODE NOWAIT;
            BEGIN
                SELECT MaLoaiDV
                INTO v_MaLoaiDV
                FROM LOAIDICHVU
                WHERE LOWER(TRIM(TenLoaiDV)) = LOWER(TRIM(p_TenLoaiDV))
                  AND ROWNUM = 1;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_MaLoaiDV := 'LDV' || LPAD(SEQ_LOAIDV_NEW.NEXTVAL, 6, '0');
                    INSERT INTO LOAIDICHVU (MaLoaiDV, TenLoaiDV, TrangThaiLDV)
                    VALUES (v_MaLoaiDV, TRIM(p_TenLoaiDV), 'Đang hoạt động');
            END;
    END;

    IF co_gia_tri(p_MaDV) THEN
        BEGIN
            SELECT MaDV, NVL(SoLuong, 0)
            INTO v_MaDV, v_SoLuongHienTai
            FROM DICHVU
            WHERE MaDV = TRIM(p_MaDV)
            FOR UPDATE NOWAIT;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_MaDV := NULL;
        END;
    ELSE
        BEGIN
            SELECT MaDV, NVL(SoLuong, 0)
            INTO v_MaDV, v_SoLuongHienTai
            FROM DICHVU
            WHERE LOWER(TRIM(TenDV)) = LOWER(TRIM(p_TenDV))
              AND ROWNUM = 1
            FOR UPDATE NOWAIT;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_MaDV := NULL;
        END;
    END IF;

    IF v_MaDV IS NULL THEN
        LOCK TABLE DICHVU IN SHARE ROW EXCLUSIVE MODE NOWAIT;

        BEGIN
            SELECT MaDV, NVL(SoLuong, 0)
            INTO v_MaDV, v_SoLuongHienTai
            FROM DICHVU
            WHERE LOWER(TRIM(TenDV)) = LOWER(TRIM(p_TenDV))
              AND ROWNUM = 1
            FOR UPDATE NOWAIT;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_MaDV := 'DV' || LPAD(SEQ_DICHVU_NEW.NEXTVAL, 6, '0');
                v_DaTaoDichVuMoi := 1;
                INSERT INTO DICHVU (
                    MaDV,
                    TenDV,
                    HinhAnh,
                    TrangThaiDV,
                    DonGia,
                    MaLoaiDV,
                    SoLuong,
                    GiaNhap
                ) VALUES (
                    v_MaDV,
                    TRIM(p_TenDV),
                    NULL,
                    'Đang hoạt động',
                    0,
                    v_MaLoaiDV,
                    v_SoLuongLuu,
                    p_GiaNhap
                );
        END;
    END IF;

    IF v_MaDV IS NOT NULL AND v_DaTaoDichVuMoi = 0 THEN
        UPDATE DICHVU
        SET SoLuong = CASE
                WHEN v_SoLuongLuu IS NULL THEN NULL
                ELSE NVL(SoLuong, 0) + v_SoLuongLuu
            END,
            MaLoaiDV = v_MaLoaiDV,
            GiaNhap = p_GiaNhap
        WHERE MaDV = v_MaDV;
    END IF;

    v_MaChungTu := 'CT' || LPAD(SEQ_CHUNGTU.NEXTVAL, 6, '0');
    IF co_gia_tri(p_TenFile) THEN
        v_TenFile := TRIM(p_TenFile);
    ELSE
        v_TenFile := NULL;
    END IF;

    INSERT INTO CHUNGTUNHAPKHO (
        MaChungTu,
        MaDV,
        MaNV,
        MaCN,
        TenFile,
        NoiDungFile,
        NgayNhap,
        SoLuongNhap
    ) VALUES (
        v_MaChungTu,
        v_MaDV,
        v_MaNV,
        v_MaCN,
        v_TenFile,
        p_NoiDungFile,
        SYSDATE,
        v_SoLuongLuu
    );

    COMMIT;
EXCEPTION
    WHEN ex_resource_busy THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20410, 'Dịch vụ hoặc loại dịch vụ đang được nhân viên khác nhập kho. Vui lòng thử lại sau.');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
