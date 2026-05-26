-- Report-only: liệt kê không gian đang bị gán trạng thái vật lý "Đã đặt trước"
-- nhưng không còn đặt chỗ/phiên đang chặn ở thời điểm hiện tại.
-- Không cập nhật dữ liệu. Nếu cần cleanup, xem kết quả trước rồi viết migration riêng.

SELECT kg.MaKG,
       kg.TenKG,
       kg.TrangThaiKG,
       kg.MaCN,
       cn.TenCN,
       CASE
           WHEN EXISTS (
               SELECT 1
               FROM PHIENLAMVIEC p
               WHERE p.MaKG = kg.MaKG
                 AND p.TrangThaiPhien = 'Đang hoạt động'
           ) THEN 'Có phiên đang hoạt động'
           WHEN EXISTS (
               SELECT 1
               FROM DATCHO dc
               WHERE dc.MaKG = kg.MaKG
                 AND (
                     dc.TrangThaiDatTruoc IN ('Đã thanh toán thành công', 'Đã đặt trước')
                     OR (
                         dc.TrangThaiDatTruoc = 'Đang chờ thanh toán'
                         AND dc.ThoiGianDat >= CAST(CURRENT_TIMESTAMP AS TIMESTAMP) - INTERVAL '10' MINUTE
                     )
                 )
                 AND CAST(CURRENT_TIMESTAMP AS TIMESTAMP) < dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR')
                 AND CAST(CURRENT_TIMESTAMP AS TIMESTAMP) > dc.ThoiGianDuKienToi
           ) THEN 'Có đặt chỗ đang chặn hiện tại'
           ELSE 'Có thể cần trả về trạng thái vật lý Trống'
       END AS GoiY
FROM KHONGGIAN kg
LEFT JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
WHERE kg.TrangThaiKG = 'Đã đặt trước'
ORDER BY kg.MaCN, kg.MaKG;
