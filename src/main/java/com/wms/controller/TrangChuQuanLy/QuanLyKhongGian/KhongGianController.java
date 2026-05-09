package com.wms.controller.TrangChuQuanLy.QuanLyKhongGian;

import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.KhongGianDTO;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.LoaiKhongGianDTO;
import com.wms.service.TrangChuQuanLy.QuanLyKhongGian.KhongGianService;

import java.time.LocalTime;
import java.util.List;

public class KhongGianController {

    private final KhongGianService service = new KhongGianService();

    public List<KhongGianDTO> timKiemKhongGian(String tuKhoa, String maCN, String maLoaiKG) {
        return service.timKiemKhongGian(tuKhoa, maCN, maLoaiKG);
    }

    public boolean themKhongGian(KhongGianDTO dto) {
        return service.themKhongGian(dto);
    }

    public boolean capNhatKhongGian(KhongGianDTO dto) {
        return service.capNhatKhongGian(dto);
    }

    public KhongGianDTO layKhongGianTheoMa(String maKG) {
        return service.layKhongGianTheoMa(maKG);
    }

    public String sinhMaKG() {
        return service.sinhMaKG();
    }

    public List<LoaiKhongGianDTO> layTatCaLoai() {
        return service.layTatCaLoai();
    }

    public List<LoaiKhongGianDTO> timKiemLoai(String tuKhoa) {
        return service.timKiemLoai(tuKhoa);
    }

    public boolean themLoai(LoaiKhongGianDTO dto) {
        return service.themLoai(dto);
    }

    public boolean capNhatLoai(LoaiKhongGianDTO dto) {
        return service.capNhatLoai(dto);
    }

    public String sinhMaLoaiKG() {
        return service.sinhMaLoaiKG();
    }

    public List<ChiNhanhDTO> layDanhSachChiNhanh() {
        return service.layDanhSachChiNhanh();
    }

    public List<String> taiDanhSachChiNhanhTen() {
        return service.layTenChiNhanhHoatDong();
    }

    public List<String> taiDanhSachLoaiKhongGianTen() {
        return service.layTenLoaiKhongGian();
    }

    public List<KhongGianDTO> layTheoChiNhanh(String maCN) {
        return service.layTheoChiNhanh(maCN);
    }

    public boolean checkChoTrong(String loai, String ngay, String gio) {
        return service.kiemTraTinhTrang(loai, ngay, gio);
    }

    public LocalTime layGioDongCua(String tenCN) {
        return service.layGioDongCua(tenCN);
    }

    public String luuToaDo(List<KhongGianDTO> danhSach) {
        return service.luuToaDo(danhSach);
    }
}
