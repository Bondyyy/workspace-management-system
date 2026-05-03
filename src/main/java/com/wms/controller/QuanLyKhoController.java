package com.wms.controller;

import com.wms.model.DichVuDTO;
import com.wms.service.QuanKhoService;
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
            view.hienThiThongBaoLoi("Lỗi khi tải dữ liệu từ CSDL: " + e.getMessage());
        }
    }

    public boolean nhapKho(String tenNV, String tenLoaiDV, String tenDV, int soLuong, String tenFile) {
        return quanKhoService.nhapKhoDichVu(tenNV, tenLoaiDV, tenDV, soLuong, tenFile);
    }

    public List<String> getDSNhanVien() { return quanKhoService.layDSNhanVien(); }
    public List<String> getDSLoaiDichVu() { return quanKhoService.layDSLoaiDichVu(); }
    
    public List<String> getDSTenDichVuTheoLoai(String tenLoaiDV) { 
        return quanKhoService.layDSTenDichVuTheoLoai(tenLoaiDV); 
    }
}