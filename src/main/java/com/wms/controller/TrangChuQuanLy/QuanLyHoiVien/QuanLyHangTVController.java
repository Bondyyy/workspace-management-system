package com.wms.controller.TrangChuQuanLy.QuanLyHoiVien;

import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HangThanhVienDTO;
import com.wms.service.TrangChuQuanLy.QuanLyHoiVien.HangTVService;
import java.util.List;

public class QuanLyHangTVController {
    private final HangTVService service = new HangTVService();

    public List<HangThanhVienDTO> layDanhSachHang() {
        return service.layDanhSachHang();
    }

    public void capNhatChinhSachGiamGia(String maHang, double discount) throws Exception {
        service.capNhatChinhSachGiamGia(maHang, discount);
    }
}
