package com.wms.dao.TrangChuQuanLy.QuanLyPhien;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.KetQuaNhanChoDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.MaTuDongUtil;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhienLamViecDAO {

    public String generateNextMaPhien() throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return MaTuDongUtil.sinhMaTiepTheo(conn, MaTuDongUtil.MaDoiTuong.PHIEN_LAM_VIEC);
        }
    }

    public boolean taoPhienLamViecMoi(PhienLamViecDTO phien) {
        if (phien.getMaPhien() == null || phien.getMaPhien().isEmpty()) {
            try {
                phien.setMaPhien(generateNextMaPhien());
            } catch (SQLException e) {
                System.err.println("[PhienLamViecDAO] Lỗi sinh mã phiên: " + e.getMessage());
                return false;
            }
        }

        String sql = "{call sp_MoPhienLamViecTrucTiep(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, phien.getMaKG());
            cstmt.setString(2, phien.getMaKH());
            cstmt.setTimestamp(3, phien.getThoiGianDuKienKetThuc());
            cstmt.setString(4, phien.getMaPhien());
            cstmt.setString(5, phien.getMaDatCho());
            cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);

            cstmt.execute();

            String message = cstmt.getString(6);
            if (laThongBaoThanhCong(message)) {
                return true;
            } else {
                System.err.println("[PhienLamViecDAO] Lỗi từ SP: " + message);
                return false;
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi khi gọi SP tạo phiên: " + e.getMessage());
            return false;
        }
    }

    public KetQuaNhanChoDTO moPhienTuQrDatCho(String maDatCho, String noiDungQr) {
        if (maDatCho == null || maDatCho.isBlank() || noiDungQr == null || noiDungQr.isBlank()) {
            return new KetQuaNhanChoDTO(false, "Vui lòng nhập mã QR nhận chỗ hợp lệ.");
        }

        String bookingSql = """
                SELECT dc.MaDatCho, dc.MaQR, dc.TrangThaiDatTruoc, dc.KhoangThoiGianSuDung,
                       NVL(dc.ThanhTien, 0) AS ThanhTien, dc.MaKG, dc.MaKH
                FROM DATCHO dc
                WHERE dc.MaDatCho = ?
                FOR UPDATE
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                String qrTrongDb;
                String trangThai;
                int soGio;
                double thanhTien;
                String maKG;
                String maKH;

                try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
                    ps.setString(1, maDatCho.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new KetQuaNhanChoDTO(false, "Không tìm thấy đặt chỗ " + maDatCho + ".");
                        }
                        qrTrongDb = rs.getString("MaQR");
                        trangThai = rs.getString("TrangThaiDatTruoc");
                        soGio = Math.max(1, rs.getInt("KhoangThoiGianSuDung"));
                        thanhTien = rs.getDouble("ThanhTien");
                        maKG = rs.getString("MaKG");
                        maKH = rs.getString("MaKH");
                    }
                }

                if (qrTrongDb == null || qrTrongDb.isBlank()) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Mã QR đã bị vô hiệu hoặc đặt chỗ chưa thanh toán.");
                }
                if (!qrTrongDb.trim().equals(noiDungQr.trim())) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Mã QR không khớp với đặt chỗ trong hệ thống.");
                }
                if (!chuanHoa(trangThai).contains("thanh toan thanh cong")) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Đặt chỗ chưa ở trạng thái đã thanh toán thành công.");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM PHIENLAMVIEC WHERE MaDatCho = ?")) {
                    ps.setString(1, maDatCho.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            return new KetQuaNhanChoDTO(false, "Đặt chỗ này đã được nhận trước đó.");
                        }
                    }
                }

                String maPhien = MaTuDongUtil.sinhMaTiepTheo(conn, MaTuDongUtil.MaDoiTuong.PHIEN_LAM_VIEC);
                String maHoaDon = MaTuDongUtil.sinhMaTiepTheo(conn, MaTuDongUtil.MaDoiTuong.HOA_DON);

                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO PHIENLAMVIEC
                            (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien,
                             CapNhatLanCuoi, MaKG, MaKH, MaDatCho)
                        VALUES (?, CURRENT_TIMESTAMP,
                                CURRENT_TIMESTAMP + NUMTODSINTERVAL(?, 'HOUR'),
                                ?, CURRENT_TIMESTAMP, ?, ?, ?)
                        """)) {
                    ps.setString(1, maPhien);
                    ps.setInt(2, soGio);
                    ps.setString(3, "Đã đặt trước");
                    ps.setString(4, maKG);
                    ps.setString(5, maKH);
                    ps.setString(6, maDatCho.trim());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE PHIENLAMVIEC SET TrangThaiPhien = 'Đang hoạt động', CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaPhien = ?")) {
                    ps.setString(1, maPhien);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO HOADON
                            (MaHoaDon, SoHD, TongTien, ThanhTien, NgayLapHoaDon,
                             PhuongThucThanhToan, TrangThaiThanhToan, MaPhien)
                        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)
                        """)) {
                    ps.setString(1, maHoaDon);
                    ps.setString(2, maHoaDon);
                    ps.setDouble(3, thanhTien);
                    ps.setDouble(4, thanhTien);
                    ps.setString(5, "Chuyển khoản");
                    ps.setString(6, "Đã thanh toán thành công");
                    ps.setString(7, maPhien);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE DATCHO
                        SET TrangThaiDatTruoc = 'Đã sử dụng',
                            MaQR = NULL,
                            CapNhatLanCuoi = CURRENT_TIMESTAMP
                        WHERE MaDatCho = ?
                        """)) {
                    ps.setString(1, maDatCho.trim());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE KHONGGIAN SET TrangThaiKG = 'Đang hoạt động' WHERE MaKG = ?")) {
                    ps.setString(1, maKG);
                    ps.executeUpdate();
                }

                conn.commit();
                return new KetQuaNhanChoDTO(true, "Mở phiên từ vé đặt chỗ thành công. Mã phiên: " + maPhien,
                        maDatCho.trim(), maPhien);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi mở phiên từ QR đặt chỗ: " + e.getMessage());
            return new KetQuaNhanChoDTO(false, "Không thể mở phiên từ QR: " + e.getMessage());
        }
    }

    private boolean laThongBaoThanhCong(String message) {
        if (message == null) {
            return false;
        }
        return chuanHoa(message).contains("thanh cong");
    }

    private String chuanHoa(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public List<PhienLamViecFullDTO> layDanhSachPhien(String keyword, String maCN) {
        List<PhienLamViecFullDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, kg.TenKG, nd.HoTen AS HoTenKH, dc.TrangThaiDatTruoc, h.TrangThaiThanhToan, lkg.DonGiaTheoGio "
                        +
                        "FROM PHIENLAMVIEC p " +
                        "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                        "JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                        "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                        "JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND " +
                        "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho " +
                        "LEFT JOIN HOADON h ON p.MaPhien = h.MaPhien " +
                        "WHERE (p.MaPhien LIKE ? OR nd.HoTen LIKE ? OR kg.TenKG LIKE ?)");

        if (maCN != null && !maCN.isEmpty()) {
            sql.append(" AND kg.MaCN = ?");
        }
        sql.append(" ORDER BY p.ThoiGianBatDau DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            String search = "%" + (keyword == null ? "" : keyword) + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);
            pstmt.setString(3, search);
            if (maCN != null && !maCN.isEmpty()) {
                pstmt.setString(4, maCN);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PhienLamViecFullDTO dto = new PhienLamViecFullDTO();
                    dto.setMaPhien(rs.getString("MaPhien"));
                    dto.setThoiGianBatDau(rs.getTimestamp("ThoiGianBatDau"));
                    dto.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                    dto.setThoiGianKetThuc(rs.getTimestamp("ThoiGianKetThuc"));
                    dto.setTrangThaiPhien(rs.getString("TrangThaiPhien"));
                    dto.setMaKG(rs.getString("MaKG"));
                    dto.setMaKH(rs.getString("MaKH"));
                    dto.setMaDatCho(rs.getString("MaDatCho"));
                    dto.setDonGiaTheoGio(rs.getDouble("DonGiaTheoGio"));
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setTenKhachHang(rs.getString("HoTenKH"));
                    dto.setTrangThaiDatCho(rs.getString("TrangThaiDatTruoc"));
                    dto.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    public boolean capNhatPhien(String maPhien, String trangThai, String tenKH) {
        String sqlPhien = "UPDATE PHIENLAMVIEC SET TrangThaiPhien = ? WHERE MaPhien = ?";
        String sqlKH = "UPDATE NGUOIDUNG SET HoTen = ? WHERE MaND = (SELECT MaND FROM KHACHHANG WHERE MaKH = (SELECT MaKH FROM PHIENLAMVIEC WHERE MaPhien = ?))";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPhien)) {
                    pstmt.setString(1, trangThai);
                    pstmt.setString(2, maPhien);
                    pstmt.executeUpdate();
                }
                if (tenKH != null && !tenKH.isEmpty()) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlKH)) {
                        pstmt.setString(1, tenKH);
                        pstmt.setString(2, maPhien);
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi cập nhật phiên: " + e.getMessage());
            return false;
        }
    }

    public boolean ketThucPhien(String maPhien) {
        String sqlPhien = "UPDATE PHIENLAMVIEC SET ThoiGianKetThuc = CURRENT_TIMESTAMP, TrangThaiPhien = 'Đã kết thúc', CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement pstmtPhien = conn.prepareStatement(sqlPhien)) {
                    pstmtPhien.setString(1, maPhien);
                    if (pstmtPhien.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                double tongTien = tinhTongTienPhien(conn, maPhien);

                String sqlHoaDon = "UPDATE HOADON SET TongTien = ?, ThanhTien = ?, NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaPhien = ?";
                try (PreparedStatement pstmtHoaDon = conn.prepareStatement(sqlHoaDon)) {
                    pstmtHoaDon.setDouble(1, tongTien);
                    pstmtHoaDon.setDouble(2, tongTien);
                    pstmtHoaDon.setString(3, maPhien);
                    pstmtHoaDon.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi kết thúc phiên: " + e.getMessage());
            return false;
        }
    }

    private double tinhTongTienPhien(Connection conn, String maPhien) throws SQLException {
        double tienKhongGian = 0;
        double tienDichVu = 0;

        String sqlKg = "SELECT LKG.DonGiaTheoGio, PLV.ThoiGianBatDau, PLV.ThoiGianKetThuc " +
                "FROM PHIENLAMVIEC PLV " +
                "JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG " +
                "JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG " +
                "WHERE PLV.MaPhien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlKg)) {
            ps.setString(1, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double donGia = rs.getDouble("DonGiaTheoGio");
                    Timestamp batDau = rs.getTimestamp("ThoiGianBatDau");
                    Timestamp ketThuc = rs.getTimestamp("ThoiGianKetThuc");
                    if (ketThuc == null)
                        ketThuc = new Timestamp(System.currentTimeMillis());

                    if (batDau != null) {
                        long diffMillis = ketThuc.getTime() - batDau.getTime();
                        long totalMinutes = diffMillis / 60000;
                        long hours = totalMinutes / 60;
                        long minutes = totalMinutes % 60;
                        long roundedHours = (minutes < 15) ? hours : hours + 1;

                        tienKhongGian = donGia * roundedHours;
                    }
                }
            }
        }

        String sqlDv = "SELECT SUM(CT.SoLuong * DV.DonGia) AS TienDV " +
                "FROM CHITIETDICHVU CT " +
                "JOIN DICHVU DV ON CT.MaDV = DV.MaDV " +
                "WHERE CT.MaPhien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDv)) {
            ps.setString(1, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tienDichVu = rs.getDouble("TienDV");
                }
            }
        }

        return Math.round((tienKhongGian + tienDichVu) * 100.0) / 100.0;
    }

    public List<DichVuTrongPhienDTO> layDichVuCuaPhien(String maPhien) {
        List<DichVuTrongPhienDTO> list = new ArrayList<>();
        String sql = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                "FROM CHITIETDICHVU ct " +
                "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                "WHERE ct.MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maPhien);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString("TenDV");
                    int sl = rs.getInt("SoLuong");
                    double dg = rs.getDouble("DonGia");
                    list.add(new DichVuTrongPhienDTO(ten, sl, dg, sl * dg));
                }
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy dịch vụ: " + e.getMessage());
        }
        return list;
    }

    public ThongTinXacNhanDatChoDTO layThongTinXacNhanDatCho(String maDatCho, String maPhien) {
        String sql = """
                SELECT p.MaPhien, dc.MaDatCho, dc.MaQR, nd.HoTen, nd.Email,
                       kg.TenKG, cn.TenCN, p.ThoiGianBatDau, p.ThoiGianDuKienKetThuc,
                       h.ThanhTien
                FROM PHIENLAMVIEC p
                JOIN DATCHO dc ON dc.MaDatCho = p.MaDatCho
                JOIN KHACHHANG kh ON kh.MaKH = p.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = p.MaKG
                JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                LEFT JOIN HOADON h ON h.MaPhien = p.MaPhien
                WHERE dc.MaDatCho = ? AND p.MaPhien = ?
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDatCho);
            ps.setString(2, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ThongTinXacNhanDatChoDTO dto = new ThongTinXacNhanDatChoDTO();
                    dto.setMaPhien(rs.getString("MaPhien"));
                    dto.setMaDatCho(rs.getString("MaDatCho"));
                    dto.setMaQR(rs.getString("MaQR"));
                    dto.setHoTen(rs.getString("HoTen"));
                    dto.setEmail(rs.getString("Email"));
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setTenChiNhanh(rs.getString("TenCN"));
                    dto.setThoiGianBatDau(rs.getTimestamp("ThoiGianBatDau"));
                    dto.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                    dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
                    return dto;
                }
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy thông tin xác nhận đặt chỗ: " + e.getMessage());
        }
        return null;
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM PHIENLAMVIEC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    public boolean xoaPhien(String maPhien) {
        String sqlChiTiet = "DELETE FROM CHITIETDICHVU WHERE PhienLamViec = ?";
        if (maPhien != null) {
            // we will run standard queries here
        }
        String sqlChiTietReal = "DELETE FROM CHITIETDICHVU WHERE MaPhien = ?";
        String sqlHoaDon = "DELETE FROM HOADON WHERE MaPhien = ?";
        String sqlPhien = "DELETE FROM PHIENLAMVIEC WHERE MaPhien = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sqlChiTietReal)) {
                    ps.setString(1, maPhien);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                    ps.setString(1, maPhien);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlPhien)) {
                    ps.setString(1, maPhien);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[PhienLamViecDAO] Lỗi khi xóa phiên: " + e.getMessage());
            return false;
        }
    }
}
