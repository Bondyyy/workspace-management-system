package com.wms.dao.TrangChuQuanLy.TongQuan;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.TongQuan.DoanhThuReportRowDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DongBaoCaoTongQuatDTO;
import com.wms.model.TrangChuQuanLy.TongQuan.DuLieuBaoCaoTongQuatDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThongKeDAO {

    private static final String TAT_CA_CHI_NHANH = "Tất cả chi nhánh";
    private static final String TRANG_THAI_DA_THANH_TOAN = "Đã thanh toán thành công";
    private static final DecimalFormat FORMAT_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DecimalFormat FORMAT_SO = new DecimalFormat("#,##0");
    private static final SimpleDateFormat FORMAT_NGAY_GIO = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat FORMAT_NGAY = new SimpleDateFormat("dd/MM/yyyy");

    public List<Object[]> layRecentTransactions() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT h.MaHoaDon, nd.HoTen AS HoTenKH, h.ThanhTien, h.TrangThaiThanhToan " +
                "FROM HOADON h " +
                "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                "ORDER BY h.NgayLapHoaDon DESC " +
                "FETCH FIRST 5 ROWS ONLY";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            DecimalFormat df = new DecimalFormat("#,###");
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("MaHoaDon"),
                        giaTriMacDinh(rs.getString("HoTenKH"), "Khách vãng lai"),
                        df.format(rs.getDouble("ThanhTien")),
                        giaTriMacDinh(rs.getString("TrangThaiThanhToan"), "")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Double> layDoanhThuTongHop(String tuNgay, String denNgay, String maChiNhanh, String loaiDT) {
        Map<String, Double> ketQua = new HashMap<>();
        ketQua.put("doanhThuThuc", 0.0);
        ketQua.put("truocGiam", 0.0);
        ketQua.put("chietKhau", 0.0);

        StringBuilder sql = new StringBuilder(
                "SELECT NVL(SUM(h.ThanhTien), 0) AS DoanhThuThuc, NVL(SUM(h.TongTien), 0) AS TruocGiam " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan = ? ");

        appendDateAndBranchFilters(sql, "h.NgayLapHoaDon", true, "kg.MaCN", tuNgay, denNgay, maChiNhanh);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, TRANG_THAI_DA_THANH_TOAN);
            idx = bindDateRange(ps, idx, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double thuc = rs.getDouble("DoanhThuThuc");
                    double truoc = rs.getDouble("TruocGiam");
                    ketQua.put("doanhThuThuc", thuc);
                    ketQua.put("truocGiam", truoc);
                    ketQua.put("chietKhau", Math.max(0, truoc - thuc));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ketQua;
    }

    public List<Double> layDoanhThu7NgayGanNhat(String maChiNhanh) {
        List<Double> data = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT TRUNC(h.NgayLapHoaDon) AS Ngay, NVL(SUM(h.ThanhTien), 0) AS Tong " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan = ? " +
                        "AND h.NgayLapHoaDon >= TRUNC(SYSDATE) - 6 ");

        if (hasBranchFilter(maChiNhanh)) {
            sql.append("AND kg.MaCN = ? ");
        }
        sql.append("GROUP BY TRUNC(h.NgayLapHoaDon) ORDER BY Ngay");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, TRANG_THAI_DA_THANH_TOAN);
            if (hasBranchFilter(maChiNhanh)) {
                ps.setString(2, extractBranchCode(maChiNhanh));
            }

            Map<String, Double> map = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("Ngay").toString(), rs.getDouble("Tong"));
                }
            }

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_YEAR, -6);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (int i = 0; i < 7; i++) {
                String d = sdf.format(cal.getTime());
                data.add(map.getOrDefault(d, 0.0));
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public Map<String, Integer> layCoCauThanhToan() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("CK", 0);
        stats.put("TM", 0);

        String sql = "SELECT PhuongThucThanhToan, COUNT(*) AS SoLuong FROM HOADON " +
                "WHERE TrangThaiThanhToan = ? " +
                "GROUP BY PhuongThucThanhToan";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, TRANG_THAI_DA_THANH_TOAN);
            try (ResultSet rs = ps.executeQuery()) {
                int tong = 0;
                int ckCount = 0;
                int tmCount = 0;

                while (rs.next()) {
                    String pt = rs.getString("PhuongThucThanhToan");
                    int sl = rs.getInt("SoLuong");
                    if ("Chuyển khoản".equalsIgnoreCase(pt) || "Momo".equalsIgnoreCase(pt)) {
                        ckCount += sl;
                    } else {
                        tmCount += sl;
                    }
                    tong += sl;
                }

                if (tong > 0) {
                    stats.put("CK", (ckCount * 100) / tong);
                    stats.put("TM", 100 - stats.get("CK"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    public List<Object[]> layDanhSachHoaDonTheoDieuKien(String tuNgay, String denNgay, String maChiNhanh, String loaiDT) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT h.MaHoaDon, nd.HoTen AS HoTenKH, h.NgayLapHoaDon, h.TongTien, " +
                        "h.ThanhTien, h.PhuongThucThanhToan, h.TrangThaiThanhToan " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan = ? ");

        appendDateAndBranchFilters(sql, "h.NgayLapHoaDon", true, "kg.MaCN", tuNgay, denNgay, maChiNhanh);
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, TRANG_THAI_DA_THANH_TOAN);
            idx = bindDateRange(ps, idx, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                DecimalFormat df = new DecimalFormat("#,###");
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString("MaHoaDon"),
                            giaTriMacDinh(rs.getString("HoTenKH"), "Khách vãng lai"),
                            rs.getTimestamp("NgayLapHoaDon") != null ? FORMAT_NGAY_GIO.format(rs.getTimestamp("NgayLapHoaDon")) : "",
                            df.format(rs.getDouble("TongTien")),
                            df.format(rs.getDouble("ThanhTien")),
                            giaTriMacDinh(rs.getString("PhuongThucThanhToan"), ""),
                            giaTriMacDinh(rs.getString("TrangThaiThanhToan"), "")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DoanhThuReportRowDTO> layDongBaoCaoDoanhThu(String tuNgay, String denNgay, String maChiNhanh, String loaiDT) {
        List<DoanhThuReportRowDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT h.MaHoaDon, h.NgayLapHoaDon, nd.HoTen AS HoTenKH, cn.TenCN, kg.TenKG, " +
                        "h.TongTien, h.ThanhTien, h.PhuongThucThanhToan, h.TrangThaiThanhToan " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                        "WHERE h.TrangThaiThanhToan = ? ");

        appendDateAndBranchFilters(sql, "h.NgayLapHoaDon", true, "kg.MaCN", tuNgay, denNgay, maChiNhanh);
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, TRANG_THAI_DA_THANH_TOAN);
            idx = bindDateRange(ps, idx, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                DecimalFormat df = new DecimalFormat("#,###");
                while (rs.next()) {
                    list.add(new DoanhThuReportRowDTO(
                            rs.getString("MaHoaDon"),
                            rs.getTimestamp("NgayLapHoaDon") != null ? FORMAT_NGAY_GIO.format(rs.getTimestamp("NgayLapHoaDon")) : "",
                            giaTriMacDinh(rs.getString("HoTenKH"), "Khách vãng lai"),
                            giaTriMacDinh(rs.getString("TenCN"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenKG"), "Không xác định"),
                            df.format(rs.getDouble("TongTien")),
                            df.format(rs.getDouble("ThanhTien")),
                            giaTriMacDinh(rs.getString("PhuongThucThanhToan"), ""),
                            giaTriMacDinh(rs.getString("TrangThaiThanhToan"), "")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public DuLieuBaoCaoTongQuatDTO layBaoCaoDoanhThu(String tuNgay, String denNgay, String maChiNhanh, String loaiDT) {
        DuLieuBaoCaoTongQuatDTO duLieu = taoBaoCaoCoBan(
                "Báo cáo doanh thu",
                "BÁO CÁO DOANH THU",
                List.of("Mã HĐ", "Ngày lập", "Khách hàng", "Chi nhánh", "Không gian", "Tổng tiền", "Thành tiền", "Thanh toán"),
                "Chỉ tính hóa đơn có trạng thái Đã thanh toán thành công."
        );
        List<DongBaoCaoTongQuatDTO> rows = new ArrayList<>();
        double tongTien = 0;
        double thanhTien = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT h.MaHoaDon, h.NgayLapHoaDon, nd.HoTen AS HoTenKH, cn.TenCN, kg.TenKG, " +
                        "NVL(h.TongTien, 0) AS TongTien, NVL(h.ThanhTien, 0) AS ThanhTien, " +
                        "h.PhuongThucThanhToan, h.TrangThaiThanhToan " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                        "WHERE h.TrangThaiThanhToan = ? ");

        appendDateAndBranchFilters(sql, "h.NgayLapHoaDon", true, "kg.MaCN", tuNgay, denNgay, maChiNhanh);
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, TRANG_THAI_DA_THANH_TOAN);
            idx = bindDateRange(ps, idx, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double dongTongTien = rs.getDouble("TongTien");
                    double dongThanhTien = rs.getDouble("ThanhTien");
                    tongTien += dongTongTien;
                    thanhTien += dongThanhTien;
                    rows.add(new DongBaoCaoTongQuatDTO(
                            giaTriMacDinh(rs.getString("MaHoaDon"), ""),
                            dinhDangNgayGio(rs.getTimestamp("NgayLapHoaDon")),
                            giaTriMacDinh(rs.getString("HoTenKH"), "Khách vãng lai"),
                            giaTriMacDinh(rs.getString("TenCN"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenKG"), "Không xác định"),
                            dinhDangTien(dongTongTien),
                            dinhDangTien(dongThanhTien),
                            dinhDangThanhToan(rs.getString("PhuongThucThanhToan"), rs.getString("TrangThaiThanhToan"))
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        duLieu.setDanhSachDongBaoCao(rows);
        duLieu.setNhanTongGiaTri1("Tổng doanh thu");
        duLieu.setTongGiaTri1(dinhDangTien(thanhTien));
        duLieu.setNhanTongGiaTri2("Trước giảm giá");
        duLieu.setTongGiaTri2(dinhDangTien(tongTien));
        duLieu.setNhanTongGiaTri3("Tổng chiết khấu");
        duLieu.setTongGiaTri3(dinhDangTien(Math.max(0, tongTien - thanhTien)));
        return duLieu;
    }

    public DuLieuBaoCaoTongQuatDTO layBaoCaoNhapKhoDichVu(String tuNgay, String denNgay, String maChiNhanh) {
        DuLieuBaoCaoTongQuatDTO duLieu = taoBaoCaoCoBan(
                "Báo cáo nhập kho dịch vụ",
                "BÁO CÁO NHẬP KHO DỊCH VỤ",
                List.of("Mã chứng từ", "Ngày nhập", "Dịch vụ", "Nhân viên", "Chi nhánh", "Số lượng", "Tệp chứng từ", "Ghi chú"),
                "Dữ liệu lấy từ chứng từ nhập kho dịch vụ trong hệ thống."
        );
        List<DongBaoCaoTongQuatDTO> rows = new ArrayList<>();
        int tongPhieu = 0;
        double tongSoLuong = 0;
        double tongGiaTri = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT ctn.MaChungTu, ctn.NgayNhap, dv.TenDV, nd.HoTen AS TenNhanVien, cn.TenCN, " +
                        "NVL(ctn.SoLuongNhap, 0) AS SoLuongNhap, ctn.TenFile, dv.GiaNhap " +
                        "FROM CHUNGTUNHAPKHO ctn " +
                        "LEFT JOIN DICHVU dv ON ctn.MaDV = dv.MaDV " +
                        "LEFT JOIN NHANVIEN nv ON ctn.MaNV = nv.MaNV " +
                        "LEFT JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND " +
                        "LEFT JOIN CHINHANH cn ON ctn.MaCN = cn.MaCN " +
                        "WHERE 1 = 1 ");

        appendDateAndBranchFilters(sql, "ctn.NgayNhap", false, "ctn.MaCN", tuNgay, denNgay, maChiNhanh);
        sql.append("ORDER BY ctn.NgayNhap DESC, ctn.MaChungTu DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = bindDateRange(ps, 1, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double soLuong = rs.getDouble("SoLuongNhap");
                    Double giaNhap = layDoubleNullable(rs, "GiaNhap");
                    tongPhieu++;
                    tongSoLuong += soLuong;
                    tongGiaTri += soLuong * (giaNhap == null ? 0 : giaNhap);
                    rows.add(new DongBaoCaoTongQuatDTO(
                            giaTriMacDinh(rs.getString("MaChungTu"), ""),
                            dinhDangNgay(rs.getDate("NgayNhap")),
                            giaTriMacDinh(rs.getString("TenDV"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenNhanVien"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenCN"), "Không xác định"),
                            FORMAT_SO.format(soLuong),
                            giaTriMacDinh(rs.getString("TenFile"), "Không có tệp"),
                            giaNhap == null ? "Chưa có giá nhập" : ""
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        duLieu.setDanhSachDongBaoCao(rows);
        duLieu.setNhanTongGiaTri1("Tổng phiếu nhập");
        duLieu.setTongGiaTri1(FORMAT_SO.format(tongPhieu));
        duLieu.setNhanTongGiaTri2("Tổng số lượng nhập");
        duLieu.setTongGiaTri2(FORMAT_SO.format(tongSoLuong));
        duLieu.setNhanTongGiaTri3("Tổng giá trị nhập");
        duLieu.setTongGiaTri3(dinhDangTien(tongGiaTri));
        return duLieu;
    }

    public DuLieuBaoCaoTongQuatDTO layBaoCaoChiPhiNhapKho(String tuNgay, String denNgay, String maChiNhanh) {
        DuLieuBaoCaoTongQuatDTO duLieu = taoBaoCaoCoBan(
                "Báo cáo chi phí nhập kho",
                "BÁO CÁO CHI PHÍ NHẬP KHO",
                List.of("Mã chứng từ", "Ngày nhập", "Dịch vụ", "Loại dịch vụ", "Chi nhánh", "Số lượng", "Giá nhập", "Chi phí nhập"),
                "Chi phí nhập kho = Số lượng nhập * Giá nhập. Nếu dịch vụ chưa có giá nhập, chi phí được tính là 0."
        );
        List<DongBaoCaoTongQuatDTO> rows = new ArrayList<>();
        int tongPhieu = 0;
        int soDongThieuGiaNhap = 0;
        double tongSoLuong = 0;
        double tongChiPhi = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT ctn.MaChungTu, ctn.NgayNhap, dv.TenDV, ldv.TenLoaiDV, cn.TenCN, " +
                        "NVL(ctn.SoLuongNhap, 0) AS SoLuongNhap, dv.GiaNhap " +
                        "FROM CHUNGTUNHAPKHO ctn " +
                        "LEFT JOIN DICHVU dv ON ctn.MaDV = dv.MaDV " +
                        "LEFT JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV " +
                        "LEFT JOIN CHINHANH cn ON ctn.MaCN = cn.MaCN " +
                        "WHERE 1 = 1 ");

        appendDateAndBranchFilters(sql, "ctn.NgayNhap", false, "ctn.MaCN", tuNgay, denNgay, maChiNhanh);
        sql.append("ORDER BY ctn.NgayNhap DESC, ctn.MaChungTu DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = bindDateRange(ps, 1, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double soLuong = rs.getDouble("SoLuongNhap");
                    Double giaNhap = layDoubleNullable(rs, "GiaNhap");
                    double chiPhi = soLuong * (giaNhap == null ? 0 : giaNhap);
                    tongPhieu++;
                    tongSoLuong += soLuong;
                    tongChiPhi += chiPhi;
                    if (giaNhap == null) {
                        soDongThieuGiaNhap++;
                    }
                    rows.add(new DongBaoCaoTongQuatDTO(
                            giaTriMacDinh(rs.getString("MaChungTu"), ""),
                            dinhDangNgay(rs.getDate("NgayNhap")),
                            giaTriMacDinh(rs.getString("TenDV"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenLoaiDV"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenCN"), "Không xác định"),
                            FORMAT_SO.format(soLuong),
                            giaNhap == null ? "Chưa có giá nhập" : dinhDangTien(giaNhap),
                            dinhDangTien(chiPhi)
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        duLieu.setDanhSachDongBaoCao(rows);
        duLieu.setNhanTongGiaTri1("Tổng chi phí nhập kho");
        duLieu.setTongGiaTri1(dinhDangTien(tongChiPhi));
        duLieu.setNhanTongGiaTri2("Tổng số lượng nhập");
        duLieu.setTongGiaTri2(FORMAT_SO.format(tongSoLuong));
        duLieu.setNhanTongGiaTri3("Tổng phiếu nhập");
        duLieu.setTongGiaTri3(FORMAT_SO.format(tongPhieu));
        if (soDongThieuGiaNhap > 0) {
            duLieu.setGhiChuBaoCao(duLieu.getGhiChuBaoCao() + " Có " + soDongThieuGiaNhap + " dòng chưa có giá nhập nên chi phí được tính là 0.");
        }
        return duLieu;
    }

    public DuLieuBaoCaoTongQuatDTO layBaoCaoDichVuBanChay(String tuNgay, String denNgay, String maChiNhanh) {
        DuLieuBaoCaoTongQuatDTO duLieu = taoBaoCaoCoBan(
                "Báo cáo dịch vụ bán chạy",
                "BÁO CÁO DỊCH VỤ BÁN CHẠY",
                List.of("Mã DV", "Dịch vụ", "Loại dịch vụ", "Số lượng bán", "Doanh thu DV", "Số phiên", "Đơn giá", "Ghi chú"),
                "Chỉ tính dịch vụ thuộc các hóa đơn đã thanh toán thành công. Doanh thu dịch vụ là số lượng * đơn giá dịch vụ."
        );
        List<DongBaoCaoTongQuatDTO> rows = new ArrayList<>();
        int soDichVu = 0;
        double tongSoLuong = 0;
        double tongDoanhThuDichVu = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT dv.MaDV, dv.TenDV, ldv.TenLoaiDV, NVL(SUM(NVL(ctdv.SoLuong, 0)), 0) AS SoLuongBan, " +
                        "NVL(SUM(NVL(ctdv.SoLuong, 0) * NVL(dv.DonGia, 0)), 0) AS DoanhThuDichVu, " +
                        "COUNT(DISTINCT ctdv.MaPhien) AS SoPhien, NVL(dv.DonGia, 0) AS DonGia " +
                        "FROM CHITIETDICHVU ctdv " +
                        "JOIN DICHVU dv ON ctdv.MaDV = dv.MaDV " +
                        "LEFT JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV " +
                        "JOIN PHIENLAMVIEC p ON ctdv.MaPhien = p.MaPhien " +
                        "JOIN HOADON h ON p.MaPhien = h.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan = ? ");

        appendDateAndBranchFilters(sql, "h.NgayLapHoaDon", true, "kg.MaCN", tuNgay, denNgay, maChiNhanh);
        sql.append("GROUP BY dv.MaDV, dv.TenDV, ldv.TenLoaiDV, dv.DonGia ");
        sql.append("ORDER BY SoLuongBan DESC, DoanhThuDichVu DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, TRANG_THAI_DA_THANH_TOAN);
            idx = bindDateRange(ps, idx, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double soLuong = rs.getDouble("SoLuongBan");
                    double doanhThu = rs.getDouble("DoanhThuDichVu");
                    soDichVu++;
                    tongSoLuong += soLuong;
                    tongDoanhThuDichVu += doanhThu;
                    rows.add(new DongBaoCaoTongQuatDTO(
                            giaTriMacDinh(rs.getString("MaDV"), ""),
                            giaTriMacDinh(rs.getString("TenDV"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenLoaiDV"), "Không xác định"),
                            FORMAT_SO.format(soLuong),
                            dinhDangTien(doanhThu),
                            FORMAT_SO.format(rs.getInt("SoPhien")),
                            dinhDangTien(rs.getDouble("DonGia")),
                            "Ước tính theo đơn giá hiện tại"
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        duLieu.setDanhSachDongBaoCao(rows);
        duLieu.setNhanTongGiaTri1("Doanh thu dịch vụ ước tính");
        duLieu.setTongGiaTri1(dinhDangTien(tongDoanhThuDichVu));
        duLieu.setNhanTongGiaTri2("Tổng số lượng bán");
        duLieu.setTongGiaTri2(FORMAT_SO.format(tongSoLuong));
        duLieu.setNhanTongGiaTri3("Số dịch vụ có bán");
        duLieu.setTongGiaTri3(FORMAT_SO.format(soDichVu));
        return duLieu;
    }

    public DuLieuBaoCaoTongQuatDTO layBaoCaoLoiNhuanGopUocTinh(String tuNgay, String denNgay, String maChiNhanh) {
        DuLieuBaoCaoTongQuatDTO duLieu = taoBaoCaoCoBan(
                "Báo cáo lợi nhuận gộp ước tính",
                "BÁO CÁO LỢI NHUẬN GỘP ƯỚC TÍNH",
                List.of("Mã DV", "Dịch vụ", "Loại dịch vụ", "Số lượng bán", "Doanh thu DV", "Giá vốn", "Lợi nhuận DV", "Ghi chú"),
                "Lợi nhuận gộp ước tính = doanh thu hóa đơn đã thanh toán - giá vốn dịch vụ. Chưa bao gồm lương, thuê mặt bằng, điện nước, thuế hoặc các chi phí chưa được lưu trong database."
        );
        List<DongBaoCaoTongQuatDTO> rows = new ArrayList<>();
        double doanhThuHoaDon = layTongDoanhThuHoaDonDaThanhToan(tuNgay, denNgay, maChiNhanh);
        double tongGiaVon = 0;
        int soDichVuThieuGiaNhap = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT dv.MaDV, dv.TenDV, ldv.TenLoaiDV, NVL(SUM(NVL(ctdv.SoLuong, 0)), 0) AS SoLuongBan, " +
                        "NVL(SUM(NVL(ctdv.SoLuong, 0) * NVL(dv.DonGia, 0)), 0) AS DoanhThuDichVu, " +
                        "NVL(SUM(NVL(ctdv.SoLuong, 0) * NVL(dv.GiaNhap, 0)), 0) AS GiaVon, " +
                        "COUNT(CASE WHEN dv.GiaNhap IS NULL THEN 1 END) AS SoDongThieuGiaNhap " +
                        "FROM CHITIETDICHVU ctdv " +
                        "JOIN DICHVU dv ON ctdv.MaDV = dv.MaDV " +
                        "LEFT JOIN LOAIDICHVU ldv ON dv.MaLoaiDV = ldv.MaLoaiDV " +
                        "JOIN PHIENLAMVIEC p ON ctdv.MaPhien = p.MaPhien " +
                        "JOIN HOADON h ON p.MaPhien = h.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan = ? ");

        appendDateAndBranchFilters(sql, "h.NgayLapHoaDon", true, "kg.MaCN", tuNgay, denNgay, maChiNhanh);
        sql.append("GROUP BY dv.MaDV, dv.TenDV, ldv.TenLoaiDV ");
        sql.append("ORDER BY GiaVon DESC, DoanhThuDichVu DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, TRANG_THAI_DA_THANH_TOAN);
            idx = bindDateRange(ps, idx, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double doanhThuDichVu = rs.getDouble("DoanhThuDichVu");
                    double giaVon = rs.getDouble("GiaVon");
                    int soDongThieuGiaNhap = rs.getInt("SoDongThieuGiaNhap");
                    tongGiaVon += giaVon;
                    if (soDongThieuGiaNhap > 0) {
                        soDichVuThieuGiaNhap++;
                    }
                    rows.add(new DongBaoCaoTongQuatDTO(
                            giaTriMacDinh(rs.getString("MaDV"), ""),
                            giaTriMacDinh(rs.getString("TenDV"), "Không xác định"),
                            giaTriMacDinh(rs.getString("TenLoaiDV"), "Không xác định"),
                            FORMAT_SO.format(rs.getDouble("SoLuongBan")),
                            dinhDangTien(doanhThuDichVu),
                            dinhDangTien(giaVon),
                            dinhDangTien(doanhThuDichVu - giaVon),
                            soDongThieuGiaNhap > 0 ? "Thiếu giá nhập" : "Ước tính"
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        duLieu.setDanhSachDongBaoCao(rows);
        duLieu.setNhanTongGiaTri1("Doanh thu thực tế");
        duLieu.setTongGiaTri1(dinhDangTien(doanhThuHoaDon));
        duLieu.setNhanTongGiaTri2("Giá vốn ước tính");
        duLieu.setTongGiaTri2(dinhDangTien(tongGiaVon));
        duLieu.setNhanTongGiaTri3("Lợi nhuận gộp ước tính");
        duLieu.setTongGiaTri3(dinhDangTien(doanhThuHoaDon - tongGiaVon));
        if (soDichVuThieuGiaNhap > 0) {
            duLieu.setGhiChuBaoCao(duLieu.getGhiChuBaoCao() + " Một số dịch vụ chưa có giá nhập nên lợi nhuận chỉ mang tính tham khảo.");
        }
        return duLieu;
    }

    private double layTongDoanhThuHoaDonDaThanhToan(String tuNgay, String denNgay, String maChiNhanh) {
        double tong = 0;
        StringBuilder sql = new StringBuilder(
                "SELECT NVL(SUM(h.ThanhTien), 0) AS TongDoanhThu " +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "WHERE h.TrangThaiThanhToan = ? ");
        appendDateAndBranchFilters(sql, "h.NgayLapHoaDon", true, "kg.MaCN", tuNgay, denNgay, maChiNhanh);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, TRANG_THAI_DA_THANH_TOAN);
            idx = bindDateRange(ps, idx, tuNgay, denNgay);
            bindBranch(ps, idx, maChiNhanh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tong = rs.getDouble("TongDoanhThu");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tong;
    }

    public DuLieuBaoCaoTongQuatDTO layBaoCaoTraLuongNhanVien(String tuNgay, String denNgay, String maChiNhanh, String loaiNV) {
        DuLieuBaoCaoTongQuatDTO duLieu = taoBaoCaoCoBan(
                "Báo cáo trả lương nhân viên",
                "BÁO CÁO TRẢ LƯƠNG NHÂN VIÊN",
                List.of("Mã NV", "Họ tên (Loại NV)", "Chi nhánh", "Ngày vào làm", "Lương CB", "Phụ cấp & Thưởng", "Số ngày TL", "Tổng lương"),
                "Tổng lương = (Lương CB / 30) * Số ngày TL + Phụ cấp + Tiền thưởng. Số ngày TL tính từ Từ ngày (hoặc Ngày vào làm nếu sau Từ ngày) đến Đến ngày."
        );
        List<DongBaoCaoTongQuatDTO> rows = new ArrayList<>();
        int tongNhanVien = 0;
        double tongLuongCoBan = 0;
        double tongPhaiTra = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT nv.MaNV, nd.HoTen, nv.LoaiNV, cn.TenCN, nv.NgayVaoLam, " +
                        "NVL(nv.LuongCoBan, 0) AS LuongCoBan, NVL(nv.PhuCap, 0) AS PhuCap, NVL(nv.TienThuong, 0) AS TienThuong " +
                        "FROM NHANVIEN nv " +
                        "JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND " +
                        "LEFT JOIN CHINHANH cn ON nv.MaCN = cn.MaCN " +
                        "WHERE NVL(nv.TrangThaiLamViec, 'Đang làm việc') <> 'Ngừng làm việc' " +
                        "AND NOT EXISTS (" +
                        "   SELECT 1 FROM CHITIETVAITRO ctv JOIN VAITRO vt ON vt.MaVaiTro = ctv.MaVaiTro " +
                        "   WHERE ctv.MaND = nd.MaND AND LOWER(vt.TenVaiTro) LIKE '%quản trị viên hệ thống%'" +
                        ") " +
                        "AND NOT EXISTS (" +
                        "   SELECT 1 FROM CHITIETVAITRO ctv JOIN VAITRO vt ON vt.MaVaiTro = ctv.MaVaiTro " +
                        "   WHERE ctv.MaND = nd.MaND AND LOWER(vt.TenVaiTro) LIKE '%hội viên%'" +
                        ") ");

        if (loaiNV != null && !loaiNV.isBlank() && !"Tất cả".equalsIgnoreCase(loaiNV)) {
            sql.append("AND nv.LoaiNV = ? ");
        }
        if (hasBranchFilter(maChiNhanh)) {
            sql.append("AND nv.MaCN = ? ");
        }

        sql.append("ORDER BY nv.MaNV ASC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (loaiNV != null && !loaiNV.isBlank() && !"Tất cả".equalsIgnoreCase(loaiNV)) {
                ps.setString(idx++, loaiNV);
            }
            if (hasBranchFilter(maChiNhanh)) {
                ps.setString(idx++, extractBranchCode(maChiNhanh));
            }

            java.time.LocalDate start = null;
            java.time.LocalDate end = null;
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            try {
                if (tuNgay != null && !tuNgay.isBlank()) start = java.time.LocalDate.parse(tuNgay, dtf);
                if (denNgay != null && !denNgay.isBlank()) end = java.time.LocalDate.parse(denNgay, dtf);
            } catch (Exception e) {}

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date ngayVaoLamSql = rs.getDate("NgayVaoLam");
                    if (ngayVaoLamSql == null) continue;
                    java.time.LocalDate ngayVaoLam = ngayVaoLamSql.toLocalDate();

                    java.time.LocalDate ngayBatDauTinhLuong = ngayVaoLam;
                    if (start != null && start.isAfter(ngayVaoLam)) {
                        ngayBatDauTinhLuong = start;
                    }

                    java.time.LocalDate ngayKetThucTinhLuong = end != null ? end : java.time.LocalDate.now();

                    if (ngayBatDauTinhLuong.isAfter(ngayKetThucTinhLuong)) {
                        continue; 
                    }
                    if (ngayVaoLam.isAfter(ngayKetThucTinhLuong)) {
                        continue; 
                    }

                    long soNgayTinhLuong = java.time.temporal.ChronoUnit.DAYS.between(ngayBatDauTinhLuong, ngayKetThucTinhLuong) + 1;

                    double luongCoBan = rs.getDouble("LuongCoBan");
                    double phuCap = rs.getDouble("PhuCap");
                    double tienThuong = rs.getDouble("TienThuong");

                    double luongTheoNgay = luongCoBan / 30.0;
                    double tongLuong = (luongTheoNgay * soNgayTinhLuong) + phuCap + tienThuong;

                    tongNhanVien++;
                    tongLuongCoBan += luongCoBan;
                    tongPhaiTra += tongLuong;

                    String hoTenLoai = rs.getString("HoTen") + " (" + rs.getString("LoaiNV") + ")";

                    rows.add(new DongBaoCaoTongQuatDTO(
                            rs.getString("MaNV"),
                            hoTenLoai,
                            giaTriMacDinh(rs.getString("TenCN"), "Chưa phân"),
                            dinhDangNgay(ngayVaoLamSql),
                            dinhDangTien(luongCoBan),
                            dinhDangTien(phuCap + tienThuong),
                            String.valueOf(soNgayTinhLuong),
                            dinhDangTien(tongLuong)
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        duLieu.setDanhSachDongBaoCao(rows);
        duLieu.setNhanTongGiaTri1("Tổng số nhân viên");
        duLieu.setTongGiaTri1(FORMAT_SO.format(tongNhanVien));
        duLieu.setNhanTongGiaTri2("Tổng lương cơ bản");
        duLieu.setTongGiaTri2(dinhDangTien(tongLuongCoBan));
        duLieu.setNhanTongGiaTri3("Tổng lương phải trả");
        duLieu.setTongGiaTri3(dinhDangTien(tongPhaiTra));
        return duLieu;
    }

    private DuLieuBaoCaoTongQuatDTO taoBaoCaoCoBan(String loaiBaoCao, String tieuDe,
                                                   List<String> danhSachTieuDeCot, String ghiChu) {
        DuLieuBaoCaoTongQuatDTO duLieu = new DuLieuBaoCaoTongQuatDTO();
        duLieu.setLoaiBaoCao(loaiBaoCao);
        duLieu.setTieuDeBaoCao(tieuDe);
        duLieu.setDanhSachTieuDeCot(danhSachTieuDeCot);
        duLieu.setGhiChuBaoCao(ghiChu);
        duLieu.setDanhSachDongBaoCao(new ArrayList<>());
        duLieu.setNhanTongGiaTri1("");
        duLieu.setTongGiaTri1("0");
        duLieu.setNhanTongGiaTri2("");
        duLieu.setTongGiaTri2("0");
        duLieu.setNhanTongGiaTri3("");
        duLieu.setTongGiaTri3("0");
        return duLieu;
    }

    private void appendDateAndBranchFilters(StringBuilder sql, String dateColumn, boolean timestampColumn,
                                            String branchColumn, String tuNgay, String denNgay, String maChiNhanh) {
        String dateFunction = timestampColumn ? "TO_TIMESTAMP" : "TO_DATE";
        if (tuNgay != null && !tuNgay.isBlank()) {
            sql.append("AND ").append(dateColumn).append(" >= ")
                    .append(dateFunction).append("(?, 'YYYY-MM-DD HH24:MI:SS') ");
        }
        if (denNgay != null && !denNgay.isBlank()) {
            sql.append("AND ").append(dateColumn).append(" <= ")
                    .append(dateFunction).append("(?, 'YYYY-MM-DD HH24:MI:SS') ");
        }
        if (hasBranchFilter(maChiNhanh) && branchColumn != null && !branchColumn.isBlank()) {
            sql.append("AND ").append(branchColumn).append(" = ? ");
        }
    }

    private int bindDateRange(PreparedStatement ps, int idx, String tuNgay, String denNgay) throws java.sql.SQLException {
        if (tuNgay != null && !tuNgay.isBlank()) {
            ps.setString(idx++, convertFormat(tuNgay) + " 00:00:00");
        }
        if (denNgay != null && !denNgay.isBlank()) {
            ps.setString(idx++, convertFormat(denNgay) + " 23:59:59");
        }
        return idx;
    }

    private int bindBranch(PreparedStatement ps, int idx, String maChiNhanh) throws java.sql.SQLException {
        if (hasBranchFilter(maChiNhanh)) {
            ps.setString(idx++, extractBranchCode(maChiNhanh));
        }
        return idx;
    }

    private boolean hasBranchFilter(String maChiNhanh) {
        if (maChiNhanh == null || maChiNhanh.isBlank()) {
            return false;
        }
        String normalized = maChiNhanh.trim();
        return !TAT_CA_CHI_NHANH.equalsIgnoreCase(normalized)
                && !normalized.toLowerCase().startsWith("tất cả");
    }

    private String extractBranchCode(String maChiNhanh) {
        if (maChiNhanh == null) {
            return "";
        }
        return maChiNhanh.split(" - ")[0].trim();
    }

    private String convertFormat(String dateStr) {
        try {
            String[] p = dateStr.split("/");
            return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String giaTriMacDinh(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String dinhDangTien(double value) {
        return FORMAT_TIEN.format(value);
    }

    private String dinhDangNgayGio(java.util.Date value) {
        return value == null ? "" : FORMAT_NGAY_GIO.format(value);
    }

    private String dinhDangNgay(java.util.Date value) {
        return value == null ? "" : FORMAT_NGAY.format(value);
    }

    private String dinhDangThanhToan(String phuongThuc, String trangThai) {
        String thanhToan = giaTriMacDinh(phuongThuc, "Chưa có phương thức");
        String trangThaiHienThi = giaTriMacDinh(trangThai, "");
        return trangThaiHienThi.isBlank() ? thanhToan : thanhToan + " / " + trangThaiHienThi;
    }

    private Double layDoubleNullable(ResultSet rs, String column) throws java.sql.SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
