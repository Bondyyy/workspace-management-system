package com.wms.model;

import java.sql.Timestamp;
import java.sql.Date; 
import java.util.List;

public class NguoiDungDTO {
    private String maND;
    private String tenTaiKhoan;
    private String matKhauMaHoa;
    private String anhDaiDien;
    private String gioiTinh;
    private String email;
    private String sdt;
    private Date ngaySinh;
    private Timestamp thoiGianTao;
    private Timestamp capNhatLanCuoi;
    private Timestamp lanCuoiDangNhap;
    private String trangThaiND;
    
    private List<String> vaiTro;

    public NguoiDungDTO() {}

    public String getMaND() { return maND; }
    public void setMaND(String maND) { this.maND = maND; }

    public String getTenTaiKhoan() { return tenTaiKhoan; }
    public void setTenTaiKhoan(String tenTaiKhoan) { this.tenTaiKhoan = tenTaiKhoan; }

    public String getMatKhauMaHoa() { return matKhauMaHoa; }
    public void setMatKhauMaHoa(String matKhauMaHoa) { this.matKhauMaHoa = matKhauMaHoa; }

    public String getAnhDaiDien() { return anhDaiDien; }
    public void setAnhDaiDien(String anhDaiDien) { this.anhDaiDien = anhDaiDien; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public Timestamp getCapNhatLanCuoi() { return capNhatLanCuoi; }
    public void setCapNhatLanCuoi(Timestamp capNhatLanCuoi) { this.capNhatLanCuoi = capNhatLanCuoi; }

    public Timestamp getLanCuoiDangNhap() { return lanCuoiDangNhap; }
    public void setLanCuoiDangNhap(Timestamp lanCuoiDangNhap) { this.lanCuoiDangNhap = lanCuoiDangNhap; }

    public String getTrangThaiND() { return trangThaiND; }
    public void setTrangThaiND(String trangThaiND) { this.trangThaiND = trangThaiND; }

    public List<String> getVaiTro() { return vaiTro; }
    public void setVaiTro(List<String> vaiTro) { this.vaiTro = vaiTro; }

    // Kiểm tra có vai trò nhất định không
    public boolean hasRole(String roleName) {
        return vaiTro != null && vaiTro.contains(roleName);
    }
}