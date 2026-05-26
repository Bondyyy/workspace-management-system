package com.wms.dao.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.HoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.KetQuaThanhToanDTO;
import java.sql.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HoaDonDAO {

    private static final long MILLIS_IN_HOUR = 3600000;

    public List<HoaDonDTO> layDanhSachHoaDon(String searchText, String statusFilter) {
        long start = System.currentTimeMillis();
        List<HoaDonDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT h.*, nd.HoTen AS HoTenKH, p.MaDatCho, p.TrangThaiPhien, p.ThoiGianBatDau, p.ThoiGianKetThuc, p.ThoiGianDuKienKetThuc, "
                        + "NVL(h.DaTraTruoc, 0) AS SoTienDaTraTruoc "
                        +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho " +
                        "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "WHERE 1=1 ");

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append("AND (h.MaHoaDon LIKE ? OR h.SoHD LIKE ? OR h.MaPhien LIKE ? OR nd.HoTen LIKE ?) ");
        }
        if (statusFilter != null && !statusFilter.equals("Tất cả")) {
            if (statusFilter.equals("Chưa thanh toán")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đang chờ thanh toán' ");
            } else if (statusFilter.equals("Đã trả trước")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đã trả trước' ");
            } else if (statusFilter.equals("Đã thanh toán")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đã thanh toán thành công' ");
            } else if (statusFilter.equals("Đang chờ thanh toán phụ thu")) {
                sql.append("AND NVL(h.DaTraTruoc, 0) > 0 AND NVL(h.ThanhTien, 0) > 0 AND h.TrangThaiThanhToan <> 'Đã thanh toán thành công' ");
            } else if (statusFilter.equals("Đã hủy")) {
                sql.append("AND h.TrangThaiThanhToan = 'Thanh toán không thành công' ");
            } else {
                sql.append("AND h.TrangThaiThanhToan = ? ");
            }
        }
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String q = "%" + searchText.trim() + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
            }
            if (statusFilter != null && !statusFilter.equals("Tất cả") 
                    && !statusFilter.equals("Chưa thanh toán") 
                    && !statusFilter.equals("Đã trả trước")
                    && !statusFilter.equals("Đã thanh toán")
                    && !statusFilter.equals("Đang chờ thanh toán phụ thu")
                    && !statusFilter.equals("Đã hủy")) {
                ps.setString(idx++, statusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDonDTO hd = new HoaDonDTO();
                    hd.setMaHoaDon(rs.getString("MaHoaDon"));
                    hd.setSoHD(rs.getString("SoHD"));
                    hd.setSoTienDaTraTruoc(rs.getDouble("SoTienDaTraTruoc"));
                    hd.setDaTraTruoc(rs.getDouble("SoTienDaTraTruoc") > 0);

                    double tt = rs.getDouble("TongTien");
                    double thanh = rs.getDouble("ThanhTien");
                    if (tt == 0 && thanh > 0)
                        tt = thanh;

                    hd.setTongTien(tt);
                    hd.setThanhTien(thanh);
                    hd.setNgayLapHoaDon(rs.getTimestamp("NgayLapHoaDon"));
                    hd.setPhuongThucThanhToan(rs.getString("PhuongThucThanhToan"));
                    hd.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
                    hd.setMaPhien(rs.getString("MaPhien"));
                    hd.setMaPGG(rs.getString("MaPGG"));
                    hd.setMaNV(rs.getString("MaNV"));
                    hd.setHoTenKH(rs.getString("HoTenKH"));
                    hd.setMaDatCho(rs.getString("MaDatCho"));
                    hd.setTrangThaiPhien(rs.getString("TrangThaiPhien"));
                    hd.setThoiGianBatDauPhien(rs.getTimestamp("ThoiGianBatDau"));
                    hd.setThoiGianKetThucPhien(rs.getTimestamp("ThoiGianKetThuc"));
                    hd.setThoiGianDuKienKetThucPhien(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                    list.add(hd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logElapsed("load bang hoa don", start);
        }
        return list;
    }

    public boolean taoHoaDonMoi(HoaDonDTO hd) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO HOADON (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, TrangThaiThanhToan, MaPhien) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        String sqlExists = "SELECT COUNT(*) FROM HOADON WHERE MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (hd.getMaPhien() != null && !hd.getMaPhien().trim().isEmpty()) {
                try (PreparedStatement psExists = conn.prepareStatement(sqlExists)) {
                    psExists.setString(1, hd.getMaPhien().trim());
                    try (ResultSet rs = psExists.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("[HoaDonDAO] Bo qua tao hoa don moi vi MaPhien da co hoa don: " + hd.getMaPhien());
                            return false;
                        }
                    }
                }
            }

            pstmt.setString(1, hd.getMaHoaDon());
            pstmt.setString(2, hd.getSoHD());
            pstmt.setDouble(3, hd.getTongTien());
            pstmt.setDouble(4, hd.getThanhTien());
            pstmt.setString(5, "Đang chờ thanh toán");
            pstmt.setString(6, hd.getMaPhien());

            return pstmt.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("[HoaDonDAO] Khong tao hoa don trung MaPhien: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            logElapsed("tao hoa don moi", start);
        }
    }

    public HoaDonDTO capNhatHoaDonDatTruocTheoPhien(String maPhien, double tongTien, double thanhTien) {
        String sqlUpdate = "UPDATE HOADON SET TongTien = ?, TongTienGoc = ?, TienGocDatTruoc = ?, "
                + "DaTraTruoc = GREATEST(NVL(DaTraTruoc, 0), ?), ThanhTien = ? WHERE MaPhien = ?";
        String sqlSelect = "SELECT h.*, nd.HoTen AS HoTenKH, p.MaDatCho, p.TrangThaiPhien, p.ThoiGianBatDau, p.ThoiGianKetThuc, p.ThoiGianDuKienKetThuc, "
                + "NVL(h.DaTraTruoc, 0) AS SoTienDaTraTruoc "
                + "FROM HOADON h "
                + "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien "
                + "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho "
                + "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH "
                + "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND "
                + "WHERE h.MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
            psUpdate.setDouble(1, tongTien);
            psUpdate.setDouble(2, tongTien);
            psUpdate.setDouble(3, tongTien);
            psUpdate.setDouble(4, Math.max(0, tongTien - thanhTien));
            psUpdate.setDouble(5, thanhTien);
            psUpdate.setString(6, maPhien);
            psUpdate.executeUpdate();

            try (PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
                psSelect.setString(1, maPhien);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        HoaDonDTO hd = new HoaDonDTO();
                        hd.setMaHoaDon(rs.getString("MaHoaDon"));
                        hd.setSoHD(rs.getString("SoHD"));
                        hd.setSoTienDaTraTruoc(rs.getDouble("SoTienDaTraTruoc"));
                    hd.setDaTraTruoc(rs.getDouble("SoTienDaTraTruoc") > 0);
                        hd.setTongTien(rs.getDouble("TongTien"));
                        hd.setThanhTien(rs.getDouble("ThanhTien"));
                        hd.setNgayLapHoaDon(rs.getTimestamp("NgayLapHoaDon"));
                        hd.setPhuongThucThanhToan(rs.getString("PhuongThucThanhToan"));
                        hd.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
                        hd.setMaPhien(rs.getString("MaPhien"));
                        hd.setMaPGG(rs.getString("MaPGG"));
                        hd.setMaNV(rs.getString("MaNV"));
                        hd.setHoTenKH(rs.getString("HoTenKH"));
                        hd.setMaDatCho(rs.getString("MaDatCho"));
                        hd.setTrangThaiPhien(rs.getString("TrangThaiPhien"));
                        hd.setThoiGianBatDauPhien(rs.getTimestamp("ThoiGianBatDau"));
                        hd.setThoiGianKetThucPhien(rs.getTimestamp("ThoiGianKetThuc"));
                        hd.setThoiGianDuKienKetThucPhien(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                        return hd;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean capNhatTrangThaiThanhToanTheoPhien(String maPhien, String trangThai) {
        String sql = "UPDATE HOADON SET TrangThaiThanhToan = ?, NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trangThai);
            pstmt.setString(2, maPhien);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ThongTinHoaDonDTO layThongTinChiTietHoaDon(String maHoaDon) {
        long start = System.currentTimeMillis();
        ThongTinHoaDonDTO thongTin = null;
        String sqlChung = "SELECT h.MaHoaDon, h.NgayLapHoaDon, h.PhuongThucThanhToan, "
                + "NVL(NULLIF(h.TongTienGoc, 0), NVL(h.TongTien, 0)) AS TongTienGocLuu, h.TongTien AS TongTienLuu, h.ThanhTien AS ThanhTienLuu, "
                + "NVL(h.TienGocDatTruoc, 0) AS TienGocDatTruoc, "
                + "NVL(h.TienGocPhatSinh, 0) AS TienGocPhatSinh, "
                + "h.MaPGG, h.MaPGGDatTruoc, "
                + "pggdt.MaChuSoPGG AS MaChuSoPGGDatTruoc, "
                + "h.MaPGGTaiQuay, "
                + "pggtq.MaChuSoPGG AS MaChuSoPGGTaiQuay, "
                + "pgg.MaChuSoPGG, NVL(pgg.GiaTriGiamGia, 0) AS GiaTriGiamVoucher, "
                + "NVL(pggtq.GiaTriGiamGia, 0) AS GiaTriGiamVoucherTaiQuay, "
                + "NVL(h.TienGiamVoucherDatTruoc, 0) AS TienGiamVoucherDatTruoc, "
                + "NVL(h.PhanTramGiamHangTVDatTruoc, 0) AS PhanTramGiamHangTVDatTruoc, "
                + "NVL(h.TienGiamHangTVDatTruoc, 0) AS TienGiamHangTVDatTruoc, "
                + "NVL(h.TienGiamVoucherTaiQuay, 0) AS TienGiamVoucherTaiQuay, "
                + "NVL(h.PhanTramGiamHangTVTaiQuay, 0) AS PhanTramGiamHangTVTaiQuay, "
                + "NVL(h.TienGiamHangTVTaiQuay, 0) AS TienGiamHangTVTaiQuay, "
                + "NVL(h.TongTienGiam, 0) AS TongTienGiamLuu, "
                + "NVL(h.SoTienThanhToanTaiQuay, 0) AS SoTienThanhToanTaiQuay, "
                + "p.MaPhien, p.ThoiGianBatDau, p.ThoiGianKetThuc, p.TrangThaiPhien, h.TrangThaiThanhToan, "
                + "nd.HoTen AS HoTenKH, kg.TenKG, cn.TenCN, p.MaDatCho, dc.KhoangThoiGianSuDung, dc.GhiChu, "
                + "NVL(dc.TongTienGoc, 0) AS DcTongTienGoc, "
                + "NVL(NULLIF(dc.ThanhTienSauGiam, 0), NVL(dc.ThanhTien, 0)) AS DcThanhTienSauGiam, "
                + "dc.MaPGG AS DcMaPGG, dc.MaChuSoPGG AS DcMaChuSoPGG, "
                + "NVL(dc.TienGiamVoucher, 0) AS DcTienGiamVoucher, "
                + "NVL(dc.PhanTramGiamHangTV, 0) AS DcPhanTramGiamHangTV, "
                + "NVL(dc.TienGiamHangTV, 0) AS DcTienGiamHangTV, "
                + "NVL(lkg.DonGiaTheoGio, 0) AS DonGiaTheoGio, "
                + "htv.TenHangThanhVien, NVL(htv.PhanTramTienGiam, 0) AS PhanTramGiamHangThanhVien, "
                + "NVL(h.DaTraTruoc, 0) AS SoTienDaTraTruoc, "
                + "FN_TinhTienDichVu(p.MaPhien) AS TienDichVuTinh, "
                + "FN_TinhTongTien(p.MaPhien) AS TongTienTinh "
                + "FROM HOADON h "
                + "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien "
                + "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH "
                + "LEFT JOIN HANGTHANHVIEN htv ON kh.MaHangThanhVien = htv.MaHangThanhVien "
                + "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND "
                + "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG "
                + "LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG "
                + "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN "
                + "LEFT JOIN PHIEUGIAMGIA pgg ON h.MaPGG = pgg.MaPGG "
                + "LEFT JOIN PHIEUGIAMGIA pggdt ON h.MaPGGDatTruoc = pggdt.MaPGG "
                + "LEFT JOIN PHIEUGIAMGIA pggtq ON h.MaPGGTaiQuay = pggtq.MaPGG "
                + "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho "
                + "WHERE h.MaHoaDon = ?";

        String sqlDichVu = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                "FROM CHITIETDICHVU ct " +
                "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                "WHERE ct.MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement psChung = conn.prepareStatement(sqlChung)) {
            psChung.setString(1, maHoaDon);
            try (ResultSet rsChung = psChung.executeQuery()) {
                if (rsChung.next()) {
                    thongTin = new ThongTinHoaDonDTO();
                    thongTin.setMaHoaDon(rsChung.getString("MaHoaDon"));
                    thongTin.setHoTenKH(rsChung.getString("HoTenKH"));
                    thongTin.setTenKhongGian(rsChung.getString("TenKG"));
                    thongTin.setTenChiNhanh(rsChung.getString("TenCN"));
                    thongTin.setPhuongThucThanhToan(rsChung.getString("PhuongThucThanhToan"));
                    
                    Timestamp ngayLap = rsChung.getTimestamp("NgayLapHoaDon");
                    if (ngayLap != null) {
                        thongTin.setNgayLapHoaDon(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ngayLap));
                    }

                    Double thanhTienLuu = getNullableDouble(rsChung, "ThanhTienLuu");
                    String maDatCho = rsChung.getString("MaDatCho");
                    boolean laDatTruoc = maDatCho != null && !maDatCho.trim().isEmpty();
                    double soGioDatTruoc = rsChung.getDouble("KhoangThoiGianSuDung");
                    double donGiaTheoGio = rsChung.getDouble("DonGiaTheoGio");
                    double tienDichVuTinh = Math.max(0, rsChung.getDouble("TienDichVuTinh"));
                    double tongTienTinh = Math.max(0, rsChung.getDouble("TongTienTinh"));

                    double tienGocDatTruoc = Math.max(0, rsChung.getDouble("TienGocDatTruoc"));
                    if (tienGocDatTruoc <= 0 && laDatTruoc) {
                        tienGocDatTruoc = Math.max(0, rsChung.getDouble("DcTongTienGoc"));
                    }
                    if (tienGocDatTruoc <= 0 && laDatTruoc) {
                        tienGocDatTruoc = Math.max(0, donGiaTheoGio * soGioDatTruoc);
                    }

                    double tienGocPhatSinh = Math.max(0, rsChung.getDouble("TienGocPhatSinh"));
                    if (tienGocPhatSinh <= 0) {
                        tienGocPhatSinh = laDatTruoc ? tienDichVuTinh : tongTienTinh;
                    }

                    double tongTienGoc = Math.max(0, rsChung.getDouble("TongTienGocLuu"));
                    if (tongTienGoc <= 0) {
                        tongTienGoc = laDatTruoc ? tienGocDatTruoc + tienGocPhatSinh : tongTienTinh;
                    }
                    if (tongTienGoc <= 0) {
                        tongTienGoc = Math.max(0, rsChung.getDouble("TongTienLuu"));
                    }

                    double soTienDaTraTruoc = Math.max(0, rsChung.getDouble("SoTienDaTraTruoc"));
                    if (soTienDaTraTruoc <= 0 && laDatTruoc) {
                        soTienDaTraTruoc = Math.max(0, rsChung.getDouble("DcThanhTienSauGiam"));
                    }

                    String maPGGDatTruoc = layGiaTriDauTien(rsChung.getString("MaPGGDatTruoc"), rsChung.getString("DcMaPGG"));
                    String maChuSoPGGDatTruoc = layGiaTriDauTien(rsChung.getString("MaChuSoPGGDatTruoc"), rsChung.getString("DcMaChuSoPGG"));
                    double tienGiamVoucherDatTruoc = Math.max(0, rsChung.getDouble("TienGiamVoucherDatTruoc"));
                    if (tienGiamVoucherDatTruoc <= 0) {
                        tienGiamVoucherDatTruoc = Math.max(0, rsChung.getDouble("DcTienGiamVoucher"));
                    }
                    double phanTramGiamHangTVDatTruoc = Math.max(0, rsChung.getDouble("PhanTramGiamHangTVDatTruoc"));
                    if (phanTramGiamHangTVDatTruoc <= 0) {
                        phanTramGiamHangTVDatTruoc = Math.max(0, rsChung.getDouble("DcPhanTramGiamHangTV"));
                    }
                    double tienGiamHangTVDatTruoc = Math.max(0, rsChung.getDouble("TienGiamHangTVDatTruoc"));
                    if (tienGiamHangTVDatTruoc <= 0) {
                        tienGiamHangTVDatTruoc = Math.max(0, rsChung.getDouble("DcTienGiamHangTV"));
                    }

                    String maPGGTaiQuay = layGiaTriDauTien(rsChung.getString("MaPGGTaiQuay"),
                            laDatTruoc ? null : rsChung.getString("MaPGG"));
                    String maChuSoPGGTaiQuay = layGiaTriDauTien(rsChung.getString("MaChuSoPGGTaiQuay"),
                            laDatTruoc ? null : rsChung.getString("MaChuSoPGG"));
                    double tienGiamVoucherTaiQuay = Math.max(0, rsChung.getDouble("TienGiamVoucherTaiQuay"));
                    if (tienGiamVoucherTaiQuay <= 0 && maPGGTaiQuay != null && !maPGGTaiQuay.isBlank()) {
                        double giaTriVoucherTaiQuay = Math.max(0, rsChung.getDouble("GiaTriGiamVoucherTaiQuay"));
                        if (giaTriVoucherTaiQuay <= 0) {
                            giaTriVoucherTaiQuay = Math.max(0, rsChung.getDouble("GiaTriGiamVoucher"));
                        }
                        tienGiamVoucherTaiQuay = Math.min(giaTriVoucherTaiQuay,
                                Math.max(0, tienGocPhatSinh));
                    }
                    double phanTramGiamHangTVTaiQuay = Math.max(0, rsChung.getDouble("PhanTramGiamHangTVTaiQuay"));
                    if (phanTramGiamHangTVTaiQuay <= 0) {
                        phanTramGiamHangTVTaiQuay = Math.max(0, rsChung.getDouble("PhanTramGiamHangThanhVien"));
                    }
                    double tienGiamHangTVTaiQuay = Math.max(0, rsChung.getDouble("TienGiamHangTVTaiQuay"));
                    if (tienGiamHangTVTaiQuay <= 0 && tienGocPhatSinh > 0 && phanTramGiamHangTVTaiQuay > 0) {
                        tienGiamHangTVTaiQuay = Math.round(Math.max(0, tienGocPhatSinh - tienGiamVoucherTaiQuay)
                                * Math.min(100, phanTramGiamHangTVTaiQuay) / 100.0);
                    }
                    double tongTienGiam = Math.max(0, rsChung.getDouble("TongTienGiamLuu"));
                    if (tongTienGiam <= 0) {
                        tongTienGiam = tienGiamVoucherDatTruoc + tienGiamHangTVDatTruoc
                                + tienGiamVoucherTaiQuay + tienGiamHangTVTaiQuay;
                    }
                    double soTienThanhToanTaiQuay = Math.max(0, rsChung.getDouble("SoTienThanhToanTaiQuay"));
                    double thanh;
                    if (thanhTienLuu != null) {
                        thanh = Math.max(0, thanhTienLuu);
                    } else {
                        thanh = Math.max(0, tongTienGoc - tongTienGiam - soTienDaTraTruoc);
                    }

                    thongTin.setTongTienGoc(tongTienGoc);
                    thongTin.setTongTien(tongTienGoc);
                    thongTin.setThanhTien(thanh);
                    thongTin.setMaPhien(rsChung.getString("MaPhien"));
                    thongTin.setTrangThaiPhien(rsChung.getString("TrangThaiPhien"));
                    thongTin.setTrangThaiThanhToan(rsChung.getString("TrangThaiThanhToan"));
                    thongTin.setSoTienDaTraTruoc(soTienDaTraTruoc);
                    thongTin.setDaTraTruoc(soTienDaTraTruoc > 0);
                    thongTin.setTienGocDatTruoc(tienGocDatTruoc);
                    thongTin.setTienGocPhatSinh(tienGocPhatSinh);
                    thongTin.setMaPGGDatTruoc(maPGGDatTruoc);
                    thongTin.setMaChuSoPGGDatTruoc(maChuSoPGGDatTruoc);
                    thongTin.setTienGiamVoucherDatTruoc(tienGiamVoucherDatTruoc);
                    thongTin.setPhanTramGiamHangTVDatTruoc(phanTramGiamHangTVDatTruoc);
                    thongTin.setTienGiamHangTVDatTruoc(tienGiamHangTVDatTruoc);
                    thongTin.setMaPGGTaiQuay(maPGGTaiQuay);
                    thongTin.setMaChuSoPGGTaiQuay(maChuSoPGGTaiQuay);
                    thongTin.setTienGiamVoucherTaiQuay(tienGiamVoucherTaiQuay);
                    thongTin.setPhanTramGiamHangTVTaiQuay(phanTramGiamHangTVTaiQuay);
                    thongTin.setTienGiamHangTVTaiQuay(tienGiamHangTVTaiQuay);
                    thongTin.setSoTienThanhToanTaiQuay(soTienThanhToanTaiQuay);
                    thongTin.setMaPGG(layGiaTriDauTien(maPGGTaiQuay, maPGGDatTruoc, rsChung.getString("MaPGG")));
                    thongTin.setMaChuSoPGG(layGiaTriDauTien(maChuSoPGGTaiQuay, maChuSoPGGDatTruoc, rsChung.getString("MaChuSoPGG")));
                    thongTin.setMaVoucher(layGiaTriDauTien(maChuSoPGGTaiQuay, maPGGTaiQuay, maChuSoPGGDatTruoc, maPGGDatTruoc));
                    thongTin.setTenHangThanhVien(rsChung.getString("TenHangThanhVien"));
                    thongTin.setPhanTramGiamHangThanhVien(Math.max(phanTramGiamHangTVDatTruoc, phanTramGiamHangTVTaiQuay));
                    thongTin.setSoTienGiamVoucher(tienGiamVoucherDatTruoc + tienGiamVoucherTaiQuay);
                    thongTin.setSoTienGiamHangThanhVien(tienGiamHangTVDatTruoc + tienGiamHangTVTaiQuay);
                    thongTin.setTongTienGiam(tongTienGiam);

                    double conPhaiThanhToanTinh = Math.max(0, tongTienGoc - tongTienGiam
                            - soTienDaTraTruoc - soTienThanhToanTaiQuay);
                    if (Math.abs(conPhaiThanhToanTinh - thanh) > 1) {
                        System.out.println("[HoaDonDAO] Chenh lech thanh tien hoa don " + maHoaDon
                                + ": DB=" + thanh + ", tinhLai=" + conPhaiThanhToanTinh);
                    }

                    Timestamp tBD = rsChung.getTimestamp("ThoiGianBatDau");
                    Timestamp tKT = rsChung.getTimestamp("ThoiGianKetThuc");

                    if (tBD != null && tKT != null) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        thongTin.setThoiGianSửDung(timeFormat.format(tBD) + " - " + timeFormat.format(tKT) + " ("
                                + dateFormat.format(tBD) + ")");

                        long diffMillis = tKT.getTime() - tBD.getTime();
                        long totalMinutes = diffMillis / 60000;
                        long hours = totalMinutes / 60;
                        long minutes = totalMinutes % 60;
                        long roundedHours = (minutes < 15) ? hours : hours + 1;

                        thongTin.setTongSoGio(roundedHours);
                    }

                    try (PreparedStatement psDV = conn.prepareStatement(sqlDichVu)) {
                        psDV.setString(1, thongTin.getMaPhien());
                        try (ResultSet rsDV = psDV.executeQuery()) {
                            while (rsDV.next()) {
                                thongTin.getDanhSachDichVu().add(new DichVuDaDungDTO(
                                        rsDV.getString("TenDV"),
                                        rsDV.getInt("SoLuong"),
                                        rsDV.getDouble("DonGia")));
                                thongTin.getDanhSachDichVuPhatSinh().add(new DichVuDaDungDTO(
                                        rsDV.getString("TenDV"),
                                        rsDV.getInt("SoLuong"),
                                        rsDV.getDouble("DonGia")));
                            }
                        }
                    }

                    String tenKGDisplay = thongTin.getTenKhongGian();
                    double soGioDisplay = thongTin.getTongSoGio();
                    double tienKhongGian = Math.max(0, tongTienGoc - tienDichVuTinh);

                    if (laDatTruoc) {
                        tenKGDisplay = "Thuê " + tenKGDisplay + " (đã đặt trước)";
                        soGioDisplay = soGioDatTruoc;
                        tienKhongGian = tienGocDatTruoc;
                    } else {
                        tenKGDisplay = "Thuê " + tenKGDisplay;
                    }

                    double donGiaKg = (soGioDisplay > 0) ? (tienKhongGian / soGioDisplay) : tienKhongGian;
                            
                    thongTin.getDanhSachDichVu().add(0, new DichVuDaDungDTO(
                            tenKGDisplay,
                            (int) Math.max(0, Math.round(soGioDisplay)),
                            donGiaKg,
                            tienKhongGian));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logElapsed("load chi tiet hoa don", start);
        }
        return thongTin;
    }

    public boolean xacNhanThanhToan(String maHoaDon, String phuongThucThanhToan, String maNV, String maPGG,
            double thanhTien) {
        String sql = "UPDATE HOADON SET PhuongThucThanhToan = ?, MaNV = ?, MaPGG = ?, MaPGGTaiQuay = ?, "
                + "SoTienThanhToanTaiQuay = ?, ThanhTien = 0, "
                + "TrangThaiThanhToan = 'Đã thanh toán thành công', NgayLapHoaDon = CURRENT_TIMESTAMP "
                + "WHERE MaHoaDon = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phuongThucThanhToan);
            pstmt.setString(2, maNV);
            pstmt.setString(3, maPGG);
            pstmt.setString(4, maPGG);
            pstmt.setDouble(5, Math.max(0, thanhTien));
            pstmt.setString(6, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean thanhToanVoiPhieuGiamGia(String maPhien, String maNV, String maPGG, String phuongThucThanhToan) {
        String sql = "{call SP_ThanhToanVoiPhieuGiamGia(?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, maPhien);
            cstmt.setString(2, maNV);
            if (maPGG == null || maPGG.trim().isEmpty()) {
                cstmt.setNull(3, Types.VARCHAR);
            } else {
                cstmt.setString(3, maPGG.trim());
            }
            cstmt.setString(4, phuongThucThanhToan);
            cstmt.registerOutParameter(5, Types.VARCHAR);
            cstmt.execute();

            String message = cstmt.getString(5);
            System.out.println("[HoaDonDAO] " + message);
            return laThongBaoThanhCong(message);
        } catch (Exception e) {
            System.err.println("[HoaDonDAO] Lỗi gọi SP_ThanhToanVoiPhieuGiamGia: " + e.getMessage());
            return false;
        }
    }

    private boolean laThongBaoDaTraTruoc(String message) {
        if (message == null) {
            return false;
        }
        return message.contains("đã được thanh toán trước qua đặt chỗ");
    }

    private boolean laThongBaoThanhCong(String message) {
        if (message == null) {
            return false;
        }
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains("thanh toan thanh cong");
    }

    public boolean huyHoaDon(String maHoaDon) {
        String sql = "UPDATE HOADON SET TrangThaiThanhToan = 'Thanh toán không thành công' WHERE MaHoaDon = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaHoaDon(String maHoaDon) {
        String sql = "DELETE FROM HOADON WHERE MaHoaDon = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public KetQuaThanhToanDTO thanhToanVoiPhieuGiamGiaMoi(String maPhien, String maNV, String maPGG, String phuongThucThanhToan) {
        long start = System.currentTimeMillis();
        String sql = "{call SP_ThanhToanVoiPhieuGiamGia(?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
             
            cstmt.setString(1, maPhien);
            cstmt.setString(2, maNV);
            if (maPGG == null || maPGG.trim().isEmpty()) {
                cstmt.setNull(3, Types.VARCHAR);
            } else {
                cstmt.setString(3, maPGG.trim());
            }
            cstmt.setString(4, phuongThucThanhToan);
            cstmt.registerOutParameter(5, Types.VARCHAR);
            
            cstmt.execute();
            String message = cstmt.getString(5);
            System.out.println("[HoaDonDAO] SP_ThanhToanVoiPhieuGiamGia: " + message);
            
            if (!laThongBaoThanhCong(message) || laThongBaoLoi(message)) {
                return new KetQuaThanhToanDTO(false, message != null ? message : "Lỗi khi cập nhật thanh toán");
            }
            
            return new KetQuaThanhToanDTO(true, message != null ? message : "Thanh toán thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return new KetQuaThanhToanDTO(false, "Lỗi khi cập nhật thanh toán: " + e.getMessage());
        } finally {
            logElapsed("thanh toan SP", start);
        }
    }

    public KetQuaThanhToanDTO thanhToanTrucTiepMoi(String maHoaDon, String phuongThucThanhToan, String maNV, String maPGG, double thanhTien) {
        // Fallback implementation if SP is not used
        boolean success = xacNhanThanhToan(maHoaDon, phuongThucThanhToan, maNV, maPGG, thanhTien);
        if (success) {
            return new KetQuaThanhToanDTO(true, "Thanh toán thành công");
        } else {
            return new KetQuaThanhToanDTO(false, "Lỗi khi cập nhật thanh toán");
        }
    }

    private boolean laThongBaoLoi(String message) {
        if (message == null) {
            return false;
        }
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains("loi")
                || normalized.contains("du lieu loi")
                || normalized.contains("khong tim thay");
    }

    private void logElapsed(String action, long start) {
        System.out.println("[HoaDonDAO] " + action + " mat " + (System.currentTimeMillis() - start) + " ms");
    }

    public String timMaHoaDonTheoMaPhien(String maPhien) {
        if (maPhien == null || maPhien.isBlank()) {
            return null;
        }
        String sql = "SELECT MaHoaDon FROM HOADON WHERE MaPhien = ? FETCH FIRST 1 ROWS ONLY";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhien.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("MaHoaDon") : null;
            }
        } catch (Exception e) {
            System.err.println("[HoaDonDAO] Lỗi tìm hóa đơn theo phiên: " + e.getMessage());
            return null;
        }
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private String layGiaTriDauTien(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
