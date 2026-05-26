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
        String sqlUpdate = "UPDATE HOADON SET TongTien = ?, ThanhTien = ? WHERE MaPhien = ?";
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
            psUpdate.setDouble(2, thanhTien);
            psUpdate.setString(3, maPhien);
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
        String sqlChung = "SELECT h.MaHoaDon, h.NgayLapHoaDon, h.PhuongThucThanhToan, " +
            "h.TongTien AS TongTienLuu, h.ThanhTien AS ThanhTienLuu, " +
            "FN_TinhThanhTien(p.MaPhien, h.MaPGG) AS TongTienSauGiam, " +
            "p.MaPhien, p.ThoiGianBatDau, p.ThoiGianKetThuc, p.TrangThaiPhien, h.TrangThaiThanhToan, " +
            "nd.HoTen AS HoTenKH, kg.TenKG, cn.TenCN, p.MaDatCho, dc.KhoangThoiGianSuDung, "
            + "NVL(h.DaTraTruoc, 0) AS SoTienDaTraTruoc, "
            + "NVL(h.DaTraTruoc, 0) AS DaTraTruoc, "
            + "FN_TinhTienKhongGian(p.MaPhien) AS TienKhongGian, "
            + "FN_TinhTongTien(p.MaPhien) AS TongTienGoc " +
                "FROM HOADON h " +
                "LEFT JOIN PHIENLAMVIEC p ON h.MaPhien = p.MaPhien " +
                "LEFT JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                "LEFT JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho " +
                "WHERE h.MaHoaDon = ?";

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

                    Double tongTienLuu = getNullableDouble(rsChung, "TongTienLuu");
                    Double thanhTienLuu = getNullableDouble(rsChung, "ThanhTienLuu");
                    double tongTienGoc = rsChung.getDouble("TongTienGoc");
                    double tt = tongTienLuu != null && tongTienLuu > 0 ? tongTienLuu : tongTienGoc;

                    double soTienDaTraTruoc = rsChung.getDouble("SoTienDaTraTruoc");
                    double thanh;
                    if (thanhTienLuu != null) {
                        thanh = Math.max(0, thanhTienLuu);
                    } else {
                        double tongTienSauGiam = rsChung.getDouble("TongTienSauGiam");
                        if (tongTienSauGiam <= 0 && tt > 0) {
                            tongTienSauGiam = tt;
                        }
                        thanh = Math.max(0, tongTienSauGiam - soTienDaTraTruoc);
                    }
                    if ("Đã thanh toán thành công".equals(rsChung.getString("TrangThaiThanhToan"))
                            && soTienDaTraTruoc >= tt) {
                        thanh = 0;
                    }

                    thongTin.setTongTien(tt);
                    thongTin.setThanhTien(thanh);
                    thongTin.setMaPhien(rsChung.getString("MaPhien"));
                    thongTin.setTrangThaiPhien(rsChung.getString("TrangThaiPhien"));
                    thongTin.setTrangThaiThanhToan(rsChung.getString("TrangThaiThanhToan"));
                    thongTin.setSoTienDaTraTruoc(soTienDaTraTruoc);
                    thongTin.setDaTraTruoc(soTienDaTraTruoc > 0);

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

                    double tienKhongGian = rsChung.getDouble("TienKhongGian");
                    if (tienKhongGian <= 0) {
                        double tongTienDichVu = 0;
                        for (DichVuDaDungDTO dv : thongTin.getDanhSachDichVu()) {
                            tongTienDichVu += dv.getThanhTien();
                        }
                        tienKhongGian = Math.max(0, thongTin.getTongTien() - tongTienDichVu);
                    }
                    
                    String tenKGDisplay = thongTin.getTenKhongGian();
                    double soGioDisplay = thongTin.getTongSoGio();
                    String maDatCho = rsChung.getString("MaDatCho");
                    
                    if (maDatCho != null && !maDatCho.trim().isEmpty()) {
                        tenKGDisplay = "Thuê " + tenKGDisplay + " (đã đặt trước)";
                        soGioDisplay = rsChung.getDouble("KhoangThoiGianSuDung");
                    } else {
                        tenKGDisplay = "Thuê " + tenKGDisplay;
                    }

                    double donGiaKg = (soGioDisplay > 0)
                            ? (tienKhongGian / soGioDisplay)
                            : tienKhongGian;
                            
                    thongTin.getDanhSachDichVu().add(0, new DichVuDaDungDTO(
                            tenKGDisplay,
                            (int) soGioDisplay,
                            donGiaKg));
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
        String sql = "UPDATE HOADON SET PhuongThucThanhToan = ?, MaNV = ?, MaPGG = ?, ThanhTien = ?, TrangThaiThanhToan = 'Đã thanh toán thành công', NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaHoaDon = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phuongThucThanhToan);
            pstmt.setString(2, maNV);
            pstmt.setString(3, maPGG);
            pstmt.setDouble(4, thanhTien);
            pstmt.setString(5, maHoaDon);
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
            if (laThongBaoDaTraTruoc(message)) {
                throw new IllegalStateException(message);
            }
            return laThongBaoThanhCong(message);
        } catch (IllegalStateException e) {
            throw e;
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
}
