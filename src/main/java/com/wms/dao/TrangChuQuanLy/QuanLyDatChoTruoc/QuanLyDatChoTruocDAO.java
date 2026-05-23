package com.wms.dao.TrangChuQuanLy.QuanLyDatChoTruoc;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyDatChoTruoc.DatChoTruocDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.KetQuaNhanChoDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.ThongTinXacNhanDatChoDTO;
import com.wms.util.MaQRUtil;
import com.wms.util.MaTuDongUtil;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuanLyDatChoTruocDAO {

    public List<DatChoTruocDTO> layDanhSach(String keyword) {
        List<DatChoTruocDTO> list = new ArrayList<>();
        String search = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = """
                SELECT dc.MaDatCho, dc.MaKH, nd.HoTen, dc.MaKG, kg.TenKG,
                       dc.ThoiGianDuKienToi, dc.KhoangThoiGianSuDung,
                       dc.TrangThaiDatTruoc, dc.ThanhTien, dc.GhiChu
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                WHERE dc.MaDatCho LIKE ?
                   OR dc.MaKH LIKE ?
                   OR nd.HoTen LIKE ?
                   OR kg.TenKG LIKE ?
                ORDER BY dc.ThoiGianDat DESC
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (Exception ex) {
            System.err.println("[QuanLyDatChoTruocDAO] Lỗi tải danh sách đặt chỗ: " + ex.getMessage());
        }
        return list;
    }

    public boolean capNhat(DatChoTruocDTO dto) {
        String sql = """
                UPDATE DATCHO
                SET MaKH = ?,
                    MaKG = ?,
                    ThoiGianDuKienToi = ?,
                    KhoangThoiGianSuDung = ?,
                    ThanhTien = ?,
                    TrangThaiDatTruoc = ?,
                    GhiChu = ?,
                    CapNhatLanCuoi = CURRENT_TIMESTAMP
                WHERE MaDatCho = ?
                """;
        if (dto == null || dto.getMaDatCho() == null || dto.getMaDatCho().isBlank()) {
            return false;
        }
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                String trangThaiHienThi = hienThiTrangThaiDatCho(dto.getTrangThaiDatTruoc());
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, dto.getMaKH());
                    ps.setString(2, dto.getMaKG());
                    ps.setTimestamp(3, dto.getThoiGianDuKienToi());
                    if (dto.getKhoangThoiGianSuDung() == null) {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(4, dto.getKhoangThoiGianSuDung());
                    }
                    ps.setBigDecimal(5, dto.getThanhTien());
                    ps.setString(6, trangThaiDatChoDb(conn, trangThaiHienThi));
                    ps.setString(7, dto.getGhiChu());
                    ps.setString(8, dto.getMaDatCho());
                    int updated = ps.executeUpdate();
                    capNhatTrangThaiKhongGian(conn, dto);
                    conn.commit();
                    return updated > 0;
                }
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                System.err.println("[QuanLyDatChoTruocDAO] Lỗi cập nhật đặt chỗ: " + ex.getMessage());
                return false;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception e) {
            System.err.println("[QuanLyDatChoTruocDAO] Lỗi kết nối CSDL: " + e.getMessage());
            return false;
        }
    }

    public ThongTinXacNhanDatChoDTO xacNhanThanhToanThuCong(String maDatCho) {
        if (maDatCho == null || maDatCho.isBlank()) {
            return null;
        }

        String sql = """
                SELECT dc.MaDatCho, dc.MaQR, dc.TrangThaiDatTruoc, dc.GhiChu,
                       dc.ThoiGianDuKienToi,
                       dc.ThoiGianDuKienToi + NUMTODSINTERVAL(NVL(dc.KhoangThoiGianSuDung, 1), 'HOUR') AS ThoiGianDuKienKetThuc,
                       NVL(dc.ThanhTien, 0) AS ThanhTien, dc.MaKG,
                       nd.HoTen, nd.Email, kg.TenKG, cn.TenCN
                FROM DATCHO dc
                JOIN KHACHHANG kh ON kh.MaKH = dc.MaKH
                JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND
                JOIN KHONGGIAN kg ON kg.MaKG = dc.MaKG
                LEFT JOIN CHINHANH cn ON cn.MaCN = kg.MaCN
                WHERE dc.MaDatCho = ?
                FOR UPDATE
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ThongTinXacNhanDatChoDTO thongTin = new ThongTinXacNhanDatChoDTO();
                String trangThaiHienTai;
                String maQrHienTai;
                String ghiChuHienTai;
                String maKG;

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maDatCho.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return null;
                        }
                        trangThaiHienTai = rs.getString("TrangThaiDatTruoc");
                        maQrHienTai = rs.getString("MaQR");
                        ghiChuHienTai = rs.getString("GhiChu");
                        maKG = rs.getString("MaKG");

                        thongTin.setMaDatCho(rs.getString("MaDatCho"));
                        thongTin.setMaQR(MaQRUtil.taoMaQRDatCho(maDatCho.trim()));
                        thongTin.setHoTen(rs.getString("HoTen"));
                        thongTin.setEmail(rs.getString("Email"));
                        thongTin.setTenKhongGian(rs.getString("TenKG"));
                        thongTin.setTenChiNhanh(rs.getString("TenCN"));
                        thongTin.setThoiGianBatDau(rs.getTimestamp("ThoiGianDuKienToi"));
                        thongTin.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                        thongTin.setThanhTien(rs.getBigDecimal("ThanhTien"));
                    }
                }

                String trangThaiChuanHoa = chuanHoa(trangThaiHienTai);
                boolean dangChoThanhToan = trangThaiChuanHoa.contains("cho thanh toan");
                boolean daThanhToanChuaCoQr = trangThaiChuanHoa.contains("thanh cong")
                        && (maQrHienTai == null || maQrHienTai.isBlank());
                if (!dangChoThanhToan && !daThanhToanChuaCoQr) {
                    conn.rollback();
                    return null;
                }

                String ghiChuMoi = themGhiChuThuCong(ghiChuHienTai);
                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE DATCHO
                        SET TrangThaiDatTruoc = ?,
                            MaQR = ?,
                            GhiChu = ?,
                            CapNhatLanCuoi = CURRENT_TIMESTAMP
                        WHERE MaDatCho = ?
                        """)) {
                    ps.setString(1, trangThaiDatChoDb(conn, "Đã thanh toán thành công"));
                    ps.setString(2, thongTin.getMaQR());
                    ps.setString(3, ghiChuMoi);
                    ps.setString(4, maDatCho.trim());
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return null;
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?")) {
                    ps.setString(1, trangThaiKhongGianDb(conn, "Đã đặt trước"));
                    ps.setString(2, maKG);
                    ps.executeUpdate();
                }

                conn.commit();
                return thongTin;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[QuanLyDatChoTruocDAO] Lỗi xác nhận thanh toán thủ công: " + ex.getMessage());
                return null;
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception e) {
            System.err.println("[QuanLyDatChoTruocDAO] Lỗi kết nối CSDL khi xác nhận thanh toán thủ công: " + e.getMessage());
            return null;
        }
    }

    public KetQuaNhanChoDTO moPhienTuDatChoThuCong(DatChoTruocDTO dto) {
        if (dto == null || dto.getMaDatCho() == null || dto.getMaDatCho().isBlank()) {
            return new KetQuaNhanChoDTO(false, "Vui lòng chọn đặt chỗ cần tạo phiên.");
        }

        String maDatCho = dto.getMaDatCho().trim();
        String bookingSql = """
                SELECT dc.MaDatCho, dc.TrangThaiDatTruoc, dc.KhoangThoiGianSuDung,
                       NVL(dc.ThanhTien, 0) AS ThanhTien, dc.MaKG, dc.MaKH
                FROM DATCHO dc
                WHERE dc.MaDatCho = ?
                FOR UPDATE
                """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                String trangThai;
                int soGio;
                double thanhTien;
                String maKG;
                String maKH;

                try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
                    ps.setString(1, maDatCho);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new KetQuaNhanChoDTO(false, "Không tìm thấy đặt chỗ " + maDatCho + ".");
                        }
                        trangThai = rs.getString("TrangThaiDatTruoc");
                        soGio = Math.max(1, rs.getInt("KhoangThoiGianSuDung"));
                        thanhTien = rs.getDouble("ThanhTien");
                        maKG = rs.getString("MaKG");
                        maKH = rs.getString("MaKH");
                    }
                }

                String maPhienDaCo = layMaPhienDaCo(conn, maDatCho);
                if (maPhienDaCo != null) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Đặt chỗ này đã có phiên " + maPhienDaCo + ".");
                }

                if (!chuanHoa(trangThai).contains("thanh toan thanh cong")) {
                    conn.rollback();
                    return new KetQuaNhanChoDTO(false, "Đặt chỗ chưa ở trạng thái đã thanh toán thành công nên chưa thể tạo phiên.");
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
                    ps.setString(3, "Đang hoạt động");
                    ps.setString(4, maKG);
                    ps.setString(5, maKH);
                    ps.setString(6, maDatCho);
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
                    ps.setString(6, "Đã thanh toán trước");
                    ps.setString(7, maPhien);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE DATCHO
                        SET TrangThaiDatTruoc = ?,
                            MaQR = NULL,
                            CapNhatLanCuoi = CURRENT_TIMESTAMP
                        WHERE MaDatCho = ?
                        """)) {
                    ps.setString(1, trangThaiDatChoDb(conn, "Đã sử dụng"));
                    ps.setString(2, maDatCho);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?")) {
                    ps.setString(1, trangThaiKhongGianDb(conn, "Đang hoạt động"));
                    ps.setString(2, maKG);
                    ps.executeUpdate();
                }

                conn.commit();
                return new KetQuaNhanChoDTO(true, "Mở phiên thủ công từ đặt chỗ thành công. Mã phiên: " + maPhien,
                        maDatCho, maPhien);
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[QuanLyDatChoTruocDAO] Lỗi mở phiên thủ công từ đặt chỗ: " + ex.getMessage());
                return new KetQuaNhanChoDTO(false, "Không thể mở phiên thủ công: " + ex.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception e) {
            System.err.println("[QuanLyDatChoTruocDAO] Lỗi kết nối CSDL khi mở phiên thủ công: " + e.getMessage());
            return new KetQuaNhanChoDTO(false, "Không thể mở phiên thủ công: " + e.getMessage());
        }
    }

    private String themGhiChuThuCong(String ghiChuHienTai) {
        String ghiChuThuCong = "Nhân viên xác nhận đã nhận chuyển khoản thủ công.";
        if (ghiChuHienTai == null || ghiChuHienTai.isBlank()) {
            return ghiChuThuCong;
        }
        if (ghiChuHienTai.contains(ghiChuThuCong)) {
            return ghiChuHienTai;
        }
        return ghiChuHienTai + " | " + ghiChuThuCong;
    }

    private String layMaPhienDaCo(Connection conn, String maDatCho) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MaPhien FROM PHIENLAMVIEC WHERE MaDatCho = ? FETCH FIRST 1 ROWS ONLY")) {
            ps.setString(1, maDatCho);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("MaPhien") : null;
            }
        }
    }

    private void capNhatTrangThaiKhongGian(Connection conn, DatChoTruocDTO dto) throws SQLException {
        String status = hienThiTrangThaiDatCho(dto.getTrangThaiDatTruoc());
        String trangThaiKG = null;
        if ("Đang chờ thanh toán".equals(status)) {
            trangThaiKG = trangThaiKhongGianDb(conn, "Tam khoa");
        } else if ("Đã thanh toán thành công".equals(status)) {
            trangThaiKG = trangThaiKhongGianDb(conn, "Da dat truoc");
        } else if ("Thanh toán không thành công".equals(status)) {
            trangThaiKG = trangThaiKhongGianDb(conn, "Trong");
        }
        if (trangThaiKG == null) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?")) {
            ps.setString(1, trangThaiKG);
            ps.setString(2, dto.getMaKG());
            ps.executeUpdate();
        }
    }

    private DatChoTruocDTO map(ResultSet rs) throws SQLException {
        DatChoTruocDTO dto = new DatChoTruocDTO();
        dto.setMaDatCho(rs.getString("MaDatCho"));
        dto.setMaKH(rs.getString("MaKH"));
        dto.setHoTenKhachHang(rs.getString("HoTen"));
        dto.setMaKG(rs.getString("MaKG"));
        dto.setTenKhongGian(rs.getString("TenKG"));
        dto.setThoiGianDuKienToi(rs.getTimestamp("ThoiGianDuKienToi"));
        int duration = rs.getInt("KhoangThoiGianSuDung");
        dto.setKhoangThoiGianSuDung(rs.wasNull() ? null : duration);
        dto.setTrangThaiDatTruoc(hienThiTrangThaiDatCho(rs.getString("TrangThaiDatTruoc")));
        dto.setThanhTien(rs.getBigDecimal("ThanhTien"));
        dto.setGhiChu(rs.getString("GhiChu"));
        return dto;
    }

    private String trangThaiDatChoDb(Connection conn, String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("khong thanh cong")) {
            return giaTriDb(conn, "CHK_DC_TRANGTHAI", "khong thanh cong", 2, "Thanh toán không thành công");
        }
        if (normalized.contains("thanh cong")) {
            return giaTriDb(conn, "CHK_DC_TRANGTHAI", "thanh cong", 1, "Đã thanh toán thành công");
        }
        if (normalized.contains("su dung")) {
            return giaTriDb(conn, "CHK_DC_TRANGTHAI", "su dung", 3, "Đã sử dụng");
        }
        return giaTriDb(conn, "CHK_DC_TRANGTHAI", "cho thanh toan", 0, "Đang chờ thanh toán");
    }

    private String trangThaiKhongGianDb(Connection conn, String status) {
        String normalized = chuanHoa(status);
        if (normalized.contains("tam khoa")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "tam khoa", 1, "Tạm khoá");
        }
        if (normalized.contains("dat truoc")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "dat truoc", 2, "Đã đặt trước");
        }
        if (normalized.contains("dang hoat dong")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "dang hoat dong", 3, "Đang hoạt động");
        }
        if (normalized.contains("bao tri")) {
            return giaTriDb(conn, "CHK_KG_TRANGTHAI", "bao tri", 5, "Bảo trì");
        }
        return giaTriDb(conn, "CHK_KG_TRANGTHAI", "trong", 0, "Trống");
    }

    private String giaTriDb(Connection conn, String constraintName, String normalizedNeedle, int fallbackIndex, String fallbackValue) {
        List<String> values = layGiaTriRangBuoc(conn, constraintName);
        for (String value : values) {
            if (chuanHoa(value).contains(normalizedNeedle)) {
                return value;
            }
        }
        if (!values.isEmpty()) {
            int index = Math.max(0, Math.min(fallbackIndex, values.size() - 1));
            return values.get(index);
        }
        return fallbackValue;
    }

    private List<String> layGiaTriRangBuoc(Connection conn, String constraintName) {
        List<String> values = new ArrayList<>();
        if (conn == null || constraintName == null || constraintName.isBlank()) {
            return values;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT search_condition_vc FROM user_constraints WHERE constraint_name = ?")) {
            ps.setString(1, constraintName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String condition = rs.getString(1);
                    if (condition != null) {
                        Matcher matcher = Pattern.compile("'([^']*)'").matcher(condition);
                        while (matcher.find()) {
                            values.add(matcher.group(1));
                        }
                    }
                }
            }
        } catch (SQLException ignored) {
            // Fallback labels keep the management screen usable without metadata access.
        }
        return values;
    }

    private String hienThiTrangThaiDatCho(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa có trạng thái";
        }
        String decoded = giaiMaLoiFont(value);
        String normalized = chuanHoa(decoded);
        if (normalized.contains("cho thanh toan")) {
            return "Đang chờ thanh toán";
        }
        if (normalized.contains("khong thanh cong")) {
            return "Thanh toán không thành công";
        }
        if (normalized.contains("thanh cong")) {
            return "Đã thanh toán thành công";
        }
        if (normalized.contains("su dung")) {
            return "Đã sử dụng";
        }
        return decoded;
    }

    private String chuanHoa(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(giaiMaLoiFont(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String giaiMaLoiFont(String value) {
        if (value == null || value.isBlank() || !coDauHieuLoiFont(value)) {
            return value;
        }
        try {
            String decoded = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            return decoded.indexOf('\uFFFD') >= 0 ? value : decoded;
        } catch (RuntimeException ex) {
            return value;
        }
    }

    private boolean coDauHieuLoiFont(String value) {
        return value.contains("Ã")
                || value.contains("Ä")
                || value.contains("Â")
                || value.contains("Æ")
                || value.contains("áº")
                || value.contains("á»");
    }
}
