package com.wms.service;

import com.wms.dao.PhieuGiamGiaDAO;

import com.wms.model.PhieuGiamGiaDTO;
import com.wms.model.XacNhanPhieuGiamGiaDTO;
import java.util.Date;

/**
 * Service xử lý logic kiểm tra và áp dụng phiếu giảm giá
 */
public class XacNhanPhieuGiamGiaService {
    
    private final PhieuGiamGiaDAO voucherDAO;
    
    public XacNhanPhieuGiamGiaService() {
        this.voucherDAO = new PhieuGiamGiaDAO();
    }
    
    public XacNhanPhieuGiamGiaService(PhieuGiamGiaDAO voucherDAO) {
        this.voucherDAO = voucherDAO;
    }
    
    /**
     * Kiểm tra và xác thực phiếu giảm giá
     * 
     * @param maChuSoPGG Mã phiếu giảm giá cần kiểm tra
     * @param tongTienGoc Tổng tiền gốc của hóa đơn
     * @return XacNhanPhieuGiamGiaDTO chứa kết quả kiểm tra
     */
    public XacNhanPhieuGiamGiaDTO kiemTraPhieuGiamGia(String maChuSoPGG, double tongTienGoc) {
        // 1. Kiểm tra input
        if (maChuSoPGG == null || maChuSoPGG.trim().isEmpty()) {
            return new XacNhanPhieuGiamGiaDTO(false, 
                "[Lỗi] Vui lòng nhập mã phiếu giảm giá", 
                "EMPTY_VOUCHER_CODE");
        }
        
        maChuSoPGG = maChuSoPGG.trim().toUpperCase();
        
        // 2. Kiểm tra mã tồn tại
        PhieuGiamGiaDTO voucher = voucherDAO.layThongTinVoucher(maChuSoPGG);
        
        if (voucher == null) {
            return new XacNhanPhieuGiamGiaDTO(false, 
                "[Lỗi] Mã phiếu giảm giá không tồn tại", 
                "VOUCHER_NOT_FOUND");
        }
        
        // 3. Kiểm tra ngày bắt đầu áp dụng
        Date now = new Date();
        if (voucher.getNgayBatDauApDung() != null && now.before(voucher.getNgayBatDauApDung())) {
            return new XacNhanPhieuGiamGiaDTO(false, 
                "[Lỗi] Mã phiếu giảm giá chưa đến ngày áp dụng", 
                "VOUCHER_NOT_STARTED");
        }
        
        // 4. Kiểm tra ngày kết thúc áp dụng (hết hạn)
        if (voucher.getNgayKetThucApDung() != null && now.after(voucher.getNgayKetThucApDung())) {
            return new XacNhanPhieuGiamGiaDTO(false, 
                "[Lỗi] Mã phiếu giảm giá đã hết hạn sử dụng", 
                "VOUCHER_EXPIRED");
        }
        
        // 5. Kiểm tra số lượng sử dụng
        if (voucher.getSlDaDung() >= voucher.getSlToiDa()) {
            return new XacNhanPhieuGiamGiaDTO(false, 
                "[Lỗi] Mã phiếu giảm giá đã hết lượt sử dụng", 
                "VOUCHER_USAGE_EXCEEDED");
        }
        
        // 6. Kiểm tra giá trị tối thiểu của hóa đơn
        if (voucher.getGiaTriApDungToiThieu() > 0 && tongTienGoc < voucher.getGiaTriApDungToiThieu()) {
            String errorMsg = String.format(
                "[Lỗi] Đơn hàng chưa đủ %,.0f ₫ để sử dụng mã này", 
                voucher.getGiaTriApDungToiThieu()
            );
            return new XacNhanPhieuGiamGiaDTO(false, 
                errorMsg, 
                "MINIMUM_AMOUNT_NOT_MET");
        }
        
        // 7. Kiểm tra giá trị giảm không vượt quá tổng tiền
        double discountAmount = Math.min(voucher.getGiaTriGiamGia(), tongTienGoc);
        
        // 8. Nếu hợp lệ, trả về thông tin voucher
        XacNhanPhieuGiamGiaDTO result = new XacNhanPhieuGiamGiaDTO(true, voucher, discountAmount);
        return result;
    }
    
    /**
     * Tính lại thành tiền sau khi áp dụng voucher
     * 
     * @param tongTienGoc Tổng tiền gốc
     * @param discountAmount Số tiền giảm
     * @return Thành tiền cuối cùng
     */
    public double tinhThanhTien(double tongTienGoc, double discountAmount) {
        return Math.max(0, tongTienGoc - discountAmount);
    }
    
    /**
     * Lấy thông tin chi tiết phiếu giảm giá
     * 
     * @param maChuSoPGG Mã phiếu giảm giá
     * @return PhieuGiamGiaDTO hoặc null nếu không tìm thấy
     */
    public PhieuGiamGiaDTO layThongTinVoucher(String maChuSoPGG) {
        if (maChuSoPGG == null || maChuSoPGG.trim().isEmpty()) {
            return null;
        }
        return voucherDAO.layThongTinVoucher(maChuSoPGG.trim().toUpperCase());
    }
}





