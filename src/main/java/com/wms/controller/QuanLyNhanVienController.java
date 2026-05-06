package com.wms.controller;

import com.wms.dao.NhanVienDAO;
import com.wms.model.NhanVienDTO;
import com.wms.model.NguoiDungDTO;
import java.util.List;

public class QuanLyNhanVienController {

    private final NhanVienDAO nhanVienDAO;

    public QuanLyNhanVienController() {
        this.nhanVienDAO = new NhanVienDAO();
    }

    public List<Object[]> layDanhSachNhanVien() {
        return nhanVienDAO.layDanhSachNhanVien();
    }

    public List<Object[]> timKiemNhanVien(String tuKhoa) {
        return nhanVienDAO.timKiemNhanVien(tuKhoa);
    }

    public boolean themNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        return nhanVienDAO.themNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
    }

    public boolean capNhatNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        return nhanVienDAO.capNhatNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
    }

    public boolean xoaNhanVien(String maNV, String maND) {
        return nhanVienDAO.xoaNhanVien(maNV, maND);
    }

    public List<String[]> layDanhSachChiNhanh() {
        return nhanVienDAO.layDanhSachChiNhanh();
    }

    public List<String[]> layDanhSachVaiTro() {
        return nhanVienDAO.layDanhSachVaiTro();
    }
}
