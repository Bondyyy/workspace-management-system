CREATE OR REPLACE PROCEDURE SP_TraCuuHoiVien(
    p_TuKhoa IN VARCHAR2,
    p_MaHangThanhVien IN VARCHAR2,
    p_outCursor OUT SYS_REFCURSOR,
    p_outMessage OUT VARCHAR2
) AS
BEGIN
    -- p_TuKhoa tìm theo MaKH, HoTenKH, SDT, Email
    OPEN p_outCursor FOR
        SELECT KH.MaKH,
               ND.HoTen,
               KH.LoaiKH,
               KH.TongChiTieu,
               KH.CapNhatLanCuoi,
               KH.MaHangThanhVien,
               CASE
                   WHEN HTV.TenHangThanhVien IS NULL OR HTV.TenHangThanhVien = 'Không có' THEN
                       NVL((
                           SELECT TenHangThanhVien
                           FROM HANGTHANHVIEN
                           WHERE MaHangThanhVien = COALESCE(
                               (SELECT MaHangThanhVien FROM HANGTHANHVIEN WHERE TenHangThanhVien = 'Đồng' AND ROWNUM = 1),
                               (SELECT MaHangThanhVien FROM (
                                   SELECT MaHangThanhVien
                                   FROM HANGTHANHVIEN
                                   WHERE TenHangThanhVien <> 'Không có'
                                   ORDER BY NVL(TongChiTieuToiThieu, 0), MaHangThanhVien
                               ) WHERE ROWNUM = 1)
                           )
                       ), 'Đồng')
                   ELSE HTV.TenHangThanhVien
               END AS TenHangThanhVien,
               HTV.PhanTramTienGiam,
               ND.TenTaiKhoan,
               ND.Email,
               ND.SDT,
               ND.TrangThaiND
        FROM KHACHHANG KH
        JOIN NGUOIDUNG ND ON KH.MaND = ND.MaND
        LEFT JOIN HANGTHANHVIEN HTV ON KH.MaHangThanhVien = HTV.MaHangThanhVien
        WHERE (p_TuKhoa IS NULL
               OR UPPER(KH.MaKH)    LIKE '%' || UPPER(p_TuKhoa) || '%'
               OR UPPER(ND.HoTen)   LIKE '%' || UPPER(p_TuKhoa) || '%'
               OR UPPER(ND.SDT)     LIKE '%' || UPPER(p_TuKhoa) || '%'
               OR UPPER(ND.Email)   LIKE '%' || UPPER(p_TuKhoa) || '%')
          AND (p_MaHangThanhVien IS NULL
               OR KH.MaHangThanhVien = p_MaHangThanhVien)
        ORDER BY ND.HoTen;

    p_outMessage := 'Tra cứu hội viên thành công!';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi tra cứu hội viên: ' || SQLERRM;
        RAISE;
END SP_TraCuuHoiVien;
/
