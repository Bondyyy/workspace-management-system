package com.wms.service.TrangChuQuanLy.QuanLyDichVuDat;

import com.wms.dao.TrangChuQuanLy.QuanLyDichVuDat.ChiTietDichVuDAO;
import com.wms.model.TrangChuQuanLy.QuanLyDichVuDat.DichVuDaDatDTO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class QuanLyDichVuDatService {

    private final ChiTietDichVuDAO dao = new ChiTietDichVuDAO();

    public List<DichVuDaDatDTO> timKiem(String keyword) {
        return Collections.emptyList();
    }

    public List<Object[]> layDanhSachPhienHoatDong(String keyword) {
        return dao.layDanhSachPhienHoatDong(keyword);
    }

    public List<Object[]> layDanhSachPhien(String keyword, boolean daKetThuc) {
        return dao.layDanhSachPhien(keyword, daKetThuc);
    }

    public List<DichVuDaDatDTO> layDSDichVuTheoPhien(String maPhien) {
        return dao.layDanhSachDichVuTheoPhien(maPhien);
    }

    public String themDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        String loi = validateDichVu(maPhien, tenDV, soLuong);
        if (loi != null) {
            return loi;
        }
        try {
            dao.themDichVuMoi(maPhien.trim(), tenDV.trim(), soLuong, ghiChu);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    public List<String> layDanhSachLoaiDichVu() {
        return dao.getDanhSachLoaiDichVu();
    }

    public List<String> layDanhSachTenDichVu(String tenLoaiDV) {
        return dao.getDanhSachTenDichVu(tenLoaiDV);
    }
    public String xoaDichVu(String maPhien, String tenDV) {
        if (isBlank(maPhien)) {
            return "Vui lòng chọn một phiên làm việc.";
        }
        if (isBlank(tenDV) || tenDV.contains("---")) {
            return "Vui lòng chọn dịch vụ cần xóa.";
        }
        try {
            dao.xoaDichVu(maPhien.trim(), tenDV.trim());
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    public String capNhatDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        String loi = validateDichVu(maPhien, tenDV, soLuong);
        if (loi != null) {
            return loi;
        }
        try {
            dao.capNhatDichVu(maPhien.trim(), tenDV.trim(), soLuong, ghiChu);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    private String validateDichVu(String maPhien, String tenDV, int soLuong) {
        if (isBlank(maPhien)) {
            return "Vui lòng chọn một phiên làm việc.";
        }
        if (isBlank(tenDV) || tenDV.contains("---")) {
            return "Vui lòng chọn dịch vụ.";
        }
        if (soLuong <= 0) {
            return "Số lượng phải lớn hơn 0.";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
