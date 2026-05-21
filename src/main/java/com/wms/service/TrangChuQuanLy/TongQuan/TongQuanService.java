package com.wms.service.TrangChuQuanLy.TongQuan;

import com.wms.dao.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDAO;
import com.wms.dao.TrangChuQuanLy.TongQuan.ThongKeDAO;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.TongQuanDTO;

import java.util.List;
import java.util.Map;

public class TongQuanService {

    private final ThongKeDAO thongKeDAO = new ThongKeDAO();
    private final ChiNhanhDAO chiNhanhDAO = new ChiNhanhDAO();

    public TongQuanDTO layDuLieuTongQuan(String tuNgay, String denNgay, String chiNhanh, String loaiDT) {
        Map<String, Double> stats = thongKeDAO.layDoanhThuTongHop(tuNgay, denNgay, chiNhanh, loaiDT);
        List<Double> chart7Ngay = thongKeDAO.layDoanhThu7NgayGanNhat(chiNhanh);
        Map<String, Integer> coCau = thongKeDAO.layCoCauThanhToan();
        List<Object[]> giaoDich = thongKeDAO.layRecentTransactions();

        TongQuanDTO dto = new TongQuanDTO();
        dto.setDoanhThuThuc(stats.getOrDefault("doanhThuThuc", 0.0));
        dto.setTruocGiam(stats.getOrDefault("truocGiam", 0.0));
        dto.setChietKhau(stats.getOrDefault("chietKhau", 0.0));
        dto.setDoanhThu7Ngay(chart7Ngay);
        dto.setCoCauThanhToan(coCau);
        dto.setGiaoDichGanNhat(giaoDich);
        return dto;
    }

    public List<ChiNhanhDTO> layDanhSachChiNhanh() {
        return chiNhanhDAO.layDanhSachChiNhanh();
    }

    public List<Object[]> layDanhSachHoaDonTheoDieuKien(String tuNgay, String denNgay, String chiNhanh, String loaiDT) {
        return thongKeDAO.layDanhSachHoaDonTheoDieuKien(tuNgay, denNgay, chiNhanh, loaiDT);
    }
}
