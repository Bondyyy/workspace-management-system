package com.wms.controller.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.NhanVienDTO;
import com.wms.model.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.VaiTroDTO;
import com.wms.model.ChucNangDTO;
import com.wms.service.TrangChuQuanLy.QuanLyNhanVien.NhanVienService;
import java.util.List;

public class QuanLyNhanVienController {

    private final NhanVienService service;

    public QuanLyNhanVienController() {
        this.service = new NhanVienService();
    }

    public List<Object[]> layDanhSachNhanVien() {
        return service.layDanhSachNhanVien();
    }

    public List<Object[]> timKiemNhanVien(String tuKhoa) {
        return service.timKiemNhanVien(tuKhoa);
    }

    public boolean themNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        try {
            return service.themNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean capNhatNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        try {
            return service.capNhatNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean xoaNhanVien(String maNV, String maND) {
        try {
            return service.xoaNhanVien(maNV, maND);
        } catch (Exception e) {
            return false;
        }
    }

    public List<String[]> layDanhSachChiNhanh() {
        return service.layDanhSachChiNhanh();
    }

    public List<VaiTroDTO> layTatCaVaiTro() {
        return service.layTatCaVaiTro();
    }
    
    public List<VaiTroDTO> layDanhSachVaiTroNhanVien() {
        return service.layDanhSachVaiTroNhanVien();
    }

    public List<ChucNangDTO> layTatCaChucNang() {
        return service.layTatCaChucNang();
    }

    public List<String[]> layChucNangCuaVaiTro(String maVaiTro) {
        return service.layChucNangCuaVaiTro(maVaiTro);
    }

    public boolean themVaiTro(VaiTroDTO vt, List<String> dsMaCN) {
        try {
            return service.themVaiTro(vt, dsMaCN);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean capNhatVaiTro(VaiTroDTO vt, List<String> dsMaCN) {
        try {
            return service.capNhatVaiTro(vt, dsMaCN);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean xoaVaiTro(String maVaiTro) {
        return service.xoaVaiTro(maVaiTro);
    }

    public boolean capNhatChucNangCuaVaiTro(String maVaiTro, List<String> dsMaCN) {
        return service.capNhatChucNangCuaVaiTro(maVaiTro, dsMaCN);
    }

    public String generateMaNV() {
        return service.generateMaNV();
    }
}
