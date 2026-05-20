package com.wms.controller.TrangChuQuanLy.QuanLyHangThanhVien;

import com.wms.model.TrangChuQuanLy.QuanLyHangThanhVien.HangThanhVienDTO;
import com.wms.service.TrangChuQuanLy.QuanLyHangThanhVien.HangTVService;
import java.util.List;

public class QuanLyHangTVController {
    private final HangTVService service = new HangTVService();

    public List<HangThanhVienDTO> layDanhSachHang() {
        return service.layDanhSachHang();
    }

    public void capNhatChinhSachGiamGia(String maHang, double discount) throws Exception {
        service.capNhatChinhSachGiamGia(maHang, discount);
    }

    public void capNhatHangThanhVien(HangThanhVienDTO dto) throws Exception {
        service.capNhatHangThanhVien(dto);
    }
}
