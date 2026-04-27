CREATE OR REPLACE PROCEDURE SP_BaoCaoDoanhThu(
    p_MaCN IN VARCHAR2,
    p_TuNgay IN TIMESTAMP,
    p_DenNgay IN TIMESTAMP,
    p_MaLoaiDV IN VARCHAR2,
    p_outTongThanhTien OUT NUMBER,
    p_outTongTruocGiam OUT NUMBER,
    p_outTongChietKhau OUT NUMBER,
    p_outSoHDThanhCong OUT NUMBER,
    p_outSoHDKhongTT OUT NUMBER,
    p_outDoanhThuTienMat OUT NUMBER,
    p_outDoanhThuCK OUT NUMBER,
    p_outMessage OUT VARCHAR2
) AS
BEGIN
    IF p_DenNgay < p_TuNgay THEN
        RAISE_APPLICATION_ERROR(-20060, 'Ngày kết thúc không được nhỏ hơn ngày bắt đầu!');
    END IF;

    SELECT
        NVL(SUM(HD.ThanhTien), 0),
        NVL(SUM(HD.TongTien), 0),
        NVL(SUM(HD.TongTien - HD.ThanhTien), 0),
        COUNT(CASE WHEN HD.TrangThaiThanhToan = 'Thành công' THEN 1 END),
        COUNT(CASE WHEN HD.TrangThaiThanhToan = 'Không thành công' THEN 1 END),
        NVL(SUM(CASE WHEN HD.PhuongThucThanhToan = 'Tiền mặt'
                      AND HD.TrangThaiThanhToan = 'Thành công'
                     THEN HD.ThanhTien ELSE 0 END), 0),
        NVL(SUM(CASE WHEN HD.PhuongThucThanhToan = 'Chuyển khoản'
                      AND HD.TrangThaiThanhToan = 'Thành công'
                     THEN HD.ThanhTien ELSE 0 END), 0)
    INTO
        p_outTongThanhTien, p_outTongTruocGiam, p_outTongChietKhau,
        p_outSoHDThanhCong, p_outSoHDKhongTT,
        p_outDoanhThuTienMat, p_outDoanhThuCK
    FROM HOADON HD
    JOIN PHIENLAMVIEC PLV ON HD.MaPhien = PLV.MaPhien
    JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG
    WHERE HD.NgayLapHoaDon BETWEEN p_TuNgay AND p_DenNgay
      AND (p_MaCN IS NULL OR KG.MaCN = p_MaCN)
      AND (
            p_MaLoaiDV IS NULL
            OR EXISTS (
                SELECT 1
                FROM CHITIETDICHVU CTDV
                JOIN DICHVU DV ON CTDV.MaDV = DV.MaDV
                WHERE CTDV.MaPhien = PLV.MaPhien
                  AND DV.MaLoaiDV = p_MaLoaiDV
            )
          );

    p_outMessage := 'Đã tổng hợp báo cáo doanh thu từ '
                    || TO_CHAR(p_TuNgay, 'DD/MM/YYYY HH24:MI')
                    || ' đến '
                    || TO_CHAR(p_DenNgay, 'DD/MM/YYYY HH24:MI') || '.';

EXCEPTION
    WHEN OTHERS THEN
        p_outMessage := 'Lỗi tổng hợp báo cáo: ' || SQLERRM;
        RAISE;
END SP_BaoCaoDoanhThu;
/
