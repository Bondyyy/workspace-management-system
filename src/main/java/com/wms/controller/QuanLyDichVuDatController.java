package com.wms.controller;

import com.wms.dao.DichVuDAO;

import com.wms.dao.ChiTietDichVuDAO;
import com.wms.model.DichVuDaDatDTO;

import com.wms.view.TrangChuQuanLy.QuanLyDichVuDat.QuanLyDichVuDatForm;

import java.util.List;

public class QuanLyDichVuDatController {
    
    private QuanLyDichVuDatForm view;
    private ChiTietDichVuDAO dao;

    public QuanLyDichVuDatController(QuanLyDichVuDatForm view) {
        this.view = view;
        this.dao = new ChiTietDichVuDAO();
    }

    public void loadData(String keyword) {
        try {
            List<DichVuDaDatDTO> danhSach = dao.layDanhSachDichVuDat(keyword);
            view.hienThiDuLieu(danhSach);
        } catch (Exception e) {
            e.printStackTrace();
            view.hienThiThongBaoLoi("Lỗi khi tải dữ liệu từ CSDL: " + e.getMessage());
        }
    }

    public boolean themDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        if (!dao.kiemTraPhienTonTai(maPhien)) {
            view.hienThiThongBaoLoi("Mã phiên không tồn tại trong hệ thống!");
            return false;
        }
        return dao.themDichVuMoi(maPhien, tenDV, soLuong, ghiChu);
    }

    public List<String> getDanhSachLoaiDichVu() {
        return dao.getDanhSachLoaiDichVu();
    }

    public List<String> getDanhSachTenDichVu(String tenLoaiDV) {
        return dao.getDanhSachTenDichVu(tenLoaiDV);
    }
}





