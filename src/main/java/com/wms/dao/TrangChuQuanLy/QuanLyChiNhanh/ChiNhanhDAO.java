package com.wms.dao.TrangChuQuanLy.QuanLyChiNhanh;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyChiNhanh.ChiNhanhDTO;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChiNhanhDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<ChiNhanhDTO> layDanhSachChiNhanh() {
        return timKiemChiNhanh(null); // Gọi hàm tìm kiếm với từ khóa null để lấy tất cả
    }

    public boolean themChiNhanh(ChiNhanhDTO chiNhanh) {
        String sql = "{call SP_ThemChiNhanh(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, null);
            cs.setString(2, chiNhanh.getTenCN());
            cs.setString(3, chiNhanh.getDiaChi());
            cs.setString(4, chiNhanh.getThoiGianMoCua());
            cs.setString(5, chiNhanh.getThoiGianDongCua());
            cs.setString(6, chiNhanh.getDuongDayNong());
            cs.setString(7, chiNhanh.getTrangThai());
            cs.registerOutParameter(8, java.sql.Types.VARCHAR);
            
            cs.execute();
            String message = cs.getString(8);
            System.out.println(message);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi thêm chi nhánh: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatChiNhanh(ChiNhanhDTO chiNhanh) {
        String sql = "{call SP_CapNhatChiNhanh(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, chiNhanh.getMaCN());
            cs.setString(2, chiNhanh.getTenCN());
            cs.setString(3, chiNhanh.getDiaChi());
            cs.setString(4, chiNhanh.getThoiGianMoCua());
            cs.setString(5, chiNhanh.getThoiGianDongCua());
            cs.setString(6, chiNhanh.getDuongDayNong());
            cs.setString(7, chiNhanh.getTrangThai());
            cs.registerOutParameter(8, java.sql.Types.VARCHAR);
            
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật chi nhánh: " + e.getMessage());
            return false;
        }
    }

    public List<ChiNhanhDTO> timKiemChiNhanh(String tuKhoa) {
        List<ChiNhanhDTO> list = new ArrayList<>();
        String sql = "{call SP_TraCuuChiNhanh(?, ?, ?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, tuKhoa);
            cs.setString(2, null); // TrangThai = null để lấy tất cả
            cs.registerOutParameter(3, -10); // OracleTypes.CURSOR = -10
            cs.registerOutParameter(4, java.sql.Types.VARCHAR);
            
            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                while (rs.next()) {
                    ChiNhanhDTO cn = new ChiNhanhDTO();
                    cn.setMaCN(rs.getString("MaCN"));
                    cn.setTenCN(rs.getString("TenCN"));
                    cn.setDiaChi(rs.getString("DiaChi"));
                    cn.setDuongDayNong(rs.getString("DuongDayNong"));
                    cn.setThoiGianMoCua(rs.getString("ThoiGianMoCua"));
                    cn.setThoiGianDongCua(rs.getString("ThoiGianDongCua"));
                    cn.setTrangThai(rs.getString("TrangThai"));
                    list.add(cn);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tìm kiếm chi nhánh: " + e.getMessage());
        }
        return list;
    }

    public String demoNonRepeatableRead(String maCN, boolean serializable) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            if (serializable) {
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            } else {
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }

            String hotlineLan1 = docHotlineChiNhanh(conn, maCN);
            Thread.sleep(15000);
            String hotlineLan2 = docHotlineChiNhanh(conn, maCN);

            boolean phatSinh = !Objects.equals(hotlineLan1, hotlineLan2);
            StringBuilder sb = new StringBuilder();
            sb.append("Isolation: ")
                    .append(serializable ? "SERIALIZABLE" : "READ_COMMITTED")
                    .append("\n\n");
            sb.append("Mã chi nhánh: ").append(maCN).append("\n");
            sb.append("Hotline lần 1: ").append(formatDemoValue(hotlineLan1)).append("\n");
            sb.append("Hotline lần 2: ").append(formatDemoValue(hotlineLan2)).append("\n\n");
            if (phatSinh) {
                sb.append("Kết quả: Phát sinh Non-repeatable Read vì cùng một transaction đọc cùng một dòng dữ liệu hai lần nhưng nhận được hai giá trị khác nhau.");
            } else {
                sb.append("Kết quả: Không phát sinh Non-repeatable Read.");
            }
            return sb.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Lỗi demo Non-repeatable Read: Luồng demo bị gián đoạn.";
        } catch (SQLException | RuntimeException e) {
            return "Lỗi demo Non-repeatable Read: " + e.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    System.err.println("[ChiNhanhDAO] Lỗi rollback demo Non-repeatable Read: " + e.getMessage());
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[ChiNhanhDAO] Lỗi đóng connection demo Non-repeatable Read: " + e.getMessage());
                }
            }
        }
    }

    private String docHotlineChiNhanh(Connection conn, String maCN) throws SQLException {
        String sql = "SELECT DuongDayNong FROM CHINHANH WHERE MaCN = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("DuongDayNong");
                }
            }
        }
        return null;
    }

    private String formatDemoValue(String value) {
        return value != null ? value : "(null)";
    }

    public boolean voHieuHoaChiNhanh(String maCN) {
        String sql = "{call SP_VoHieuHoaChiNhanh(?, ?)}";
        try (Connection conn = getConn();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, maCN);
            cs.registerOutParameter(2, java.sql.Types.VARCHAR);
            
            cs.execute();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi vô hiệu hóa chi nhánh: " + e.getMessage());
            return false;
        }
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM CHINHANH";
        try (Connection conn = getConn();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("Lỗi đếm số lượng chi nhánh: " + e.getMessage());
        }
        return 0;
    }

    public String taoMaMoi() {
        return "";
    }
}
