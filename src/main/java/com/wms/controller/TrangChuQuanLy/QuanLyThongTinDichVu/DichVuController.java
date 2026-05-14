package com.wms.controller.TrangChuQuanLy.QuanLyThongTinDichVu;

import com.wms.model.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuDTO;
import com.wms.model.TrangChuQuanLy.QuanLyLoaiDichVu.LoaiDichVuDTO;
import com.wms.service.TrangChuQuanLy.QuanLyThongTinDichVu.DichVuService;
import java.util.List;

public class DichVuController {
    private final DichVuService service;

    public DichVuController() {
        this.service = new DichVuService();
    }

    public List<DichVuDTO> layDanhSach(String maLoai, String trangThai, String tuKhoa) {
        return service.layDanhSachDichVu(maLoai, trangThai, tuKhoa);
    }

    public List<LoaiDichVuDTO> layDanhSachLoai() {
        return service.layTatCaLoai();
    }

    public boolean themMoi(DichVuDTO dv) {
        try {
            return service.themDichVu(dv);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean capNhat(DichVuDTO dv) {
        try {
            return service.capNhatDichVu(dv);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean themLoai(LoaiDichVuDTO l) {
        try {
            return service.themLoaiDichVu(l);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean capNhatLoai(LoaiDichVuDTO l) {
        try {
            return service.capNhatLoaiDichVu(l);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateMaDV() {
        return service.generateMaDV();
    }

    public String generateMaLoai() {
        return service.generateMaLoai();
    }
}
