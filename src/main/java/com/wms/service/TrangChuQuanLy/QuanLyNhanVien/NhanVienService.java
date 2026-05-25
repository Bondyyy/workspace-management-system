package com.wms.service.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.config.AppConstants;
import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NhanVienDAO;
import com.wms.dao.TrangChuQuanLy.QuanLyVaiTro.VaiTroDAO;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.NhanVienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.ChucNangDTO;
import java.util.List;
import java.util.stream.Collectors;

public class NhanVienService {
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final VaiTroDAO vaiTroDAO = new VaiTroDAO();

    public List<Object[]> layDanhSachNhanVien(String maCN) {
        return nhanVienDAO.layDanhSachNhanVien(maCN);
    }

    public List<Object[]> timKiemNhanVien(String tuKhoa, String maCN) {
        return nhanVienDAO.timKiemNhanVien(tuKhoa != null ? tuKhoa.trim() : null, maCN);
    }

    public boolean themNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) throws Exception {
        validateNhanVien(nv, nd, hoTen);
        if (maVaiTro == null || maVaiTro.isBlank()) throw new Exception("Vui lòng chọn vai trò!");
        if (matKhau == null || matKhau.isBlank()) throw new Exception("Vui lòng nhập mật khẩu tài khoản!");
        return nhanVienDAO.themNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
    }

    public boolean capNhatNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) throws Exception {
        if (nv.getMaNV() == null || nv.getMaND() == null) throw new Exception("Thiếu mã định danh nhân viên!");
        validateNhanVien(nv, nd, hoTen);
        return nhanVienDAO.capNhatNhanVien(nv, nd, hoTen, maVaiTro, matKhau);
    }

    public boolean xoaNhanVien(String maNV, String maND) throws Exception {
        if (maNV == null || maND == null) throw new Exception("Thiếu mã định danh nhân viên!");
        return nhanVienDAO.xoaNhanVien(maNV, maND);
    }

    private void validateNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen) throws Exception {
        if (hoTen == null || hoTen.isBlank()) throw new Exception("Vui lòng nhập họ tên.");
        if (nd.getSdt() == null || nd.getSdt().isBlank()) throw new Exception("Vui lòng nhập số điện thoại.");
        if (!nd.getSdt().matches("\\d{10}")) throw new Exception("Số điện thoại không hợp lệ.");
        if (nd.getEmail() == null || nd.getEmail().isBlank()) throw new Exception("Vui lòng nhập email.");
        if (!nd.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) throw new Exception("Email không hợp lệ.");
        if (nv.getLuongCoBan() == null) throw new Exception("Vui lòng nhập tiền lương.");
        if (nv.getLuongCoBan() != null && nv.getLuongCoBan() < 0) throw new Exception("Lương cơ bản không được âm!");
        if (nv.getMaCN() == null || nv.getMaCN().isBlank()) throw new Exception("Vui lòng chọn chi nhánh!");
    }

    public String generateMaNV() {
        return "";
    }

    public List<String[]> layDanhSachChiNhanh() {
        return nhanVienDAO.layDanhSachChiNhanh();
    }

    public List<VaiTroDTO> layDanhSachVaiTroNhanVien() {
        return vaiTroDAO.layTatCaVaiTro().stream()
                .filter(vt -> !AppConstants.ROLE_CUSTOMER_CODE.equals(vt.getMaVaiTro()))
                .collect(Collectors.toList());
    }

    public List<ChucNangDTO> layTatCaChucNang() {
        return vaiTroDAO.layTatCaChucNang();
    }

    public List<String[]> layChucNangCuaVaiTro(String maVaiTro) {
        return vaiTroDAO.layChucNangCuaVaiTro(maVaiTro);
    }

    public boolean themVaiTro(VaiTroDTO vt, List<String> dsMaCN) throws Exception {
        if (vt.getTenVaiTro() == null || vt.getTenVaiTro().isBlank()) throw new Exception("Tên nhóm quyền trống!");
        return vaiTroDAO.themVaiTro(vt, dsMaCN);
    }

    public boolean capNhatVaiTro(VaiTroDTO vt, List<String> dsMaCN) throws Exception {
        if (vt.getMaVaiTro() == null || vt.getTenVaiTro().isBlank()) throw new Exception("Dữ liệu không hợp lệ!");
        return vaiTroDAO.capNhatVaiTro(vt, dsMaCN);
    }

    public List<VaiTroDTO> layTatCaVaiTro() {
        return vaiTroDAO.layTatCaVaiTro();
    }

    public boolean capNhatChucNangCuaVaiTro(String maVaiTro, List<String> dsMaCN) {
        return vaiTroDAO.capNhatChucNangCuaVaiTro(maVaiTro, dsMaCN);
    }

    public void khoiTaoDuLieuChucNang() {
        vaiTroDAO.khoiTaoDuLieuChucNang();
    }

    public boolean xoaVaiTro(String maVaiTro) throws Exception {
        if (AppConstants.ROLE_CUSTOMER_CODE.equals(maVaiTro) || AppConstants.ROLE_ADMIN_CODE.equals(maVaiTro)) return false;
        return vaiTroDAO.xoaVaiTro(maVaiTro);
    }

    public String layMaCNTuMaND(String maND) {
        return nhanVienDAO.layMaCNTuMaND(maND);
    }
}
