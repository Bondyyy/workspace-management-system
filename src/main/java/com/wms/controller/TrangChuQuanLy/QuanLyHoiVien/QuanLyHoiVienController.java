package com.wms.controller.TrangChuQuanLy.QuanLyHoiVien;

import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.service.TrangChuQuanLy.QuanLyHoiVien.HoiVienService;
import java.util.List;

public class QuanLyHoiVienController {
    private final HoiVienService service = new HoiVienService();

    public List<HoiVienDTO> layDanhSachHoiVien() {
        return service.layDanhSach();
    }

    public List<HoiVienDTO> timKiemHoiVien(String keyword) {
        return service.timKiem(keyword);
    }

    public void themMoiHoiVien(HoiVienDTO dto) throws Exception {
        service.themMoi(dto);
    }

    public void capNhatHoiVien(HoiVienDTO dto) throws Exception {
        service.capNhat(dto);
    }

    public void xoaHoiVien(String maKH, String maND) throws Exception {
        service.xoa(maKH, maND);
    }

    public String generateMaKH() {
        return service.generateMaKH();
    }
}
