package com.wms.controller.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.HangThanhVienDTO;
import com.wms.service.TrangChuQuanLy.QuanLyNhanVien.HangTVService;
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
