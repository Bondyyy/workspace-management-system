package com.wms.service;

import com.wms.dao.TrangChuQuanLy.QuanLyKho.QuanLyKhoDao;
import com.wms.model.DichVuDTO;

import java.util.List;

public class QuanKhoService {
    
    private QuanLyKhoDao quanLyKhoDao;

    public QuanKhoService() {
        this.quanLyKhoDao = new QuanLyKhoDao();
    }

    public List<DichVuDTO> layDanhSachKho(String keyword) {
        return quanLyKhoDao.layDanhSachKho(keyword);
    }

    public boolean nhapKhoDichVu(String tenNV, String tenLoaiDV, String tenDV, int soLuong, String tenFile, double giaGoc, byte[] fileData) {
        return quanLyKhoDao.nhapKhoDichVu(tenNV, tenLoaiDV, tenDV, soLuong, tenFile, giaGoc, fileData);
    }

    public List<String> layDSNhanVien() { return quanLyKhoDao.layDSNhanVien(); }
    public List<String> layDSLoaiDichVu() { return quanLyKhoDao.layDSLoaiDichVu(); }
    
    public List<String> layDSTenDichVuTheoLoai(String tenLoaiDV) { 
        return quanLyKhoDao.layDSTenDichVuTheoLoai(tenLoaiDV); 
    }

    public double layDonGiaDichVu(String tenDV) {
        return quanLyKhoDao.layDonGiaDichVu(tenDV);
    }
}
