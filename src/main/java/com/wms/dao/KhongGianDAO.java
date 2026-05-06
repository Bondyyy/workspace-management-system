package com.wms.dao;

import com.wms.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.wms.model.KhongGianDTO;
import java.sql.CallableStatement;
import java.sql.ResultSetMetaData;
import java.sql.Types;

/**
 *
 * @author kyduy
 */

public class KhongGianDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<String> layDanhSachChiNhanhHoatDong() {
        List<String> danhSach = new ArrayList<>();
        Connection conn = getConn();
        if (conn == null)
            return danhSach;

        String sql = "SELECT TenCN FROM CHINHANH WHERE TrangThai = 'Đang hoạt động'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                danhSach.add(rs.getString("TenCN"));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy danh sách chi nhánh!");
            e.printStackTrace();
        }
        return danhSach;
    }

    public void kiemTraDanhSachKhongGian() {
        Connection conn = getConn();
        if (conn == null) {
            System.out.println("Lỗi: Không lấy được kết nối tới Database!");
            return;
        }
        String sql = "SELECT MaKhongGian, TenKhongGian FROM KHONGGIAN";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            System.out.println("--- DANH SÁCH KHÔNG GIAN ---");
            while (rs.next()) {
                System.out.println("Mã: " + rs.getString("MaKhongGian") + " | Tên: " + rs.getString("TenKhongGian"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> layDanhSachLoaiKhongGian() {
        List<String> danhSach = new ArrayList<>();
        Connection conn = getConn();
        if (conn == null)
            return danhSach;
        String sql = "SELECT TenLoaiKG FROM LOAIKHONGGIAN ORDER BY TenLoaiKG";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                danhSach.add(rs.getString("TenLoaiKG"));
            }
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi lấy danh sách loại: " + e.getMessage());
        }
        return danhSach;
    }

    public boolean kiemTraTinhTrangKhongGian(String tenLoaiKG, String ngayDat, String gioToi) {
        Connection conn = getConn();
        if (conn == null)
            return false;

        String sql = "SELECT COUNT(*) FROM KHONGGIAN kg " +
                "JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "WHERE lkg.TenLoaiKG = ? AND (kg.TrangThaiKG = 'Trống' OR kg.TrangThaiKG = 'TRONG')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenLoaiKG);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public java.time.LocalTime layGioDongCuaCuaChiNhanh(String tenChiNhanh) {
        java.time.LocalTime gioDongCuaDeFault = java.time.LocalTime.of(22, 0);
        Connection conn = getConn();
        if (conn == null)
            return gioDongCuaDeFault;

        String sql = "SELECT ThoiGianDongCua FROM CHINHANH WHERE TenCN = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenChiNhanh.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String timeStr = rs.getString("ThoiGianDongCua");
                    if (timeStr != null && !timeStr.trim().isEmpty()) {
                        if (timeStr.length() > 5) {
                            timeStr = timeStr.substring(0, 5);
                        }
                        return java.time.LocalTime.parse(timeStr);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gioDongCuaDeFault;
    }

    public List<KhongGianDTO> layTatCaKhongGian() {
        return timKiem(null, null, null);
    }

    public List<KhongGianDTO> layTheoChiNhanh(String maCN) {
        return timKiem(null, maCN, null);
    }

    public List<KhongGianDTO> timKiem(String tuKhoa, String maCN, String maLoaiKG) {
        String sql = "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, " +
                "kg.MaCN, cn.TenCN, kg.ToaDoX, kg.ToaDoY, kg.ChieuDai, kg.ChieuRong " +
                "FROM KHONGGIAN kg " +
                "LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                "WHERE 1=1 ";

        List<Object> params = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql += "AND (UPPER(kg.TenKG) LIKE UPPER(?) OR UPPER(kg.MaKG) LIKE UPPER(?)) ";
            params.add("%" + tuKhoa + "%");
            params.add("%" + tuKhoa + "%");
        }
        if (maCN != null && !maCN.isBlank()) {
            sql += "AND kg.MaCN = ? ";
            params.add(maCN);
        }
        if (maLoaiKG != null && !maLoaiKG.isBlank()) {
            sql += "AND kg.MaLoaiKG = ? ";
            params.add(maLoaiKG);
        }
        sql += "ORDER BY kg.MaKG DESC";

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
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-00904")) {
                return executeBasicQuery(tuKhoa, maCN, maLoaiKG);
            }
            e.printStackTrace();
        }
        return list;
    }

    private List<KhongGianDTO> executeBasicQuery(String tuKhoa, String maCN, String maLoaiKG) {
        String sql = "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, " +
                "kg.MaCN, cn.TenCN FROM KHONGGIAN kg " +
                "LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                "WHERE 1=1 ";
        List<Object> params = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql += "AND (UPPER(kg.TenKG) LIKE UPPER(?) OR UPPER(kg.MaKG) LIKE UPPER(?)) ";
            params.add("%" + tuKhoa + "%");
            params.add("%" + tuKhoa + "%");
        }
        if (maCN != null && !maCN.isBlank()) {
            sql += "AND kg.MaCN = ? ";
            params.add(maCN);
        }
        if (maLoaiKG != null && !maLoaiKG.isBlank()) {
            sql += "AND kg.MaLoaiKG = ? ";
            params.add(maLoaiKG);
        }

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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean them(KhongGianDTO dto) {
        if (dto.getMaKG() == null || dto.getMaKG().trim().isEmpty()) {
            dto.setMaKG(taoMaMoi());
        }

        String sql = "INSERT INTO KHONGGIAN (MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN, ToaDoX, ToaDoY, ChieuDai, ChieuRong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dto.getMaKG());
            ps.setString(2, dto.getTenKG());
            ps.setString(3, dto.getTrangThaiKG());
            ps.setString(4, dto.getViTri());
            ps.setString(5, dto.getMaLoaiKG());
            ps.setString(6, dto.getMaCN());
            ps.setInt(7, dto.getToaDoX());
            ps.setInt(8, dto.getToaDoY());
            ps.setInt(9, dto.getChieuDai());
            ps.setInt(10, dto.getChieuRong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-00904") || e.getMessage().contains("ORA-00947")) {
                return themBasic(dto);
            }
            System.err.println("[KhongGianDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    private boolean themBasic(KhongGianDTO dto) {
        String sql = "INSERT INTO KHONGGIAN (MaKG, TenKG, TrangThaiKG, ViTri, MaLoaiKG, MaCN) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dto.getMaKG());
            ps.setString(2, dto.getTenKG());
            ps.setString(3, dto.getTrangThaiKG());
            ps.setString(4, dto.getViTri());
            ps.setString(5, dto.getMaLoaiKG());
            ps.setString(6, dto.getMaCN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhat(KhongGianDTO dto) {
        // Ưu tiên cập nhật trực tiếp vào các cột tọa độ
        if (capNhatDirect(dto)) {
            return true;
        }
        
        // Chỉ thử Stored Procedure nếu cập nhật trực tiếp thất bại
        String sql = "{call SP_CapNhatKhongGian(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = getConn().prepareCall(sql)) {
            cs.setString(1, dto.getMaKG());
            cs.setString(2, dto.getTenKG());
            cs.setString(3, dto.getViTri());
            cs.setString(4, dto.getMaLoaiKG());
            cs.setString(5, dto.getTrangThaiKG());
            cs.setInt(6, dto.getToaDoX());
            cs.setInt(7, dto.getToaDoY());
            cs.setInt(8, dto.getChieuDai());
            cs.setInt(9, dto.getChieuRong());
            cs.registerOutParameter(10, Types.VARCHAR);

            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi cập nhật nghiêm trọng: " + e.getMessage());
            return false; // Trả về false để Form biết là lưu thất bại
        }
    }

    private boolean capNhatDirect(KhongGianDTO dto) {
        String sql = "UPDATE KHONGGIAN SET TenKG = ?, ViTri = ?, MaLoaiKG = ?, TrangThaiKG = ?, " +
                "ToaDoX = ?, ToaDoY = ?, ChieuDai = ?, ChieuRong = ? WHERE MaKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dto.getTenKG());
            ps.setString(2, dto.getViTri());
            ps.setString(3, dto.getMaLoaiKG());
            ps.setString(4, dto.getTrangThaiKG());
            ps.setInt(5, dto.getToaDoX());
            ps.setInt(6, dto.getToaDoY());
            ps.setInt(7, dto.getChieuDai());
            ps.setInt(8, dto.getChieuRong());
            ps.setString(9, dto.getMaKG().trim()); // Sử dụng trim() để tránh lỗi khoảng trắng
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi capNhatDirect: " + e.getMessage());
            return false;
        }
    }

    private boolean capNhatBasic(KhongGianDTO dto) {
        String sql = "UPDATE KHONGGIAN SET TenKG = ?, ViTri = ?, MaLoaiKG = ?, TrangThaiKG = ? WHERE MaKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dto.getTenKG());
            ps.setString(2, dto.getViTri());
            ps.setString(3, dto.getMaLoaiKG());
            ps.setString(4, dto.getTrangThaiKG());
            ps.setString(5, dto.getMaKG().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhongGianDAO] Lỗi capNhatBasic: " + e.getMessage());
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
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maKG) {
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
        String sql = "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, " +
                "kg.MaCN, cn.TenCN, kg.ToaDoX, kg.ToaDoY, kg.ChieuDai, kg.ChieuRong " +
                "FROM KHONGGIAN kg " +
                "LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                "WHERE kg.MaKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            return layTheoMaBasic(maKG);
        }
        return null;
    }

    private KhongGianDTO layTheoMaBasic(String maKG) {
        String sql = "SELECT kg.MaKG, kg.TenKG, kg.TrangThaiKG, kg.ViTri, kg.MaLoaiKG, lkg.TenLoaiKG, " +
                "kg.MaCN, cn.TenCN FROM KHONGGIAN kg " +
                "LEFT JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "LEFT JOIN CHINHANH cn ON kg.MaCN = cn.MaCN " +
                "WHERE kg.MaKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        
        if (hasColumn(rs, "TenLoaiKG")) dto.setTenLoaiKG(rs.getString("TenLoaiKG"));
        if (hasColumn(rs, "TenCN")) dto.setTenCN(rs.getString("TenCN"));

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
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(x)))
                return true;
        }
        return false;
    }

    public String taoMaMoi() {
        return "KG" + (System.currentTimeMillis() % 1000000);
    }
}
