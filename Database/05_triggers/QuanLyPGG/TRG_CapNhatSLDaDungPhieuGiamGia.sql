CREATE OR REPLACE TRIGGER TRG_CapNhatSLDaDungPhieuGiamGia
AFTER INSERT ON HOADON
FOR EACH ROW
BEGIN
    -- Lượt dùng phiếu giảm giá được cộng trong SP_ThanhToanVoiPhieuGiamGia.
    -- Trigger này giữ lại tên cũ để script triển khai không lỗi, nhưng không cộng lần hai.
    NULL;
END TRG_CapNhatSLDaDungPhieuGiamGia;
/
