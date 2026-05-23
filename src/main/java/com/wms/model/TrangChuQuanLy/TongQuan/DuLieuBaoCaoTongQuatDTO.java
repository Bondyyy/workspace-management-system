package com.wms.model.TrangChuQuanLy.TongQuan;

import java.util.ArrayList;
import java.util.List;

public class DuLieuBaoCaoTongQuatDTO {

    private String loaiBaoCao;
    private String tieuDeBaoCao;
    private String phuDeBaoCao;
    private String tuNgay;
    private String denNgay;
    private String tenChiNhanh;
    private String nguoiXuat;
    private String thoiGianXuat;
    private String tongGiaTri1;
    private String nhanTongGiaTri1;
    private String tongGiaTri2;
    private String nhanTongGiaTri2;
    private String tongGiaTri3;
    private String nhanTongGiaTri3;
    private String ghiChuBaoCao;
    private List<DongBaoCaoTongQuatDTO> danhSachDongBaoCao = new ArrayList<>();
    private List<String> danhSachTieuDeCot = new ArrayList<>();

    public String getLoaiBaoCao() {
        return loaiBaoCao;
    }

    public void setLoaiBaoCao(String loaiBaoCao) {
        this.loaiBaoCao = loaiBaoCao;
    }

    public String getTieuDeBaoCao() {
        return tieuDeBaoCao;
    }

    public void setTieuDeBaoCao(String tieuDeBaoCao) {
        this.tieuDeBaoCao = tieuDeBaoCao;
    }

    public String getPhuDeBaoCao() {
        return phuDeBaoCao;
    }

    public void setPhuDeBaoCao(String phuDeBaoCao) {
        this.phuDeBaoCao = phuDeBaoCao;
    }

    public String getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(String tuNgay) {
        this.tuNgay = tuNgay;
    }

    public String getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(String denNgay) {
        this.denNgay = denNgay;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getNguoiXuat() {
        return nguoiXuat;
    }

    public void setNguoiXuat(String nguoiXuat) {
        this.nguoiXuat = nguoiXuat;
    }

    public String getThoiGianXuat() {
        return thoiGianXuat;
    }

    public void setThoiGianXuat(String thoiGianXuat) {
        this.thoiGianXuat = thoiGianXuat;
    }

    public String getTongGiaTri1() {
        return tongGiaTri1;
    }

    public void setTongGiaTri1(String tongGiaTri1) {
        this.tongGiaTri1 = tongGiaTri1;
    }

    public String getNhanTongGiaTri1() {
        return nhanTongGiaTri1;
    }

    public void setNhanTongGiaTri1(String nhanTongGiaTri1) {
        this.nhanTongGiaTri1 = nhanTongGiaTri1;
    }

    public String getTongGiaTri2() {
        return tongGiaTri2;
    }

    public void setTongGiaTri2(String tongGiaTri2) {
        this.tongGiaTri2 = tongGiaTri2;
    }

    public String getNhanTongGiaTri2() {
        return nhanTongGiaTri2;
    }

    public void setNhanTongGiaTri2(String nhanTongGiaTri2) {
        this.nhanTongGiaTri2 = nhanTongGiaTri2;
    }

    public String getTongGiaTri3() {
        return tongGiaTri3;
    }

    public void setTongGiaTri3(String tongGiaTri3) {
        this.tongGiaTri3 = tongGiaTri3;
    }

    public String getNhanTongGiaTri3() {
        return nhanTongGiaTri3;
    }

    public void setNhanTongGiaTri3(String nhanTongGiaTri3) {
        this.nhanTongGiaTri3 = nhanTongGiaTri3;
    }

    public String getGhiChuBaoCao() {
        return ghiChuBaoCao;
    }

    public void setGhiChuBaoCao(String ghiChuBaoCao) {
        this.ghiChuBaoCao = ghiChuBaoCao;
    }

    public List<DongBaoCaoTongQuatDTO> getDanhSachDongBaoCao() {
        return danhSachDongBaoCao;
    }

    public void setDanhSachDongBaoCao(List<DongBaoCaoTongQuatDTO> danhSachDongBaoCao) {
        this.danhSachDongBaoCao = danhSachDongBaoCao == null ? new ArrayList<>() : danhSachDongBaoCao;
    }

    public List<String> getDanhSachTieuDeCot() {
        return danhSachTieuDeCot;
    }

    public void setDanhSachTieuDeCot(List<String> danhSachTieuDeCot) {
        this.danhSachTieuDeCot = danhSachTieuDeCot == null ? new ArrayList<>() : danhSachTieuDeCot;
    }
}
