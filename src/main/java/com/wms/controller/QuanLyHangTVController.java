package com.wms.controller;

import com.wms.dao.HangThanhVienDAO;
import com.wms.model.HangThanhVienDTO;

import java.util.List;

public class QuanLyHangTVController {

    private final HangThanhVienDAO hangThanhVienDAO;

    public QuanLyHangTVController() {
        this.hangThanhVienDAO = new HangThanhVienDAO();
    }

    public List<HangThanhVienDTO> layDanhSachHang() {
        return hangThanhVienDAO.getAll();
    }

    public List<HangThanhVienDTO> timKiemHang(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return hangThanhVienDAO.getAll();
        }
        return hangThanhVienDAO.search(keyword.trim());
    }

    public void themHang(HangThanhVienDTO dto) throws Exception {
        if (dto.getMaHangThanhVien() == null || dto.getMaHangThanhVien().trim().isEmpty()) {
            throw new Exception("Mã hạng không được để trống!");
        }
        if (dto.getTenHangThanhVien() == null || dto.getTenHangThanhVien().trim().isEmpty()) {
            throw new Exception("Tên hạng không được để trống!");
        }
        hangThanhVienDAO.insert(dto);
    }

    public void capNhatHang(HangThanhVienDTO dto) throws Exception {
        if (dto.getMaHangThanhVien() == null || dto.getMaHangThanhVien().trim().isEmpty()) {
            throw new Exception("Lỗi hệ thống: Mã hạng bị trống!");
        }
        hangThanhVienDAO.update(dto);
    }

    public void xoaHang(String maHang) throws Exception {
        if (maHang == null || maHang.trim().isEmpty()) {
            throw new Exception("Lỗi hệ thống: Mã hạng bị trống!");
        }
        hangThanhVienDAO.delete(maHang);
    }
}





