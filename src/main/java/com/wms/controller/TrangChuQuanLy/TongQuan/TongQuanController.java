package com.wms.controller.TrangChuQuanLy.TongQuan;

import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.TongQuanDTO;
import com.wms.service.TrangChuQuanLy.TongQuan.TongQuanService;

import java.util.List;

public class TongQuanController {

    private final TongQuanService service = new TongQuanService();

    public TongQuanDTO layDuLieu(String tuNgay, String denNgay, String chiNhanh, String loaiDT) {
        return service.layDuLieuTongQuan(tuNgay, denNgay, chiNhanh, loaiDT);
    }

    public List<ChiNhanhDTO> layDanhSachChiNhanh() {
        return service.layDanhSachChiNhanh();
    }

    public List<Object[]> layDanhSachHoaDonTheoDieuKien(String tuNgay, String denNgay, String chiNhanh, String loaiDT) {
        return service.layDanhSachHoaDonTheoDieuKien(tuNgay, denNgay, chiNhanh, loaiDT);
    }
}
