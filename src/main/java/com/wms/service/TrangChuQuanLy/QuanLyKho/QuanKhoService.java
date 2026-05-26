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
        if (tenNV == null || tenNV.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên.");
        }
        if (tenLoaiDV == null || tenLoaiDV.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn loại dịch vụ.");
        }
        if (tenDV == null || tenDV.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ.");
        }
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        }
        if (giaNhap < 0) {
            throw new IllegalArgumentException("Giá nhập không được âm.");
        }
        if ((maDV == null || maDV.isBlank()) && quanLyKhoDao.tonTaiTenDichVu(tenDV)) {
            throw new IllegalArgumentException("Tên dịch vụ đã tồn tại. Vui lòng nhập tên dịch vụ mới hoặc chọn dòng dịch vụ trong bảng để nhập thêm tồn kho.");
        }
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




