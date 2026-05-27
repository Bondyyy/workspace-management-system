DECLARE
    v_MaHangDong HANGTHANHVIEN.MaHangThanhVien%TYPE;
BEGIN
    BEGIN
        SELECT MaHangThanhVien INTO v_MaHangDong
        FROM HANGTHANHVIEN
        WHERE TenHangThanhVien = 'Đồng'
          AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            SELECT MaHangThanhVien INTO v_MaHangDong
            FROM (
                SELECT MaHangThanhVien
                FROM HANGTHANHVIEN
                WHERE TenHangThanhVien <> 'Không có'
                ORDER BY NVL(TongChiTieuToiThieu, 0), MaHangThanhVien
            )
            WHERE ROWNUM = 1;
    END;

    UPDATE KHACHHANG kh
    SET kh.MaHangThanhVien = v_MaHangDong,
        kh.TongChiTieu = NVL(kh.TongChiTieu, 0),
        kh.CapNhatLanCuoi = CURRENT_TIMESTAMP
    WHERE kh.LoaiKH = 'Hội viên'
      AND (
          kh.MaHangThanhVien IS NULL
          OR EXISTS (
              SELECT 1
              FROM HANGTHANHVIEN htv
              WHERE htv.MaHangThanhVien = kh.MaHangThanhVien
                AND htv.TenHangThanhVien = 'Không có'
          )
      );

    COMMIT;
END;
/
