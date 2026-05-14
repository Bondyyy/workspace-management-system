package com.wms.model.TrangChuQuanLy.QuanLyNguoiDung;

import java.util.List;

public class NguoiDungDTO {
    private String maND;
    private String tenTaiKhoan;
    private String matKhauMaHoa;
    private byte[] anhDaiDien;
    private String gioiTinh;
    private String email;
    private String sdt;
    private java.sql.Date ngaySinh;
    private java.sql.Timestamp thoiGianTao;
    private java.sql.Timestamp capNhatLanCuoi;
    private java.sql.Timestamp lanCuoiDangNhap;
    private String hoTen;
    private String trangThaiND;
    private List<String> vaiTro;
    private List<String> chucNang; // Danh sách MaChucNang được phép truy cập
    private String maNV;

    public NguoiDungDTO() {}

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getMaND() {
        return maND;
    }

    public void setMaND(String maND) {
        this.maND = maND;
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public void setTenTaiKhoan(String tenTaiKhoan) {
        this.tenTaiKhoan = tenTaiKhoan;
    }

    public String getMatKhauMaHoa() {
        return matKhauMaHoa;
    }

    public void setMatKhauMaHoa(String matKhauMaHoa) {
        this.matKhauMaHoa = matKhauMaHoa;
    }

    public byte[] getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(byte[] anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public java.sql.Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(java.sql.Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public java.sql.Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(java.sql.Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public java.sql.Timestamp getCapNhatLanCuoi() {
        return capNhatLanCuoi;
    }

    public void setCapNhatLanCuoi(java.sql.Timestamp capNhatLanCuoi) {
        this.capNhatLanCuoi = capNhatLanCuoi;
    }

    public java.sql.Timestamp getLanCuoiDangNhap() {
        return lanCuoiDangNhap;
    }

    public void setLanCuoiDangNhap(java.sql.Timestamp lanCuoiDangNhap) {
        this.lanCuoiDangNhap = lanCuoiDangNhap;
    }

    public String getTrangThaiND() {
        return trangThaiND;
    }

    public void setTrangThaiND(String trangThaiND) {
        this.trangThaiND = trangThaiND;
    }

    public List<String> getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(List<String> vaiTro) {
        this.vaiTro = vaiTro;
    }

    public boolean hasRole(String roleName) {
        if (vaiTro == null) return false;
        for (String r : vaiTro) {
            if (r != null && r.trim().equalsIgnoreCase(roleName.trim())) return true;
        }
        return false;
    }

    public List<String> getChucNang() {
        return chucNang;
    }

    public void setChucNang(List<String> chucNang) {
        this.chucNang = chucNang;
    }

    /**
     * Kiểm tra người dùng có quyền truy cập chức năng theo MaChucNang không.
     * Nếu danh sách chucNang null/rỗng thì coi là chưa cấu hình → trả về false.
     */
    public boolean hasChucNang(String maChucNang) {
        if (chucNang == null || chucNang.isEmpty()) return false;
        for (String cn : chucNang) {
            if (cn != null && cn.trim().equalsIgnoreCase(maChucNang.trim())) return true;
        }
        return false;
    }

    /**
     * Kiểm tra xem người dùng có bất kỳ chức năng nào (tức là đã được phân quyền).
     */
    public boolean daPhanQuyen() {
        return chucNang != null && !chucNang.isEmpty();
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }
}
