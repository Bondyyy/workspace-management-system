package com.wms.dao.TrangChuQuanLy.QuanLyHoaDon;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.HoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.ThongTinHoaDonDTO;
import com.wms.model.TrangChuQuanLy.QuanLyHoaDon.DichVuDaDungDTO;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    private static final long MILLIS_IN_HOUR = 3600000;

    public List<HoaDonDTO> layDanhSachHoaDon(String searchText, String statusFilter) {
        List<HoaDonDTO> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return list;

        StringBuilder sql = new StringBuilder(
                "SELECT h.*, nd.HoTen AS HoTenKH, p.MaDatCho, p.TrangThaiPhien, p.ThoiGianBatDau, p.ThoiGianKetThuc, p.ThoiGianDuKienKetThuc "
                        +
                        "FROM HOADON h " +
                        "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                        "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "WHERE 1=1 ");

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append("AND (h.MaHoaDon LIKE ? OR nd.HoTen LIKE ? OR h.SoHD LIKE ?) ");
        }
        if (statusFilter != null && !statusFilter.equals("Tất cả")) {
            if (statusFilter.equals("Chưa thanh toán")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đang chờ thanh toán' ");
            } else if (statusFilter.equals("Đã thanh toán")) {
                sql.append("AND h.TrangThaiThanhToan = 'Đã thanh toán thành công' ");
            } else {
                sql.append("AND h.TrangThaiThanhToan = ? ");
            }
        }
        sql.append("ORDER BY h.NgayLapHoaDon DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String q = "%" + searchText.trim() + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
            }
            if (statusFilter != null && !statusFilter.equals("Tất cả") && !statusFilter.equals("Chưa thanh toán")) {
                ps.setString(idx++, statusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDonDTO hd = new HoaDonDTO();
                    hd.setMaHoaDon(rs.getString("MaHoaDon"));
                    hd.setSoHD(rs.getString("SoHD"));

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean taoHoaDonMoi(HoaDonDTO hd) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;

        String sql = "INSERT INTO HOADON (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon, TrangThaiThanhToan, MaPhien) "
                +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hd.getMaHoaDon());
            pstmt.setString(2, hd.getSoHD());
            pstmt.setDouble(3, hd.getTongTien());
            pstmt.setDouble(4, hd.getThanhTien());
            pstmt.setString(5, "Đang chờ thanh toán");
            pstmt.setString(6, hd.getMaPhien());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatTrangThaiThanhToanTheoPhien(String maPhien, String trangThai) {
        String sql = "UPDATE HOADON SET TrangThaiThanhToan = ?, NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaPhien = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trangThai);
            pstmt.setString(2, maPhien);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public ThongTinHoaDonDTO layThongTinChiTietHoaDon(String maHoaDon) {
        ThongTinHoaDonDTO thongTin = null;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return null;

        String sqlChung = "SELECT h.MaHoaDon, h.TongTien, h.ThanhTien, p.MaPhien, " +
                "p.ThoiGianBatDau, p.ThoiGianKetThuc, p.TrangThaiPhien, " +
                "nd.HoTen AS HoTenKH, kg.TenKG " +
                "FROM HOADON h " +
                "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                "WHERE h.MaHoaDon = ?";

        String sqlDichVu = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                "FROM CHITIETDICHVU ct " +
                "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                "WHERE ct.MaPhien = ?";

        try (PreparedStatement psChung = conn.prepareStatement(sqlChung)) {
            psChung.setString(1, maHoaDon);
            try (ResultSet rsChung = psChung.executeQuery()) {
                if (rsChung.next()) {
                    thongTin = new ThongTinHoaDonDTO();
                    thongTin.setMaHoaDon(rsChung.getString("MaHoaDon"));
                    thongTin.setHoTenKH(rsChung.getString("HoTenKH"));
                    thongTin.setTenKhongGian(rsChung.getString("TenKG"));

                    double tt = rsChung.getDouble("TongTien");
                    double thanh = rsChung.getDouble("ThanhTien");
                    if (tt == 0 && thanh > 0)
                        tt = thanh;

                    thongTin.setTongTien(tt);
                    thongTin.setThanhTien(thanh);
                    thongTin.setMaPhien(rsChung.getString("MaPhien"));
                    thongTin.setTrangThaiPhien(rsChung.getString("TrangThaiPhien"));

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
                            }
                        }
                    }

                    // Tính toán tiền dịch vụ đã load
                    double tongTienDichVu = 0;
                    for (DichVuDaDungDTO dv : thongTin.getDanhSachDichVu()) {
                        tongTienDichVu += dv.getThanhTien();
                    }

                    // Phần còn lại chính là tiền thuê không gian
                    double tienKhongGian = thongTin.getTongTien() - tongTienDichVu;
                    if (tienKhongGian > 0) {
                        double donGiaKg = (thongTin.getTongSoGio() > 0) ? (tienKhongGian / thongTin.getTongSoGio()) : tienKhongGian;
                        // Thêm vào đầu danh sách để hiển thị đầu tiên
                        thongTin.getDanhSachDichVu().add(0, new DichVuDaDungDTO(
                                "Thuê " + thongTin.getTenKhongGian(),
                                (int) thongTin.getTongSoGio(),
                                donGiaKg));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return thongTin;
    }

    public boolean xacNhanThanhToan(String maHoaDon, String phuongThucThanhToan, String maNV, String maPGG,
            double thanhTien) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;

        String sql = "UPDATE HOADON SET PhuongThucThanhToan = ?, MaNV = ?, MaPGG = ?, ThanhTien = ?, TrangThaiThanhToan = 'Đã thanh toán thành công', NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaHoaDon = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phuongThucThanhToan);
            pstmt.setString(2, maNV);
            pstmt.setString(3, maPGG);
            pstmt.setDouble(4, thanhTien);
            pstmt.setString(5, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean huyHoaDon(String maHoaDon) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;
        // Vì SQL không có trạng thái 'Đã hủy', tạm thời chuyển về 'Thanh toán không
        // thành công' hoặc xử lý khác
        String sql = "UPDATE HOADON SET TrangThaiThanhToan = 'Thanh toán không thành công' WHERE MaHoaDon = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaHoaDon(String maHoaDon) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null)
            return false;

        String sql = "DELETE FROM HOADON WHERE MaHoaDon = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHoaDon);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
