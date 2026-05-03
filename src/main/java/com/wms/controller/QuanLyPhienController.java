package com.wms.controller;

import com.wms.dao.PhienLamViecDAO;

import com.wms.model.DichVuTrongPhienDTO;
import com.wms.model.PhienLamViecFullDTO;

import com.wms.view.TrangChuQuanLy.QuanLyPhien.QuanLyPhienForm;
import java.util.List;

public class QuanLyPhienController {
    private QuanLyPhienForm view;
    private PhienLamViecDAO dao;

    public QuanLyPhienController(QuanLyPhienForm view) {
        this.view = view;
        this.dao = new PhienLamViecDAO();
    }

    public void loadDanhSachPhien(String keyword) {
        List<PhienLamViecFullDTO> list = dao.layDanhSachPhien(keyword);
        view.hienThiDanhSachPhien(list);
    }

    public void loadChiTietDichVu(String maPhien) {
        List<DichVuTrongPhienDTO> list = dao.layDichVuCuaPhien(maPhien);
        view.hienThiDichVuTrongPhien(list);
    }

    public boolean ketThucPhien(String maPhien) {
        return dao.ketThucPhien(maPhien);
    }
}





