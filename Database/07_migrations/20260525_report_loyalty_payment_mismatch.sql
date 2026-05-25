-- Report-only script: rà soát khách hàng có TongChiTieu lệch so với tổng hóa đơn
-- đã chuyển sang trạng thái thanh toán thành công chuẩn.
-- Không tự trừ/cập nhật vì dữ liệu lịch sử cần đối soát thủ công.

SELECT
    kh.MaKH,
    kh.MaND,
    nd.HoTen,
    NVL(kh.TongChiTieu, 0) AS TongChiTieuHienTai,
    NVL(SUM(CASE
        WHEN hd.TrangThaiThanhToan = 'Đã thanh toán thành công'
        THEN NVL(hd.ThanhTien, 0) + NVL(hd.DaTraTruoc, 0)
        ELSE 0
    END), 0) AS TongHoaDonThanhCong,
    NVL(SUM(CASE
        WHEN NVL(hd.TrangThaiThanhToan, ' ') <> 'Đã thanh toán thành công'
        THEN NVL(hd.ThanhTien, 0) + NVL(hd.DaTraTruoc, 0)
        ELSE 0
    END), 0) AS TongHoaDonChuaThanhCong,
    NVL(kh.TongChiTieu, 0)
        - NVL(SUM(CASE
            WHEN hd.TrangThaiThanhToan = 'Đã thanh toán thành công'
            THEN NVL(hd.ThanhTien, 0) + NVL(hd.DaTraTruoc, 0)
            ELSE 0
        END), 0) AS ChenhLechCanDoiSoat
FROM KHACHHANG kh
JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
LEFT JOIN PHIENLAMVIEC plv ON plv.MaKH = kh.MaKH
LEFT JOIN HOADON hd ON hd.MaPhien = plv.MaPhien
GROUP BY kh.MaKH, kh.MaND, nd.HoTen, kh.TongChiTieu
HAVING NVL(kh.TongChiTieu, 0)
       <> NVL(SUM(CASE
            WHEN hd.TrangThaiThanhToan = 'Đã thanh toán thành công'
            THEN NVL(hd.ThanhTien, 0) + NVL(hd.DaTraTruoc, 0)
            ELSE 0
       END), 0)
ORDER BY ABS(
    NVL(kh.TongChiTieu, 0)
        - NVL(SUM(CASE
            WHEN hd.TrangThaiThanhToan = 'Đã thanh toán thành công'
            THEN NVL(hd.ThanhTien, 0) + NVL(hd.DaTraTruoc, 0)
            ELSE 0
        END), 0)
) DESC;
