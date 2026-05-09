package com.wms.controller;

import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TongQuanDTO;
import com.wms.service.TongQuanService;

import java.util.List;

public class TongQuanController {

    private final TongQuanService service = new TongQuanService();

    public TongQuanDTO layDuLieu(String tuNgay, String denNgay, String chiNhanh, String loaiDT) {
        return service.layDuLieuTongQuan(tuNgay, denNgay, chiNhanh, loaiDT);
    }

    public List<ChiNhanhDTO> layDanhSachChiNhanh() {
        return service.layDanhSachChiNhanh();
    }
}
