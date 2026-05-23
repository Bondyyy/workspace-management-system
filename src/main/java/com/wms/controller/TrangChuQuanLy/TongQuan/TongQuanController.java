package com.wms.controller.TrangChuQuanLy.TongQuan;

import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DoanhThuReportRowDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.TongQuanDTO;
import com.wms.service.TrangChuQuanLy.TongQuan.TongQuanService;
import com.wms.util.BaoCaoJasperExporter;

import java.io.File;
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

    public List<DoanhThuReportRowDTO> layDongBaoCaoDoanhThu(String tuNgay, String denNgay, String chiNhanh, String loaiDT) {
        return service.layDongBaoCaoDoanhThu(tuNgay, denNgay, chiNhanh, loaiDT);
    }

    public DuLieuBaoCaoTongQuatDTO taoDuLieuBaoCao(String loaiBaoCao, String tuNgay, String denNgay,
                                                   String chiNhanh, String loaiDoanhThu, String nguoiXuat) {
        return service.taoDuLieuBaoCao(loaiBaoCao, tuNgay, denNgay, chiNhanh, loaiDoanhThu, nguoiXuat);
    }

    public void xuatBaoCaoPdf(File file, String loaiBaoCao, String tuNgay, String denNgay,
                              String chiNhanh, String loaiDoanhThu, String nguoiXuat) throws Exception {
        DuLieuBaoCaoTongQuatDTO duLieu = taoDuLieuBaoCao(loaiBaoCao, tuNgay, denNgay, chiNhanh, loaiDoanhThu, nguoiXuat);
        BaoCaoJasperExporter.xuatBaoCaoPdf(file, duLieu);
    }
}
