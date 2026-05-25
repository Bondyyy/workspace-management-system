package com.wms.service.TrangChuQuanLy.TongQuan;

import com.wms.dao.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDAO;
import com.wms.dao.TrangChuQuanLy.TongQuan.ThongKeDAO;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DoanhThuReportRowDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.TongQuanDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TongQuanService {

    public static final String BAO_CAO_DOANH_THU = "Báo cáo doanh thu";
    public static final String BAO_CAO_NHAP_KHO_DICH_VU = "Báo cáo nhập kho dịch vụ";
    public static final String BAO_CAO_CHI_PHI_NHAP_KHO = "Báo cáo chi phí nhập kho";
    public static final String BAO_CAO_DICH_VU_BAN_CHAY = "Báo cáo dịch vụ bán chạy";
    public static final String BAO_CAO_LOI_NHUAN_GOP_UOC_TINH = "Báo cáo lợi nhuận gộp ước tính";
    public static final String BAO_CAO_TRA_LUONG_NHAN_VIEN = "Báo cáo trả lương nhân viên";

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

    public List<DoanhThuReportRowDTO> layDongBaoCaoDoanhThu(String tuNgay, String denNgay, String chiNhanh, String loaiDT) {
        return thongKeDAO.layDongBaoCaoDoanhThu(tuNgay, denNgay, chiNhanh, loaiDT);
    }

    public DuLieuBaoCaoTongQuatDTO taoDuLieuBaoCao(
            String loaiBaoCao,
            String tuNgay,
            String denNgay,
            String maChiNhanh,
            String loaiDoanhThu,
            String nguoiXuat
    ) {
        String loaiBaoCaoAnToan = loaiBaoCao == null || loaiBaoCao.isBlank()
                ? BAO_CAO_DOANH_THU
                : loaiBaoCao.trim();

        DuLieuBaoCaoTongQuatDTO duLieu;
        switch (loaiBaoCaoAnToan) {
            case BAO_CAO_NHAP_KHO_DICH_VU -> duLieu = thongKeDAO.layBaoCaoNhapKhoDichVu(tuNgay, denNgay, maChiNhanh);
            case BAO_CAO_CHI_PHI_NHAP_KHO -> duLieu = thongKeDAO.layBaoCaoChiPhiNhapKho(tuNgay, denNgay, maChiNhanh);
            case BAO_CAO_DICH_VU_BAN_CHAY -> duLieu = thongKeDAO.layBaoCaoDichVuBanChay(tuNgay, denNgay, maChiNhanh);
            case BAO_CAO_LOI_NHUAN_GOP_UOC_TINH -> duLieu = thongKeDAO.layBaoCaoLoiNhuanGopUocTinh(tuNgay, denNgay, maChiNhanh);
            case BAO_CAO_TRA_LUONG_NHAN_VIEN -> duLieu = thongKeDAO.layBaoCaoTraLuongNhanVien(tuNgay, denNgay, maChiNhanh, loaiDoanhThu); // loaiDoanhThu acts as loaiNV here
            case BAO_CAO_DOANH_THU -> duLieu = thongKeDAO.layBaoCaoDoanhThu(tuNgay, denNgay, maChiNhanh, loaiDoanhThu);
            default -> duLieu = thongKeDAO.layBaoCaoDoanhThu(tuNgay, denNgay, maChiNhanh, loaiDoanhThu);
        }

        duLieu.setLoaiBaoCao(loaiBaoCaoAnToan);
        duLieu.setTuNgay(giaTriNgay(tuNgay));
        duLieu.setDenNgay(giaTriNgay(denNgay));
        duLieu.setTenChiNhanh(tenChiNhanhHienThi(maChiNhanh));
        duLieu.setNguoiXuat(nguoiXuat == null || nguoiXuat.isBlank() ? "Người dùng hệ thống" : nguoiXuat.trim());
        duLieu.setThoiGianXuat(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        duLieu.setPhuDeBaoCao("Dữ liệu được tổng hợp từ thông tin đã ghi nhận trong hệ thống.");
        return duLieu;
    }

    private String giaTriNgay(String value) {
        return value == null || value.isBlank() ? "Không giới hạn" : value.trim();
    }

    private String tenChiNhanhHienThi(String value) {
        return value == null || value.isBlank() ? "Tất cả chi nhánh" : value.trim();
    }
}
