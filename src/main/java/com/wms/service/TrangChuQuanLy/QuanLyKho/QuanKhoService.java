package com.wms.service.TrangChuQuanLy.QuanLyKho;

import com.wms.dao.TrangChuQuanLy.QuanLyKho.QuanLyKhoDao;
import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;

import java.util.List;

public class QuanKhoService {
    
    private QuanLyKhoDao quanLyKhoDao;

    public QuanKhoService() {
        this.quanLyKhoDao = new QuanLyKhoDao();
    }

    public List<DichVuDTO> layDanhSachKho(String keyword) {
        return quanLyKhoDao.layDanhSachKho(keyword);
    }

    public boolean nhapKhoDichVu(String maDV, String tenNV, String tenLoaiDV, String tenDV, int soLuong, String tenFile, double giaNhap, byte[] fileData) {
        return quanLyKhoDao.nhapKhoDichVu(maDV, tenNV, tenLoaiDV, tenDV, soLuong, tenFile, giaNhap, fileData);
    }

    public List<String> layDSNhanVien() { return quanLyKhoDao.layDSNhanVien(); }
    public List<String> layDSLoaiDichVu() { return quanLyKhoDao.layDSLoaiDichVu(); }
    
    public List<String> layDSTenDichVuTheoLoai(String tenLoaiDV) { 
        return quanLyKhoDao.layDSTenDichVuTheoLoai(tenLoaiDV); 
    }

    public double layDonGiaDichVu(String tenDV) {
        return quanLyKhoDao.layDonGiaDichVu(tenDV);
    }

    public Object[] layHoaDonMoiNhat(String maDV) {
        return quanLyKhoDao.layHoaDonMoiNhat(maDV);
    }
}




