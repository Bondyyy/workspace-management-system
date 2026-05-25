package com.wms.controller.TrangChuQuanLy.QuanLyKho;

import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;
import com.wms.service.TrangChuQuanLy.QuanLyKho.QuanKhoService;
import com.wms.view.TrangChuQuanLy.QuanLyKho.QuanLyKhoForm;

import java.util.List;

public class QuanLyKhoController {

    private QuanLyKhoForm view;
    private QuanKhoService quanKhoService;

    public QuanLyKhoController(QuanLyKhoForm view) {
        this.view = view;
        this.quanKhoService = new QuanKhoService();
    }

    public void loadData(String keyword) {
        try {
            List<DichVuDTO> danhSach = quanKhoService.layDanhSachKho(keyword);
            view.hienThiDuLieu(danhSach);
        } catch (Exception e) {
            e.printStackTrace();
            view.hienThiThongBaoLoi(com.wms.util.ErrorMessageUtil.toUserMessage(e));
        }
    }

    public boolean nhapKho(String maDV, String tenNV, String tenLoaiDV, String tenDV, int soLuong, String tenFile, double giaNhap, byte[] fileData) {
        return quanKhoService.nhapKhoDichVu(maDV, tenNV, tenLoaiDV, tenDV, soLuong, tenFile, giaNhap, fileData);
    }

    public List<String> getDSNhanVien() { return quanKhoService.layDSNhanVien(); }
    public List<String> getDSLoaiDichVu() { return quanKhoService.layDSLoaiDichVu(); }
    
    public List<String> getDSTenDichVuTheoLoai(String tenLoaiDV) { 
        return quanKhoService.layDSTenDichVuTheoLoai(tenLoaiDV); 
    }

    public double layDonGiaDichVu(String tenDV) {
        return quanKhoService.layDonGiaDichVu(tenDV);
    }

    /**
     * Lấy dữ liệu hóa đơn mới nhất theo MaDV.
     * Trả về Object[]{byte[] fileData, String tenFile} hoặc null nếu không có.
     */
    public Object[] xemHoaDon(String maDV) {
        return quanKhoService.layHoaDonMoiNhat(maDV);
    }
}
