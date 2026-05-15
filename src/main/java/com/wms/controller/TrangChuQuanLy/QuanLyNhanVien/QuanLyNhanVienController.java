package com.wms.controller.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.NhanVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.ChucNangDTO;
import com.wms.service.TrangChuQuanLy.QuanLyNhanVien.NhanVienService;
import java.util.List;

public class QuanLyNhanVienController {

    private final NhanVienService service;

    public QuanLyNhanVienController() {
        this.service = new NhanVienService();
    }

    public List<Object[]> layDanhSachNhanVien(String maCN) {
        return service.layDanhSachNhanVien(maCN);
    }

    public List<Object[]> timKiemNhanVien(String tuKhoa, String maCN) {
        return service.timKiemNhanVien(tuKhoa, maCN);
    }

    public boolean themNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) throws Exception {
        return service.themNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
    }

    public boolean capNhatNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) throws Exception {
        return service.capNhatNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
    }

    public boolean xoaNhanVien(String maNV, String maND) throws Exception {
        return service.xoaNhanVien(maNV, maND);
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

    public boolean themVaiTro(VaiTroDTO vt, List<String> dsMaCN) throws Exception {
        return service.themVaiTro(vt, dsMaCN);
    }

    public boolean capNhatVaiTro(VaiTroDTO vt, List<String> dsMaCN) throws Exception {
        return service.capNhatVaiTro(vt, dsMaCN);
    }

    public boolean xoaVaiTro(String maVaiTro) {
        return service.xoaVaiTro(maVaiTro);
    }

    public boolean capNhatChucNangCuaVaiTro(String maVaiTro, List<String> dsMaCN) {
        return service.capNhatChucNangCuaVaiTro(maVaiTro, dsMaCN);
    }

    public String layMaCNTuMaND(String maND) {
        return service.layMaCNTuMaND(maND);
    }

    public String generateMaNV() {
        return service.generateMaNV();
    }
}
