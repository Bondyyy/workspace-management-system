package com.wms.controller;

import com.wms.dao.KhachHangDAO;
import com.wms.model.NhanSu_KhachHang.HoiVienDTO;
import java.util.List;

public class QuanLyHoiVienController {

    private final KhachHangDAO khachHangDAO;

    public QuanLyHoiVienController() {
        this.khachHangDAO = new KhachHangDAO();
    }

    public List<HoiVienDTO> layDanhSachHoiVien() {
        return khachHangDAO.getAll();
    }

    public List<HoiVienDTO> timKiemHoiVien(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return khachHangDAO.getAll();
        }
        return khachHangDAO.search(keyword.trim());
    }

    public void themMoiHoiVien(HoiVienDTO dto) throws Exception {
        if (dto.getHoTen() == null || dto.getHoTen().trim().isEmpty()) {
            throw new Exception("Họ tên không được để trống!");
        }
        khachHangDAO.insert(dto);
    }

    public void capNhatHoiVien(HoiVienDTO dto) throws Exception {
        if (dto.getMaKH() == null || dto.getMaND() == null) {
            throw new Exception("Lỗi hệ thống: Tham chiếu ID bị trống!");
        }
        khachHangDAO.update(dto);
    }

    public void xoaHoiVien(String maKH, String maND) throws Exception {
        if (maKH == null || maND == null) {
            throw new Exception("Lỗi hệ thống: Tham chiếu ID bị trống!");
        }
        khachHangDAO.delete(maKH, maND);
    }
}
