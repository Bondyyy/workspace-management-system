package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.CoSoVatChat.KhongGianDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhongGianDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<KhongGianDTO> layTatCaKhongGian() {
        return timKiem(null, null, null);
    }

    public List<KhongGianDTO> timKiem(String tuKhoa, String maCN, String maLoaiKG) {
        String sqlFull = buildQuery(true, tuKhoa, maCN, maLoaiKG);
        List<Object> params = buildParams(tuKhoa, maCN, maLoaiKG);

        try {
            return executeQuery(sqlFull, params);
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-00904")) {
                String sqlBasic = buildQuery(false, tuKhoa, maCN, maLoaiKG);
                try {
                    return executeQuery(sqlBasic, params);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    private String buildQuery(boolean includeCoords, String tuKhoa, String maCN, String maLoaiKG) {
        StringBuilder sb = new StringBuilder("SELECT MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN ");
        if (includeCoords) {
            sb.append(", ToaDoX, ToaDoY, ChieuDai, ChieuRong ");
        }
        sb.append("FROM KHONGGIAN WHERE 1=1 ");
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sb.append("AND (UPPER(TenKG) LIKE UPPER(?) OR UPPER(MaKG) LIKE UPPER(?)) ");
        }
        if (maCN != null && !maCN.isBlank()) {
            sb.append("AND MaCN = ? ");
        }
        if (maLoaiKG != null && !maLoaiKG.isBlank()) {
            sb.append("AND MaLoaiKG = ? ");
        }
        sb.append("ORDER BY MaKG DESC");
        return sb.toString();
    }

    private List<Object> buildParams(String tuKhoa, String maCN, String maLoaiKG) {
        List<Object> params = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            params.add("%" + tuKhoa + "%");
            params.add("%" + tuKhoa + "%");
        }
        if (maCN != null && !maCN.isBlank())
            params.add(maCN);
        if (maLoaiKG != null && !maLoaiKG.isBlank())
            params.add(maLoaiKG);
        return params;
    }

    private List<KhongGianDTO> executeQuery(String sql, List<Object> params) throws SQLException {
        List<KhongGianDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<KhongGianDTO> layTheoChiNhanh(String maCN) {
        return timKiem(null, maCN, null);
    }

    public boolean them(KhongGianDTO dto) {
        // 1. Sử dụng mã từ DTO nếu có, nếu không thì tự sinh
        if (dto.getMaKG() == null || dto.getMaKG().trim().isEmpty()) {
            dto.setMaKG(taoMaMoi());
        }

        // Chỉ thêm các trường cơ bản, toạ độ sẽ được cập nhật sau trong Sơ đồ
        String sql = "INSERT INTO KHONGGIAN (MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dto.getMaKG());
            ps.setString(2, dto.getTenKG());
            ps.setString(3, dto.getTrangThaiKG());
            ps.setString(4, dto.getViTri());
            ps.setString(5, dto.getMaLoaiKG());
            ps.setString(6, dto.getMaCN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(KhongGianDTO dto) {
        // Sử dụng Stored Procedure mới (10 tham số)
        String sql = "{call SP_CapNhatKhongGian(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = getConn().prepareCall(sql)) {
            cs.setString(1, dto.getMaKG());
            cs.setString(2, dto.getTenKG());
            cs.setString(3, dto.getViTri());
            cs.setString(4, dto.getMaLoaiKG());
            cs.setString(5, dto.getTrangThaiKG()); // Tham số mới bổ sung
            cs.setInt(6, dto.getToaDoX());
            cs.setInt(7, dto.getToaDoY());
            cs.setInt(8, dto.getChieuDai());
            cs.setInt(9, dto.getChieuRong());
            cs.registerOutParameter(10, Types.VARCHAR);

            cs.execute();
            String msg = cs.getString(10);
            System.out.println("[KhongGianDAO] " + msg);
            return true;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTrangThai(String maKG, String trangThai) {
        String sql = "UPDATE KHONGGIAN SET TrangThaiKG = ? WHERE MaKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maKG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi cập nhật trạng thái: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maKG) {
        // Kiểm tra còn phiếu đặt chỗ liên quan
        String sqlCheck = "SELECT COUNT(*) FROM PHIEUDATCHO WHERE MaKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sqlCheck)) {
            ps.setString(1, maKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("[KhongGianDAO] Không thể xóa: có phiếu đặt chỗ liên quan.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi kiểm tra ràng buộc: " + e.getMessage());
            return false;
        }

        String sql = "DELETE FROM KHONGGIAN WHERE MaKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi xóa: " + e.getMessage());
            return false;
        }
    }

    public KhongGianDTO layTheoMa(String maKG) {
        String sqlFull = "SELECT MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN, ToaDoX, ToaDoY, ChieuDai, ChieuRong FROM KHONGGIAN WHERE MaKG = ?";
        String sqlBasic = "SELECT MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN FROM KHONGGIAN WHERE MaKG = ?";

        try {
            return querySingle(sqlFull, maKG);
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-00904")) {
                try {
                    return querySingle(sqlBasic, maKG);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    private KhongGianDTO querySingle(String sql, String maKG) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    private KhongGianDTO mapRow(ResultSet rs) throws SQLException {
        KhongGianDTO dto = new KhongGianDTO();
        dto.setMaKG(rs.getString("MaKG"));
        dto.setTenKG(rs.getString("TenKG"));
        dto.setTrangThaiKG(rs.getString("TrangThaiKG"));
        dto.setViTri(rs.getString("ViTri"));
        dto.setMaLoaiKG(rs.getString("MaLoaiKG"));
        dto.setMaCN(rs.getString("MaCN"));

        // Kiểm tra an toàn trước khi lấy dữ liệu toạ độ
        if (hasColumn(rs, "ToaDoX"))
            dto.setToaDoX(rs.getInt("ToaDoX"));
        if (hasColumn(rs, "ToaDoY"))
            dto.setToaDoY(rs.getInt("ToaDoY"));
        if (hasColumn(rs, "ChieuDai"))
            dto.setChieuDai(rs.getInt("ChieuDai"));
        if (hasColumn(rs, "ChieuRong"))
            dto.setChieuRong(rs.getInt("ChieuRong"));

        return dto;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    public String taoMaMoi() {
        return "KG" + (System.currentTimeMillis() % 1000000);
    }
}
