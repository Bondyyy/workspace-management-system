package com.wms.dao.TrangChuQuanLy.QuanLyPhieuGiamGia;
import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;


public class PhieuGiamGiaDAO {
    private static final String SQL_DEMO_PHANTOM_READ =
            "SELECT MaPGG, MaChuSoPGG, GiaTriGiamGia, SLToiDa, TrangThai " +
            "FROM PHIEUGIAMGIA " +
            "WHERE TrangThai = 'Đang có hiệu lực' " +
            "ORDER BY NgayTaoPGG DESC";
    
    // Gộp layThongTinVoucher vào timTheoMa để tránh trùng lặp code
    public PhieuGiamGiaDTO timTheoMa(String maPGG) {
        String sql = "SELECT * FROM PHIEUGIAMGIA WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tìm theo mã: " + e.getMessage());
        }
        return null;
    }

    public PhieuGiamGiaDTO timTheoMaChuSo(String maChuSoPGG) {
        String sql = "SELECT * FROM PHIEUGIAMGIA WHERE UPPER(MaChuSoPGG) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChuSoPGG.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tìm theo mã chữ số: " + e.getMessage());
        }
        return null;
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM PHIEUGIAMGIA";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    public String taoMaMoi() {
        return "";
    }

    public List<PhieuGiamGiaDTO> layDanhSach() {
        return traCuu(null, null, "lấy danh sách");
    }

    public List<PhieuGiamGiaDTO> timKiem(String keyword) {
        return traCuu(keyword, null, "tìm kiếm");
    }

    public String demoPhantomRead(boolean serializable) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            if (serializable) {
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            } else {
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }

            List<String> lan1 = docDanhSachPhieuGiamGiaDangHieuLuc(conn);
            Thread.sleep(15000);
            List<String> lan2 = docDanhSachPhieuGiamGiaDangHieuLuc(conn);

            StringBuilder sb = new StringBuilder();
            sb.append("Isolation: ")
                    .append(serializable ? "SERIALIZABLE" : "READ_COMMITTED")
                    .append("\n\n");
            appendDanhSachDemo(sb, "lần 1", lan1);
            sb.append("\n");
            appendDanhSachDemo(sb, "lần 2", lan2);
            sb.append("\n");
            if (lan2.size() > lan1.size()) {
                sb.append("Kết quả: Phát sinh Phantom Read vì lần đọc 2 xuất hiện thêm phiếu giảm giá mới.");
            } else {
                sb.append("Kết quả: Không phát sinh Phantom Read.");
            }
            return sb.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Lỗi demo Phantom Read: Luồng demo bị gián đoạn.";
        } catch (SQLException | RuntimeException e) {
            return "Lỗi demo Phantom Read: " + e.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    System.err.println("[PhieuGiamGiaDAO] Lỗi rollback demo Phantom Read: " + e.getMessage());
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[PhieuGiamGiaDAO] Lỗi đóng connection demo Phantom Read: " + e.getMessage());
                }
            }
        }
    }

    private List<String> docDanhSachPhieuGiamGiaDangHieuLuc(Connection conn) throws SQLException {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DEMO_PHANTOM_READ);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MaPGG") + " | " +
                        rs.getString("MaChuSoPGG") + " | Giảm: " +
                        dinhDangSoDemo(rs.getBigDecimal("GiaTriGiamGia")) + " | SL tối đa: " +
                        rs.getInt("SLToiDa"));
            }
        }
        return list;
    }

    private void appendDanhSachDemo(StringBuilder sb, String tenLanDoc, List<String> danhSach) {
        sb.append("--- Danh sách ").append(tenLanDoc).append(" ---\n");
        sb.append("Số phiếu: ").append(danhSach.size()).append("\n");
        for (int i = 0; i < danhSach.size(); i++) {
            sb.append(i + 1).append(". ").append(danhSach.get(i)).append("\n");
        }
    }

    private String dinhDangSoDemo(java.math.BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private List<PhieuGiamGiaDTO> traCuu(String keyword, String trangThai, String thaoTac) {
        List<PhieuGiamGiaDTO> list = new ArrayList<>();
        String sql = "{call SP_TraCuuPhieuGiamGia(?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, keyword);
            cs.setString(2, trangThai);
            cs.registerOutParameter(3, OracleTypes.CURSOR);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();

            String message = cs.getString(4);
            if (message != null && message.startsWith("Lỗi")) {
                throw new SQLException(message);
            }

            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi " + thaoTac + ": " + e.getMessage());
        }
        return list;
    }

    public boolean themMoi(PhieuGiamGiaDTO dto) {
        String sql = "{call SP_ThemPhieuGiamGia(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, dto.getMaChuSoPGG());
            cs.setDouble(2, dto.getGiaTriGiamGia());
            cs.setDouble(3, dto.getGiaTriApDungToiThieu());
            cs.setTimestamp(4, new Timestamp(dto.getNgayBatDauApDung().getTime()));
            cs.setTimestamp(5, new Timestamp(dto.getNgayKetThucApDung().getTime()));
            cs.setInt(6, dto.getSlToiDa());
            cs.setString(7, dto.getMaNV());
            cs.setString(8, dto.getTrangThai() != null ? dto.getTrangThai() : "Đang có hiệu lực");
            cs.registerOutParameter(9, Types.VARCHAR);

            cs.execute();

            String message = cs.getString(9);
            if (message != null && message.startsWith("Lỗi")) {
                System.err.println("[PhieuGiamGiaDAO] " + message);
                return false;
            }

            return true;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi thêm mới bằng SP: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(PhieuGiamGiaDTO dto) {
        String sql = "UPDATE PHIEUGIAMGIA SET MaChuSoPGG = ?, GiaTriGiamGia = ?, GiaTriApDungToiThieu = ?, " +
                     "NgayBatDauApDung = ?, NgayKetThucApDung = ?, SLToiDa = ?, TrangThai = ? WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaChuSoPGG());
            ps.setDouble(2, dto.getGiaTriGiamGia());
            ps.setDouble(3, dto.getGiaTriApDungToiThieu());
            ps.setTimestamp(4, new Timestamp(dto.getNgayBatDauApDung().getTime()));
            ps.setTimestamp(5, new Timestamp(dto.getNgayKetThucApDung().getTime()));
            ps.setInt(6, dto.getSlToiDa());
            ps.setString(7, dto.getTrangThai() != null ? dto.getTrangThai() : "Đang có hiệu lực");
            ps.setString(8, dto.getMaPGG());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maPGG) {
        String sql = "UPDATE PHIEUGIAMGIA SET TrangThai = 'Đã vô hiệu hoá' WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi ngưng hoạt động: " + e.getMessage());
            return false;
        }
    }

    public boolean tangSoLuongDaDung(String maPGG) {
        String sql = "UPDATE PHIEUGIAMGIA SET SLDaDung = SLDaDung + 1 WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tăng số lượng đã dùng: " + e.getMessage());
            return false;
        }
    }

    private String tinhTrangThaiTuDong(PhieuGiamGiaDTO dto, String dbTrangThai) {
        if ("Đã vô hiệu hoá".equals(dbTrangThai) || "Đã ngưng".equals(dbTrangThai)) {
            return "Đã vô hiệu hoá";
        }
        java.util.Date now = new java.util.Date();
        if (dto.getNgayBatDauApDung() != null && now.before(dto.getNgayBatDauApDung())) {
            return "Chưa đến hạn bắt đầu";
        }
        if (dto.getNgayKetThucApDung() != null && now.after(dto.getNgayKetThucApDung())) {
            return "Hết hiệu lực";
        }
        if (dto.getSlToiDa() > 0 && dto.getSlDaDung() >= dto.getSlToiDa()) {
            return "Hết hiệu lực";
        }
        return "Đang có hiệu lực";
    }

    private PhieuGiamGiaDTO mapRow(ResultSet rs) throws SQLException {
        PhieuGiamGiaDTO dto = new PhieuGiamGiaDTO();
        dto.setMaPGG(rs.getString("MaPGG"));
        dto.setMaChuSoPGG(rs.getString("MaChuSoPGG"));
        dto.setGiaTriGiamGia(rs.getDouble("GiaTriGiamGia"));
        dto.setGiaTriApDungToiThieu(rs.getDouble("GiaTriApDungToiThieu"));
        dto.setNgayBatDauApDung(rs.getTimestamp("NgayBatDauApDung"));
        dto.setNgayKetThucApDung(rs.getTimestamp("NgayKetThucApDung"));
        dto.setSlDaDung(rs.getInt("SLDaDung"));
        dto.setSlToiDa(rs.getInt("SLToiDa"));
        dto.setNgayTaoPGG(rs.getTimestamp("NgayTaoPGG"));
        dto.setMaNV(rs.getString("MaNV"));
        
        String dbTrangThai = rs.getString("TrangThai");
        dto.setTrangThai(tinhTrangThaiTuDong(dto, dbTrangThai));
        return dto;
    }
}
