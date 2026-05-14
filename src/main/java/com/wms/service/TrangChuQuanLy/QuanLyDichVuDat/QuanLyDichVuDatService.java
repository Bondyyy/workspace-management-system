package com.wms.service.TrangChuQuanLy.QuanLyDichVuDat;

import com.wms.dao.TrangChuQuanLy.QuanLyDichVuDat.ChiTietDichVuDAO;
import com.wms.model.TrangChuQuanLy.QuanLyDichVuDat.DichVuDaDatDTO;

import java.util.List;

public class QuanLyDichVuDatService {

    private final ChiTietDichVuDAO dao = new ChiTietDichVuDAO();

    public List<DichVuDaDatDTO> timKiem(String keyword) {
        return dao.layDanhSachDichVuTheoPhien(keyword == null || keyword.trim().isEmpty() ? null : keyword.trim());
    }

    public List<Object[]> layDanhSachPhienHoatDong(String keyword) {
        return dao.layDanhSachPhienHoatDong(keyword);
    }

    public List<DichVuDaDatDTO> layDSDichVuTheoPhien(String maPhien) {
        return dao.layDanhSachDichVuTheoPhien(maPhien);
    }

    public String themDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        if (!dao.kiemTraPhienTonTai(maPhien)) {
            return "Mã phiên không tồn tại trong hệ thống!";
        }
        boolean ok = dao.themDichVuMoi(maPhien, tenDV, soLuong, ghiChu);
        return ok ? null : "Thêm dịch vụ thất bại. Vui lòng kiểm tra lại thông tin.";
    }

    public List<String> layDanhSachLoaiDichVu() {
        return dao.getDanhSachLoaiDichVu();
    }

    public List<String> layDanhSachTenDichVu(String tenLoaiDV) {
        return dao.getDanhSachTenDichVu(tenLoaiDV);
    }
    public String xoaDichVu(String maPhien, String tenDV) {
        boolean ok = dao.xoaDichVu(maPhien, tenDV);
        return ok ? null : "Xóa dịch vụ thất bại!";
    }

    public String capNhatDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        boolean ok = dao.capNhatDichVu(maPhien, tenDV, soLuong, ghiChu);
        return ok ? null : "Cập nhật dịch vụ thất bại!";
    }
}
