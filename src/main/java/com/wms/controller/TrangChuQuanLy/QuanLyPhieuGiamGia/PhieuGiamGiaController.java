package com.wms.controller.TrangChuQuanLy.QuanLyPhieuGiamGia;

import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;
import com.wms.service.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaService;
import java.util.List;

public class PhieuGiamGiaController {
    private final PhieuGiamGiaService service;

    public PhieuGiamGiaController() {
        this.service = new PhieuGiamGiaService();
    }

    public List<PhieuGiamGiaDTO> layDanhSach() {
        return service.layDanhSach();
    }

    public boolean themMoi(PhieuGiamGiaDTO dto) {
        return service.themMoi(dto);
    }

    public boolean capNhat(PhieuGiamGiaDTO dto) {
        return service.capNhat(dto);
    }

    public boolean xoa(String maPGG) {
        return service.xoa(maPGG);
    }

    public PhieuGiamGiaDTO timTheoMa(String maPGG) {
        return service.timTheoMa(maPGG);
    }

    public List<PhieuGiamGiaDTO> timKiem(String keyword) {
        return service.timKiem(keyword);
    }

    public String sinhMaMoi() {
        return service.sinhMaMoi();
    }
}
