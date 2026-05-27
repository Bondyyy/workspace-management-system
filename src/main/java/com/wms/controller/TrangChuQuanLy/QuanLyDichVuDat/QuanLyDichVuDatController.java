package com.wms.controller.TrangChuQuanLy.QuanLyDichVuDat;

import com.wms.model.TrangChuQuanLy.QuanLyDichVuDat.DichVuDaDatDTO;
import com.wms.service.TrangChuQuanLy.QuanLyDichVuDat.QuanLyDichVuDatService;

import java.util.List;

public class QuanLyDichVuDatController {

    private final QuanLyDichVuDatService service = new QuanLyDichVuDatService();

    public List<DichVuDaDatDTO> timKiem(String keyword) {
        return service.timKiem(keyword);
    }

    public List<Object[]> layDanhSachPhienHoatDong(String keyword) {
        return service.layDanhSachPhienHoatDong(keyword);
    }

    public List<Object[]> layDanhSachPhien(String keyword, boolean daKetThuc) {
        return service.layDanhSachPhien(keyword, daKetThuc);
    }

    public List<DichVuDaDatDTO> layDSDichVuTheoPhien(String maPhien) {
        return service.layDSDichVuTheoPhien(maPhien);
    }

    public String themDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        return service.themDichVu(maPhien, tenDV, soLuong, ghiChu);
    }

    public List<String> layDanhSachLoaiDichVu() {
        return service.layDanhSachLoaiDichVu();
    }

    public List<String> layDanhSachTenDichVu(String tenLoaiDV) {
        return service.layDanhSachTenDichVu(tenLoaiDV);
    }
    public String xoaDichVu(String maPhien, String tenDV) {
        return service.xoaDichVu(maPhien, tenDV);
    }

    public String capNhatDichVu(String maPhien, String tenDV, int soLuong, String ghiChu) {
        return service.capNhatDichVu(maPhien, tenDV, soLuong, ghiChu);
    }
}
